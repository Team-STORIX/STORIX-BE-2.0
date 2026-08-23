// 소스만 읽어서 "엔드포인트별로 던질 수 있는 에러코드"를 뽑는다. 앱 코드는 건드리지 않는다.
import { readFileSync, readdirSync, statSync } from "node:fs";
import path from "node:path";

const ROOT = process.env.STORIX_ROOT;
if (!ROOT) throw new Error("STORIX_ROOT 환경변수가 필요합니다 (저장소 루트)");
const MAX_DEPTH = 12;

function javaFiles(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const p = path.join(dir, name);
    const st = statSync(p);
    if (st.isDirectory()) javaFiles(p, out);
    else if (name.endsWith(".java")) out.push(p);
  }
  return out;
}

// 모듈 목록을 박아두면 모듈이 늘 때 조용히 빠진다. src/main/java 가 있는 디렉터리를 모듈로 본다.
function sourceRoots(root) {
  return readdirSync(root, { withFileTypes: true })
    .filter((d) => d.isDirectory() && !d.name.startsWith("."))
    .map((d) => path.join(root, d.name, "src/main/java"))
    .filter((p) => {
      try {
        return statSync(p).isDirectory();
      } catch {
        return false;
      }
    });
}

const roots = sourceRoots(ROOT);
if (!roots.length) throw new Error(`${ROOT} 아래에서 src/main/java 를 찾지 못했습니다`);
const files = roots.flatMap((r) => javaFiles(r));

