#!/usr/bin/env python3
"""Parameter Store 의 값으로 서버의 .env 를 만든다. 배포가 서버에서 실행한다.

  python3 render-env.py <dev|prod>           # .env 를 새로 쓴다. 이전 파일은 .env.prev
  python3 render-env.py <dev|prod> --check   # 쓰지 않고 지금 .env 와 같은지만 본다

값은 화면에 찍지 않는다. 필요한 키 이름은 .env.example 에서 읽는다.
"""
import json, os, subprocess, sys, urllib.request

REGION = "ap-northeast-2"
HERE = os.path.dirname(os.path.abspath(__file__))
ENV_FILE = os.path.join(HERE, ".env")
EXAMPLE = os.path.join(HERE, ".env.example")
# 서버마다 다른 값이라 Parameter Store 에 두지 않고 서버가 자기 값으로 채운다.
LOCAL_KEYS = {"INSTANCE_ID"}


def instance_id():
    token = urllib.request.urlopen(urllib.request.Request(
        "http://169.254.169.254/latest/api/token", method="PUT",
        headers={"X-aws-ec2-metadata-token-ttl-seconds": "60"}), timeout=3).read().decode()
    return urllib.request.urlopen(urllib.request.Request(
        "http://169.254.169.254/latest/meta-data/instance-id",
        headers={"X-aws-ec2-metadata-token": token}), timeout=3).read().decode()


def fetch(env, keys):
    values, missing = {}, []
    names = [f"/storix/{env}/env/{k}" for k in keys]
    # GetParameters 는 한 번에 10개까지 받는다
    for i in range(0, len(names), 10):
        out = subprocess.run(
            ["aws", "ssm", "get-parameters", "--region", REGION, "--with-decryption",
             "--names", *names[i:i + 10], "--output", "json"],
            capture_output=True, text=True)
        if out.returncode != 0:
            sys.exit("Parameter Store 를 읽지 못했다: " + (out.stderr.strip().splitlines() or ["?"])[-1][:160])
        data = json.loads(out.stdout)
        for p in data["Parameters"]:
            values[p["Name"].rsplit("/", 1)[1]] = p["Value"]
        missing += [n.rsplit("/", 1)[1] for n in data.get("InvalidParameters", [])]
    return values, missing


def read_env(path):
    result = {}
    for raw in open(path, encoding="utf-8"):
        line = raw.rstrip("\n")
        if not line.strip() or line.lstrip().startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        result[key.strip()] = value
    return result


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    check = "--check" in sys.argv
    if len(args) != 1 or args[0] not in ("dev", "prod"):
        sys.exit("사용법: render-env.py <dev|prod> [--check]")
    env = args[0]

    keys = list(read_env(EXAMPLE))
    values, missing = fetch(env, [k for k in keys if k not in LOCAL_KEYS])
    if missing:
        sys.exit("Parameter Store 에 없는 값: " + ", ".join(sorted(missing)))
    if values.get("ENV") != env:
        sys.exit(f"/storix/{env}/env/ENV 의 값이 {env} 가 아니다. 경로가 섞였다.")
    values["INSTANCE_ID"] = instance_id()

    if check:
        current = read_env(ENV_FILE)
        differ = sorted(k for k in keys if k in current and current[k] != values[k])
        only_new = sorted(k for k in keys if k not in current)
        only_old = sorted(k for k in current if k not in values)
        print(f"키 {len(keys)}개 · 값이 다른 것 {differ or '없음'} · 새로 생기는 것 {only_new or '없음'} · 사라지는 것 {only_old or '없음'}")
        sys.exit(1 if differ or only_old else 0)

    tmp = ENV_FILE + ".new"
    fd = os.open(tmp, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, "w", encoding="utf-8") as f:
        for key in keys:
            f.write(f"{key}={values[key]}\n")
    if os.path.exists(ENV_FILE):
        os.replace(ENV_FILE, ENV_FILE + ".prev")
    os.replace(tmp, ENV_FILE)
    print(f".env 를 썼다 · 키 {len(keys)}개 · 환경 {env}")


main()
