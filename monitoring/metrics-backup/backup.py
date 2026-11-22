"""Prometheus 지표를 하루 단위로 S3 에 쌓는다. Prometheus 는 15일만 보관해서 그 전 지표가 사라지기 때문이다.

매일 04:30 KST 에 전날(KST 0시 ~ 24시) 지표를 1분 간격으로 꺼낸다. 켜질 때는 최근 13일 중 빠진 날을 채운다.
버킷은 계정 번호로 정한다: storix-log-archive-<계정>. 경로는 metrics/prometheus/daily/YYYY/MM/DD/
파일은 지표 이름마다 <이름>.jsonl.gz. 한 줄은 시계열 하나의 1시간 구간이다:
  {"metric": {...라벨}, "values": [[초, "값"], ...]}
같은 시계열이 시간마다 여러 줄로 나오니 읽을 때 라벨로 합친다. manifest.json 이 있으면 그날은 끝난 것이다.

  PROM_URL      Prometheus 주소. 기본 http://prometheus:9090
  LOCAL_DIR     있으면 S3 대신 이 폴더에 쓴다 (로컬 시험용)
  RUN_ONCE      있으면 빠진 날만 채우고 끝낸다
"""
import gzip, io, json, os, sys, time, urllib.parse, urllib.request
from datetime import datetime, timedelta, timezone

KST = timezone(timedelta(hours=9))
PROM = os.environ.get("PROM_URL", "http://prometheus:9090")
LOCAL_DIR = os.environ.get("LOCAL_DIR")
STEP = 60
CHUNK = 3600
BACKFILL_DAYS = 13
RUN_AT = (4, 30)
# 조회 사이 쉬는 시간. 빠진 날을 몰아 채울 때 Prometheus(메모리 512MB)가 버거워 운영 지표 수집이 끊기지 않게 한다
PAUSE_SECONDS = 0.2


def log(msg):
    print(f"[metrics-backup] {datetime.now(KST):%m-%d %H:%M:%S} {msg}", flush=True)


def prom(path, params):
    query = urllib.parse.urlencode(params)
    for attempt in range(5):
        try:
            with urllib.request.urlopen(f"{PROM}{path}?{query}", timeout=120) as r:
                body = json.load(r)
            time.sleep(PAUSE_SECONDS)
            if body.get("status") == "success":
                return body["data"]
            raise RuntimeError(body.get("error"))
        except Exception:
            if attempt == 4:
                raise
            time.sleep(5 * (attempt + 1))


class Store:
    def __init__(self):
        if LOCAL_DIR:
            self.s3 = None
            return
        import boto3
        self.s3 = boto3.client("s3")
        account = boto3.client("sts").get_caller_identity()["Account"]
        self.bucket = f"storix-log-archive-{account}"

    def exists(self, key):
        if not self.s3:
            return os.path.exists(os.path.join(LOCAL_DIR, key))
        try:
            self.s3.head_object(Bucket=self.bucket, Key=key)
            return True
        except self.s3.exceptions.ClientError as e:
            if e.response["Error"]["Code"] in ("404", "NoSuchKey", "NotFound"):
                return False
            raise

    def put(self, key, data, content_type):
        if not self.s3:
            path = os.path.join(LOCAL_DIR, key)
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, "wb") as f:
                f.write(data)
            return
        self.s3.put_object(Bucket=self.bucket, Key=key, Body=data, ContentType=content_type)


def backup_day(store, day):
    prefix = f"metrics/prometheus/daily/{day:%Y/%m/%d}/"
    if store.exists(prefix + "manifest.json"):
        return False
    start = int(datetime(day.year, day.month, day.day, tzinfo=KST).timestamp())
    end = start + 86400
    names = prom("/api/v1/label/__name__/values", {"start": start, "end": end})
    series = points = 0
    for name in names:
        buf = io.BytesIO()
        with gzip.GzipFile(fileobj=buf, mode="wb") as gz:
            for t in range(start, end, CHUNK):
                data = prom("/api/v1/query_range",
                            {"query": f'{{__name__="{name}"}}', "start": t, "end": t + CHUNK - STEP, "step": STEP})
                for s in data["result"]:
                    gz.write((json.dumps({"metric": s["metric"], "values": s["values"]}) + "\n").encode())
                    series += 1
                    points += len(s["values"])
        store.put(prefix + f"{name}.jsonl.gz", buf.getvalue(), "application/gzip")
    manifest = {"day_kst": f"{day:%Y-%m-%d}", "start": start, "end": end, "step_seconds": STEP,
                "metrics": len(names), "series_chunks": series, "points": points,
                "line": "series for one hour: {metric, values:[[ts, value]]}. merge lines by labels",
                "exported_at": int(time.time())}
    store.put(prefix + "manifest.json", json.dumps(manifest, indent=2).encode(), "application/json")
    log(f"{day:%Y-%m-%d} 올림 · 지표 {len(names)} · 점 {points}")
    return True


def fill_missing(store):
    today = datetime.now(KST).date()
    for back in range(BACKFILL_DAYS, 0, -1):
        try:
            backup_day(store, today - timedelta(days=back))
        except Exception as e:
            log(f"{today - timedelta(days=back)} 실패: {e}")


def seconds_until_next_run():
    now = datetime.now(KST)
    run = now.replace(hour=RUN_AT[0], minute=RUN_AT[1], second=0, microsecond=0)
    if run <= now:
        run += timedelta(days=1)
    return (run - now).total_seconds()


def main():
    store = Store()
    log(f"시작 · 저장 {'로컬 ' + LOCAL_DIR if LOCAL_DIR else 'S3 ' + store.bucket}")
    fill_missing(store)
    if os.environ.get("RUN_ONCE"):
        return
    while True:
        time.sleep(seconds_until_next_run())
        fill_missing(store)


if __name__ == "__main__":
    sys.exit(main())