// ── 예외 클래스 → ErrorCode ──────────────────────────────────
const exceptionCode = new Map();
for (const f of files) {
  if (!f.endsWith("Exception.java")) continue;
  const text = readFileSync(f, "utf8");
  const cls = path.basename(f, ".java");
  const m = text.match(/super\(\s*ErrorCode\.([A-Z][A-Z0-9_]*)/);
  if (m) exceptionCode.set(cls, m[1]);
}

// ── 클래스 인덱스 ────────────────────────────────────────────
function matchBody(text, openIdx) {
  let depth = 0;
  for (let i = openIdx; i < text.length; i++) {
    if (text[i] === "{") depth++;
    else if (text[i] === "}") {
      depth--;
      if (depth === 0) return text.slice(openIdx, i + 1);
    }
  }
  return text.slice(openIdx);
}

const classes = new Map();
const implsOf = new Map();

for (const f of files) {
  const text = readFileSync(f, "utf8");
  const cls = path.basename(f, ".java");
  const fields = new Map();
  for (const m of text.matchAll(/private\s+(?:final\s+)?([A-Z][\w<>,.\s]*?)\s+(\w+)\s*[;=]/g)) {
    const type = m[1].trim().replace(/<.*/, "").replace(/.*\./, "");
    fields.set(m[2], type);
  }
  const methods = new Map();
  const signatures = new Map();
  for (const m of text.matchAll(/(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?[\w<>,.\[\]\s?]+?\s+(\w+)\s*\([^;{]*?\)\s*(?:throws [\w,\s]+)?\{/g)) {
    const open = text.indexOf("{", m.index + m[0].length - 1);
    methods.set(m[1], matchBody(text, open));
    signatures.set(m[1], m[0]);
  }
  // 생성자는 반환타입이 없어서 위 정규식에 안 걸린다. 엔티티 검증이 여기 있는 경우가 많다.
  const ctorRe = new RegExp(`(?:public|protected|private)\\s+${cls}\\s*\\([^;{]*?\\)\\s*\\{`, "g");
  const ctorBodies = [];
  for (const m of text.matchAll(ctorRe)) {
    ctorBodies.push(matchBody(text, text.indexOf("{", m.index + m[0].length - 1)));
  }
  if (ctorBodies.length) methods.set("<init>", ctorBodies.join("\n"));

  const impl = text.match(/class\s+\w+[^{]*implements\s+([\w,\s<>]+?)\s*\{/);
  const ifaces = impl ? impl[1].split(",").map((x) => x.trim().replace(/<.*/, "")) : [];
  for (const i of ifaces) {
    if (!implsOf.has(i)) implsOf.set(i, []);
    implsOf.get(i).push(cls);
  }
  classes.set(cls, { file: f, text, fields, methods, signatures, ifaces });
}

// ── ErrorCode 표 (경로가 아니라 enum 선언으로 찾는다) ────────
const errorCodes = new Map();
{
  const source = [...classes.values()].find((info) => /enum ErrorCode\b/.test(info.text));
  if (!source) throw new Error("ErrorCode enum 을 찾지 못했습니다");
  const re = /^\s{4}([A-Z][A-Z0-9_]*)\(HttpStatus\.(\w+),\s*"([^"]+)",\s*"([^"]*)"\)/gm;
  let m;
  while ((m = re.exec(source.text))) errorCodes.set(m[1], { status: m[2], code: m[3], message: m[4] });
}

export function allSourceFiles() {
  return files;
}

export function errorCodeTable() {
  return errorCodes;
}

// ── 호출 그래프를 타고 에러 수집 ─────────────────────────────
function collect(clsName, methodName, depth, seen, found, trail) {
  if (depth > MAX_DEPTH) return;
  const key = `${clsName}#${methodName}`;
  if (seen.has(key)) return;
  seen.add(key);

  // 인터페이스도 파일이라 classes 에 잡힌다. 거기서 멈추면 포트 뒤 구현체를 영영 못 본다.
  const targets = new Set([clsName, ...(implsOf.get(clsName) ?? [])]);
  for (const t of targets) {
    const cls = classes.get(t);
    const body = cls?.methods.get(methodName);
    if (!body) continue;

    // throw 뿐 아니라 orElseThrow(() -> X.EXCEPTION) 처럼 값으로 넘기는 자리도 잡아야 한다.
    for (const re of [/throw\s+new\s+(\w+Exception)/g, /(\w+Exception)\.EXCEPTION/g]) {
      for (const m of body.matchAll(re)) {
        const code = exceptionCode.get(m[1]);
        if (code) found.set(code, [...trail, `${t}.${methodName}`]);
      }
    }

    // 지역변수로 받아 호출하는 경우가 많다 (Entity e = adaptor.get(...); e.update(...))
    const locals = new Map();
    for (const m of body.matchAll(/(?:^|[;{(}\s])([A-Z]\w*)(?:<[^>]*>)?\s+(\w+)\s*=/g)) {
      locals.set(m[2], m[1]);
    }

    for (const m of body.matchAll(/(\w+)\.(\w+)\s*\(/g)) {
      const [, recv, callee] = m;
      const next = [...trail, `${t}.${methodName}`];
      // 빌더는 결국 생성자를 부른다
      if (callee === "builder" && /^[A-Z]/.test(recv)) {
        collect(recv, "<init>", depth + 1, seen, found, next);
        continue;
      }
      const type = cls.fields.get(recv) ?? locals.get(recv);
      if (type) collect(type, callee, depth + 1, seen, found, next);
      else if (/^[A-Z]/.test(recv)) collect(recv, callee, depth + 1, seen, found, next);
    }

    for (const m of body.matchAll(/new\s+([A-Z]\w*)\s*\(/g)) {
      collect(m[1], "<init>", depth + 1, seen, found, [...trail, `${t}.${methodName}`]);
    }
    for (const m of body.matchAll(/(?<![.\w])(\w+)\s*\(/g)) {
      if (cls.methods.has(m[1]) && m[1] !== methodName) {
        collect(t, m[1], depth + 1, seen, found, [...trail, `${t}.${methodName}`]);
      }
    }
  }
}

// ── 컨트롤러 엔드포인트 ──────────────────────────────────────
function params(text, open) {
  let depth = 0;
  for (let i = open; i < text.length; i++) {
    if (text[i] === "(") depth++;
    else if (text[i] === ")") {
      depth--;
      if (depth === 0) return text.slice(open, i + 1);
    }
  }
  return "()";
}

const MAPPINGS = { GetMapping: "GET", PostMapping: "POST", PutMapping: "PUT", PatchMapping: "PATCH", DeleteMapping: "DELETE" };

export function endpoints() {
  const out = [];
  for (const [cls, info] of classes) {
    if (!/@RestController/.test(info.text)) continue;
    const base = info.text.match(/@RequestMapping\("([^"]*)"\)/)?.[1] ?? "";

    // 매핑 어노테이션과 메서드 선언 사이에 @Operation 설명이 길게 들어간다.
    // 고정 폭으로 훑으면 그런 핸들러를 통째로 놓쳐서, 선언 목록을 따로 만들고 이어붙인다.
    const decls = [...info.text.matchAll(/(?:public|protected)\s+[\w<>,.\[\]\s?]+?\s+(\w+)\s*\(/g)];
    const annos = [...info.text.matchAll(/@(Get|Post|Put|Patch|Delete)Mapping(?:\(([^)]*)\))?/g)];

    annos.forEach((anno, i) => {
      const nextAnno = annos[i + 1]?.index ?? Infinity;
      const decl = decls.find((d) => d.index > anno.index && d.index < nextAnno);
      if (!decl) return;

      const sub = anno[2]?.match(/"([^"]*)"/)?.[1] ?? "";
      const open = info.text.indexOf("(", decl.index + decl[0].length - 1);
      out.push({
        method: MAPPINGS[`${anno[1]}Mapping`],
        path: (base + sub) || "/",
        controller: cls,
        handler: decl[1],
        signature: params(info.text, open),
      });
    });
  }
  return out;
}

export function handlerText(ep) {
  return classes.get(ep.controller)?.methods.get(ep.handler) ?? "";
}

export function handlerSignature(ep) {
  return classes.get(ep.controller)?.signatures.get(ep.handler) ?? "";
}

export function controllerText(name) {
  return classes.get(name)?.text ?? "";
}

export function errorsOf(ep) {
  const found = new Map();
  collect(ep.controller, ep.handler, 0, new Set(), found, []);
  return [...found.entries()]
    .map(([name, trail]) => ({ name, ...errorCodes.get(name), trail }))
    .sort((a, b) => (a.status + a.code).localeCompare(b.status + b.code));
}

/** 소스 어디에서든 실제로 던져지는 예외 클래스와 그 위치. 엔드포인트에 안 붙은 걸 찾는 데 쓴다. */
export function thrownExceptions() {
  const out = new Map();
  for (const [cls, info] of classes) {
    // orElseThrow(X::new) 처럼 생성자 참조로 넘기는 자리도 던지는 것으로 본다
    for (const re of [/throw\s+new\s+(\w+Exception)/g, /(\w+Exception)\.EXCEPTION/g, /(\w+Exception)::new/g]) {
      for (const m of info.text.matchAll(re)) {
        if (!exceptionCode.has(m[1])) continue;
        if (!out.has(m[1])) out.set(m[1], new Set());
        out.get(m[1]).add(info.file);
      }
    }
  }
  return out;
}

/** 경로 대신 내용으로 소스를 찾는다. 파일이 옮겨져도 따라간다. */
export function findSources(pattern) {
  return [...classes.entries()]
    .filter(([, info]) => pattern.test(info.text))
    .map(([cls, info]) => ({ cls, file: info.file, text: info.text }));
}

/** 호출 그래프를 타고 그 메서드에서 닿을 수 있는 예외 클래스 이름들. */
export function exceptionsFrom(clsName, methodName, maxDepth = MAX_DEPTH) {
  const found = new Map();
  collect(clsName, methodName, MAX_DEPTH - maxDepth, new Set(), found, []);
  return [...found.keys()];
}

export function codeOfException(name) {
  return exceptionCode.get(name);
}

export const stats = {
  files: files.length,
  errorCodes: errorCodes.size,
  exceptions: exceptionCode.size,
  classes: classes.size,
};
