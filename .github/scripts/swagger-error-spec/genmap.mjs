import { readFileSync, writeFileSync, mkdirSync, readdirSync } from "node:fs";
import path from "node:path";
import { endpoints, errorsOf, handlerText, handlerSignature, thrownExceptions, codeOfException, findSources, exceptionsFrom } from "./analyze.mjs";

const ROOT = process.env.STORIX_ROOT;
const FILTER_DEPTH = 3;

/** 파일 경로는 언제든 바뀐다. 프레임워크 계약(enum·어노테이션·상속)으로 찾는다. */
function sourceOf(label, pattern) {
  const found = findSources(pattern);
  if (!found.length) throw new Error(`${label} 소스를 찾지 못했습니다: ${pattern}`);
  return found[0].text;
}
const STATUS_NUM = { BAD_REQUEST:400, UNAUTHORIZED:401, PAYMENT_REQUIRED:402, FORBIDDEN:403, NOT_FOUND:404,
  METHOD_NOT_ALLOWED:405, NOT_ACCEPTABLE:406, REQUEST_TIMEOUT:408, CONFLICT:409, GONE:410, PRECONDITION_FAILED:412,
  PAYLOAD_TOO_LARGE:413, URI_TOO_LONG:414, UNSUPPORTED_MEDIA_TYPE:415, UNPROCESSABLE_ENTITY:422, LOCKED:423,
  TOO_MANY_REQUESTS:429, INTERNAL_SERVER_ERROR:500, NOT_IMPLEMENTED:501, BAD_GATEWAY:502, SERVICE_UNAVAILABLE:503,
  GATEWAY_TIMEOUT:504 };

// 매핑에 없는 상태가 조용히 500 으로 떨어지면 스펙이 틀어진다. 눈에 띄게 남긴다.
const unknownStatuses = new Set();
function statusOf(name) {
  const n = STATUS_NUM[name];
  if (n) return n;
  unknownStatuses.add(name);
  return 500;
}

// ErrorCode 표
const errorCodes = new Map();
{
  const text = sourceOf("ErrorCode", /enum ErrorCode\b/);
  for (const m of text.matchAll(/^\s{4}([A-Z][A-Z0-9_]*)\(HttpStatus\.(\w+),\s*"([^"]+)",\s*"([^"]*)"\)/gm)) {
    errorCodes.set(m[1], { status: STATUS_NUM[m[2]] ?? 500, code: m[3], message: m[4] });
  }
}
const codeOf = (name) => errorCodes.get(name);

