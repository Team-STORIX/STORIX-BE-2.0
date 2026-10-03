#!/bin/sh
# 부팅 때 Parameter Store 의 현재 값으로 .env 를 다시 만들고, 설정이 바뀐 컨테이너만 다시 띄운다.
# 환경과 이미지 태그는 마지막 배포가 남긴 deploy.state 에서 읽는다.
# Parameter Store 를 못 읽으면 .env 를 건드리지 않고 끝나서 컨테이너는 예전 값으로 떠 있다.
set -eu
cd /home/ubuntu/storix

if [ ! -f deploy.state ]; then
  echo "deploy.state 가 없어 건너뛴다"
  exit 0
fi
. ./deploy.state

# 배포와 겹치면 기다린다
exec 9>.deploy.lock
flock 9

python3 render-env.py "$DEPLOY_ENV"
IMAGE_TAG="$IMAGE_TAG" docker compose up -d
