// 도달 불가·미사용 에러코드 찾기. 에러맵과 소스 인덱스를 대조한다.
import { readFileSync, readdirSync, statSync } from "node:fs";
import path from "node:path";
import { thrownExceptions, codeOfException, errorCodeTable, findSources, allSourceFiles } from "./analyze.mjs";

const ROOT = process.env.STORIX_ROOT;
const map = JSON.parse(readFileSync(process.argv[2], "utf8"));

const codes = errorCodeTable();

const exceptionFiles = allSourceFiles().filter((f) => f.endsWith("Exception.java"));

const exceptionToCode = new Map();
for (const f of exceptionFiles) {
  const m = readFileSync(f, "utf8").match(/super\(\s*ErrorCode\.([A-Z][A-Z0-9_]*)/);
  if (m) exceptionToCode.set(path.basename(f, ".java"), { code: m[1], file: f });
}

const thrown = thrownExceptions();

// 1. 예외 클래스가 하나도 안 쓰는 ErrorCode
// 예외 클래스뿐 아니라 소스 전체에서 ErrorCode.X 로 쓰이는지 본다 (디코더가 직접 참조하는 경우가 있다)
const allSources = allSourceFiles().filter((f) => !f.endsWith("/ErrorCode.java"));
const referenced = new Set();
for (const f of allSources) {
  for (const m of readFileSync(f, "utf8").matchAll(/ErrorCode\.([A-Z][A-Z0-9_]*)/g)) referenced.add(m[1]);
}
// ErrorCode.valueOf("NOE" + raw) 처럼 이름을 런타임에 조립하는 곳이 있다.
// 그 파일에 등장하는 접두사로 시작하는 상수는 동적으로 쓰이는 것으로 본다.
const dynamicPrefixes = new Set();
for (const f of allSources) {
  const text = readFileSync(f, "utf8");
  if (!/ErrorCode\.valueOf\(/.test(text)) continue;

  // "NOE" + raw 처럼 접두사를 문자열로 붙이는 경우
  for (const m of text.matchAll(/"([A-Z][A-Z0-9_]{1,8})"\s*\+/g)) dynamicPrefixes.add(m[1]);

  // 카카오처럼 응답값을 그대로 넘기는 경우엔 접두사가 안 보인다.
  // 같은 파일이 참조하는 다른 ErrorCode 이름(KOE_INVALID_REQUEST)에서 계열을 추론한다.
  for (const m of text.matchAll(/ErrorCode\.([A-Z][A-Z0-9]*)_/g)) dynamicPrefixes.add(m[1]);
}
const dynamic = (n) => [...dynamicPrefixes].some((p) => n.startsWith(p));

const orphanCodes = [...codes.keys()].filter((n) => !referenced.has(n) && !dynamic(n));
const dynamicCodes = [...codes.keys()].filter((n) => !referenced.has(n) && dynamic(n));

// 2. 선언됐지만 어디서도 안 던지는 예외 클래스
const neverThrown = [...exceptionToCode.keys()].filter((c) => !thrown.has(c));

// 3. 인증 뒤에서만 도달하는 401 도메인 에러
const endpointsOf = new Map();
for (const v of Object.values(map.endpoints)) {
  for (const e of v.own) {
    if (!endpointsOf.has(e.code)) endpointsOf.set(e.code, []);
    endpointsOf.get(e.code).push(v.security);
  }
}
const unreachable401 = [];
for (const [code, securities] of endpointsOf) {
  const meta = [...codes.values()].find((c) => c.code === code);
  if (meta?.status !== "UNAUTHORIZED") continue;
  if (securities.every((s) => s !== "public")) unreachable401.push({ code, message: meta.message, n: securities.length });
}

const show = (title, rows) => {
  console.log(`\n${title} — ${rows.length}건`);
  rows.slice(0, 25).forEach((r) => console.log("  " + r));
};

show("① 예외 클래스도 핸들러도 안 쓰는 ErrorCode", orphanCodes.map((n) => `${codes.get(n).code.padEnd(30)} ${n}  "${codes.get(n).message}"`));
show("①-b 런타임 valueOf 로 붙는 코드 (미사용 아님)", dynamicCodes.map((n) => `${codes.get(n).code.padEnd(30)} ${n}`));
show("② 선언만 되고 아무 데서도 안 던지는 예외", neverThrown.map((c) => `${c.padEnd(44)} → ${exceptionToCode.get(c).code}`));
show("③ 인증 뒤에서만 닿는 401 도메인 에러 (도달 불가 의심)", unreachable401.map((r) => `${r.code.padEnd(30)} "${r.message}"  · 엔드포인트 ${r.n}개 전부 인증 필요`));