// SecurityConfig 규칙 (선언 순서대로 먼저 맞는 것)
const rules = [];
{
  const text = sourceOf("SecurityConfig", /SecurityFilterChain\s+\w+\s*\(/);
  // 스웨거 전용 체인(Order 1)이 아니라 API 체인만 본다
  const start = text.indexOf("SecurityFilterChain securityFilterChain(");
  const nextBean = text.indexOf("@Bean", start);
  const body = text.slice(start, nextBean > 0 ? nextBean : undefined);
  for (const m of body.matchAll(/\.requestMatchers\((?:HttpMethod\.(\w+),\s*)?"([^"]+)"\)\s*\.\s*(permitAll|hasRole|authenticated)\(?\s*"?(\w+)?/g)) {
    rules.push({ method: m[1] ?? null, pattern: m[2], rule: m[3], role: m[4] ?? null });
  }
  const fallback = body.match(/\.anyRequest\(\)\s*\.\s*(permitAll|hasRole|authenticated)\(?\s*"?(\w+)?/);
  rules.push({ method: null, pattern: "/**", rule: fallback?.[1] ?? "authenticated", role: fallback?.[2] ?? null });
}

function toRegex(pattern) {
  const src = pattern
    .replace(/\{[^}]+\}/g, "§VAR§")
    .replace(/[.+^$()|[\]\\]/g, "\\$&")
    .replace(/\*\*/g, "§ANY§")
    .replace(/\*/g, "[^/]*")
    .replace(/§ANY§/g, ".*")
    .replace(/§VAR§/g, "[^/]+");
  return new RegExp(`^${src}$`);
}
const compiled = rules.map((r) => ({ ...r, re: toRegex(r.pattern) }));

function securityOf(ep) {
  for (const r of compiled) {
    if (r.method && r.method !== ep.method) continue;
    if (r.re.test(ep.path)) return r;
  }
  return { rule: "authenticated", role: null };
}

// GlobalExceptionHandler 에서 공통 에러코드를 읽는다 (하드코딩 대신)
const commonByException = new Map();
const withFieldErrors = new Set();
{
  const text = sourceOf("예외 핸들러", /@RestControllerAdvice/);
  // 핸들러 하나가 여러 ErrorCode 를 쓸 수 있다. 구간을 잘라서 그 안의 코드를 모두 모은다.
  const segments = text.split(/@ExceptionHandler\(/).slice(1);
  for (const seg of segments) {
    const name = seg.match(/^(\w+)\.class/)?.[1];
    if (!name) continue;
    const codes = [...new Set([...seg.matchAll(/ErrorCode\.([A-Z_0-9]+)/g)].map((m) => m[1]))];
    if (codes.length) commonByException.set(name, codes);
    // 한 핸들러가 분기마다 다른 코드를 쓴다. 코드 선언 뒤 첫 ErrorResponse 생성자의 인자 수로 판단한다.
    for (const m of seg.matchAll(/ErrorCode\s+\w+\s*=\s*ErrorCode\.([A-Z_0-9]+)\s*;([\s\S]{0,300}?)new ErrorResponse\(([^;]*?)\)/g)) {
      const args = m[3].split(",").length;
      if (args >= 2) withFieldErrors.add(m[1]);
    }
  }
}
const commonCodesOf = (exceptionName, reason) =>
  (commonByException.get(exceptionName) ?? [])
    .map((name) => codeOf(name))
    .filter(Boolean)
    .map((c) => ({
      status: c.status,
      code: c.code,
      message: c.message,
      reason,
      fieldErrors: withFieldErrorsCode.has(c.code),
      // auth: 인증 단계 · global: API 성격과 무관 · endpoint: 이 API 의 요청 모양에 달림
      scope: withFieldErrorsCode.has(c.code) ? "endpoint" : "global",
    }));

// 인증 단계 에러는 GlobalExceptionHandler 를 안 거친다. ErrorHandlingFilter 가 응답을 직접 쓰고,
// 진입점·거부 핸들러는 ErrorCode 를 직접 참조한다. 그래서 필터 계층을 따로 읽는다.
function directCodesIn(label, pattern) {
  const found = findSources(pattern);
  if (!found.length) console.warn(`${label} 를 찾지 못했습니다`);
  return found.flatMap((f) => [...f.text.matchAll(/ErrorCode\.([A-Z][A-Z0-9_]*)/g)].map((m) => m[1]));
}

// 온보딩·리프레시 토큰은 해당 경로에서만 의미가 있다
const SCOPED = { ONBOARDING_TOKEN_EXPIRED: "ONBOARDING", REFRESH_TOKEN_EXPIRED: "REFRESH" };

// 인증 단계 에러는 GlobalExceptionHandler 를 안 거친다. 진입점·거부 핸들러는 ErrorCode 를 직접 쓰고,
// 필터가 던진 예외는 ErrorHandlingFilter 가 응답으로 바꾼다. 그래서 필터 호출 그래프까지 따라간다.
const filterCodes = findSources(/extends\s+OncePerRequestFilter/)
  .flatMap((f) => exceptionsFrom(f.cls, "doFilterInternal", FILTER_DEPTH));

const authCommonNames = [
  ...directCodesIn("인증 진입점", /implements\s+AuthenticationEntryPoint/),
  ...directCodesIn("접근 거부 핸들러", /implements\s+AccessDeniedHandler/),
  ...filterCodes,
].filter((name) => !SCOPED[name]);

const onboardingCodeName = filterCodes.find((name) => SCOPED[name] === "ONBOARDING");

const TOKEN_CODES = [...new Set(authCommonNames)];

// ErrorCode 이름 → 코드 문자열로 변환해 둔다 (엔트리는 코드 문자열을 쓴다)
const withFieldErrorsCode = new Set([...withFieldErrors].map((n) => codeOf(n)?.code).filter(Boolean));

// REST 밖에서 던지는 것과 추적 실패를 구분해 둔다
function classify(where) {
  const joined = where.join(" ");
  if (/scheduler|batch|STORIX-Batch/i.test(joined)) return "배치";
  if (/stomp|websocket|chat\/Redis|handler\/Stomp/i.test(joined)) return "웹소켓";
  if (/decoder|feign/i.test(joined)) return "외부호출";
  if (/filter|interceptor|resolver|security/i.test(joined)) return "필터";
  return "확인필요";
}

// 같은 코드가 두 경로로 들어오면 먼저 붙은 설명을 남긴다 (@Valid 쪽이 더 구체적이다)
function dedupe(entries) {
  const seen = new Set();
  return entries.filter((c) => {
    const key = c.code;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}


// 오버로드된 핸들러는 클래스#메서드가 같다. 파라미터 개수까지 넣어야 구분된다.
function paramCount(signature) {
  const open = signature.indexOf("(");
  if (open < 0) return 0;
  const params = signature.slice(open + 1, signature.lastIndexOf(")"));
  if (!params.trim()) return 0;
  let depth = 0, count = 1;
  for (const ch of params) {
    if (ch === "<" || ch === "(" || ch === "[") depth++;
    else if (ch === ">" || ch === ")" || ch === "]") depth--;
    else if (ch === "," && depth === 0) count++;
  }
  return count;
}

const out = { _generatedFrom: "static analysis", endpoints: {} };
let withErrors = 0;

for (const ep of endpoints()) {
  const sec = securityOf(ep);
  // 어노테이션은 파라미터 목록에 있으므로 본문이 아니라 시그니처를 본다
  const sig = ep.signature ?? handlerSignature(ep);
  const hasBody = /@RequestBody/.test(sig);
  const hasValid = /@Valid/.test(sig);
  const isMultipart = /MultipartFile/.test(sig);

  const own = errorsOf(ep).map((e) => ({ status: statusOf(e.status), code: e.code, message: e.message }));
  const common = [];

  if (sec.rule !== "permitAll") {
    // 역할별 메시지가 더 구체적이라 먼저 넣는다 (dedupe 는 먼저 온 것을 남긴다)
    if (sec.role) {
      const f = codeOf("FORBIDDEN");
      if (f) common.push({ status: f.status, code: f.code, message: `${sec.role} 권한이 필요합니다.`, reason: `hasRole(${sec.role})`, scope: "auth" });
    }
    for (const c of TOKEN_CODES) {
      const v = codeOf(c);
      if (v) common.push({ status: v.status, code: v.code, message: v.message, reason: "인증 단계", scope: "auth" });
    }
    if (sec.role === "ONBOARDING") {
      const v = codeOf(onboardingCodeName);
      if (v) common.push({ status: v.status, code: v.code, message: v.message, reason: "온보딩 토큰", scope: "auth" });
    }
  }
  if (hasValid) {
    for (const ex of ["MethodArgumentNotValidException", "ConstraintViolationException"]) {
      common.push(...commonCodesOf(ex, "요청 검증 실패"));
    }
  }
  if (hasBody) common.push(...commonCodesOf("HttpMessageNotReadableException", "요청 JSON 형식 오류"));
  common.push(...commonCodesOf("Exception", "처리되지 않은 서버 오류").map((c) => ({ ...c, scope: "global" })));
  if (isMultipart) common.push(...commonCodesOf("MaxUploadSizeExceededException", "업로드 용량 초과").map((c) => ({ ...c, scope: "endpoint" })));
  // Exception 폴백(UNHANDLED_ERROR)은 모든 API에 똑같이 붙어서 노이즈만 된다. 서버 버그지 프론트가 분기할 것도 아니다.

  if (own.length) withErrors++;
  out.endpoints[`${ep.controller}#${ep.handler}/${paramCount(sig)}`] = {
    path: `${ep.method} ${ep.path}`,
    security: sec.rule === "permitAll" ? "public" : sec.role ? `role:${sec.role}` : "authenticated",
    own,
    common: dedupe(common),
  };
}

// 어느 엔드포인트에도 연결되지 않은 예외. 진짜 REST 밖(배치·웹소켓)인 것과 추적 실패가 섞여 있어서
// 숫자가 늘면 분석기가 못 따라가고 있다는 신호로 본다.
const mappedCodes = new Set(Object.values(out.endpoints).flatMap((v) => v.own.map((e) => e.code)));
const orphans = [];
for (const [exception, files] of thrownExceptions()) {
  const code = codeOf(codeOfException(exception));
  if (!code || mappedCodes.has(code.code)) continue;
  const where = [...files].map((f) => f.replace(ROOT + "/", ""));
  orphans.push({ exception, code: code.code, where: where.slice(0, 3), kind: classify(where) });
}
out._orphans = orphans.sort((a, b) => a.kind.localeCompare(b.kind) || a.exception.localeCompare(b.exception));

const byKind = {};
for (const o of orphans) byKind[o.kind] = (byKind[o.kind] ?? 0) + 1;
if (unknownStatuses.size) console.warn(`매핑 없는 HttpStatus: ${[...unknownStatuses].join(", ")}`);
console.log(`엔드포인트에 안 붙은 예외 ${orphans.length}개 ${JSON.stringify(byKind)}`);

// ── 웹소켓(STOMP) ────────────────────────────────────────────
// REST 와 응답 형식이 다르다. ERROR 프레임의 message 문자열로 나가므로 code 가 없다.
const stompFrames = [];
for (const src of findSources(/new MessageDeliveryException\(/)) {
  for (const m of src.text.matchAll(/(\w+)?[\s\S]{0,400}?new MessageDeliveryException\("([^"]+)"\)/g)) {
    stompFrames.push({ message: m[2], source: src.cls });
  }
}
const seenFrames = new Set();
out._websocket = {
  frames: stompFrames.filter((f) => {
    if (seenFrames.has(f.message)) return false;
    seenFrames.add(f.message);
    return true;
  }),
  // 발행 실패처럼 REST 응답으로 나가지 않는 에러코드
  codes: orphans.filter((o) => o.kind === "웹소켓").map((o) => {
    const c = codeOf(codeOfException(o.exception));
    return { status: c.status, code: c.code, message: c.message };
  }),
};
console.log(`웹소켓 ERROR 프레임 ${out._websocket.frames.length}개 · 코드 ${out._websocket.codes.length}개`);

const dest = process.argv[2];
mkdirSync(path.dirname(dest), { recursive: true });
writeFileSync(dest, JSON.stringify(out, null, 2), "utf8");
console.log(`엔드포인트 ${Object.keys(out.endpoints).length}개 · 고유 에러 있는 것 ${withErrors}개 → ${dest}`);
