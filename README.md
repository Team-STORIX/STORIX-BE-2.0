<h1>
<img src="https://github.com/user-attachments/assets/b65bf457-6754-4e0d-9d94-4511721b2b36" align=left width=100>
STORIX-BE
</h1>

> 이야기에 대한 이야기로 모두가 연결되는 이 곳! 데이터로 취향을 연결하는 웹툰·웹소설 팬덤 플랫폼, STORIX

<br>

[![App Store](https://img.shields.io/badge/App_Store-스토릭스-0D96F6?logo=appstore&logoColor=white)](https://apps.apple.com/kr/app/%EC%8A%A4%ED%86%A0%EB%A6%AD%EC%8A%A4-storix/id6762126035)
[![Google Play](https://img.shields.io/badge/Google_Play-스토릭스-414141?logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=kr.storix.android)

<h1><img src="https://github.com/user-attachments/assets/77539278-b8fc-40e2-85ac-f50340e5c417" width="25"/> STORIX 2.0</h1>

![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.7-6DB33F)

## 🌐 서버 인프라 2.0

## 📂 프로젝트 구조 2.0
```
STORIX-BE/
  │
  ├── STORIX-Common/                  # 공통 응답 · 에러 코드 · 예외 · 어노테이션(@UseCase 등) · Redis 키
  │
  ├── STORIX-Domain/                  # 도메인 모듈
  │   └── com/storix/domain/domains/<도메인>/
  │       ├── domain/                 # 엔티티
  │       ├── repository/             # JPA · QueryDSL
  │       ├── adaptor/                # Repository · Redis · ES 를 감싸는 조회 · 저장 창구
  │       ├── service/                # 트랜잭션 단위 비즈니스 로직
  │       └── dto/ · exception/
  │   └── resources/elasticsearch/    # ES 인덱스 매핑 (works · feeds)
  │
  ├── STORIX-Infrastructure/          # 인프라 모듈
  │   └── com/storix/infrastructure/
  │       ├── config/                 # Security · Redis · WebSocket · JPA · QueryDSL · Firebase · Feign 설정
  │       ├── external/               # STOMP 채팅 · FCM 알림 · 소셜 로그인 · 포트원 · Slack
  │       ├── global/                 # JWT · 인증 필터 · Rate Limit
  │       └── s3/                     # S3 업로드 · 삭제
  │
  ├── STORIX-Api/                     # 앱 API 모듈
  │   └── com/storix/api/
  │       ├── config/                 # Swagger (에러 응답 스펙 자동 생성)
  │       ├── domain/<도메인>/        # Controller · UseCase
  │       └── global/                 # 전역 예외 처리 · 요청 검증
  │   └── resources/db/migration/     # Flyway 마이그레이션
  │   └── Dockerfile
  │
  ├── STORIX-Internal/                # 크롤러용 내부 API (작품 적재 · 병합 · 검수, Controller · UseCase)
  │
  ├── STORIX-Batch/                   # 배치 모듈
  │   └── com/storix/batch/scheduler/ # 장르 점수 · 랭킹 · 알림 · 개인정보 파기 · 하드 삭제 등 스케줄러
  │   └── Dockerfile
  │
  ├── deploy/                         # 서버 부팅 때 Parameter Store → .env 생성
  ├── monitoring/                     # Prometheus · Loki · Grafana · nginx
  └── .github/workflows/              # PR 테스트 · CI(이미지 빌드) · CD(dev · prod) · Release
```

- 모듈 의존성
  ```
  STORIX-Common → STORIX-Domain → STORIX-Infrastructure → STORIX-Api
                                                        → STORIX-Batch
                                → STORIX-Internal ─────→ STORIX-Api
  ```
- 계층 규칙(Service 는 Adaptor 를 거침, Service → Service 금지, 트랜잭션 안 Redis · ES 호출 금지 등)은 ArchUnit 테스트가 지킨다 (`STORIX-Api/src/test/.../architecture`)

## 🚀 Trouble Shooting

## 🛠️ Developers

### 2026.03 ~ : 2.0 설계 · 개발 · 배포
<table>
    <tr>
        <td align="center" width="160"><B>Lead·Backend</B></td>
        <td rowspan="4">
            <ul>
                <li><B>2.0 기반 · 배포</B>
                    <ul>
                        <li>단일 모듈 → 멀티모듈 전환 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/2">#2</a></li>
                        <li>CI/CD · dev · prod 분리 · Parameter Store — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/3">#3</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/20">#20</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/142">#142</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/303">#303</a></li>
                        <li>Flyway 도입 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/387">#387</a></li>
                    </ul>
                </li>
                <li><B>인증</B>
                    <ul>
                        <li>Apple · 카카오 · 네이버 · X 소셜 로그인 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/40">#40</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/50">#50</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/89">#89</a></li>
                        <li>Refresh Token 다중 기기 저장 · 재발급 검증 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/170">#170</a></li>
                        <li>포트원 성인인증 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/256">#256</a></li>
                    </ul>
                </li>
                <li><B>알림</B>
                    <ul>
                        <li>이벤트 기반 FCM 푸시 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/64">#64</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/67">#67</a></li>
                        <li>관리자 알림 발송 · 재발송 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/114">#114</a></li>
                        <li>토픽룸 채팅 알림 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/213">#213</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/219">#219</a></li>
                    </ul>
                </li>
                <li><B>검색 · 작품</B>
                    <ul>
                        <li>Elasticsearch 작품 · 토픽룸 검색 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/318">#318</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/322">#322</a></li>
                        <li>크롤러 검수 · 적재 · 병합 내부 API — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/324">#324</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/341">#341</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/355">#355</a></li>
                    </ul>
                </li>
                <li><B>운영 · 관리자</B>
                    <ul>
                        <li>관리자 이벤트 · 팝업 · 배너 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/122">#122</a></li>
                        <li>보유기간 경과 데이터 파기 · 탈퇴 개인정보 파기 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/127">#127</a></li>
                        <li>앱 버전 게이팅 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/165">#165</a></li>
                    </ul>
                </li>
                <li><B>모니터링</B>
                    <ul>
                        <li>Prometheus · Loki · Grafana 스택 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/135">#135</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/136">#136</a></li>
                        <li>JSON 구조화 로깅 · API 요청 로그 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/134">#134</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/180">#180</a></li>
                    </ul>
                </li>
                <li><B>구조 · 성능</B>
                    <ul>
                        <li>계층 · 트랜잭션 컨벤션과 ArchUnit 규칙 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/339">#339</a></li>
                        <li>JWT 파서 재사용 (앱 CPU 약 40% 절감) — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/393">#393</a></li>
                        <li>인기 해시태그 Redis 캐시 · 트랜잭션 경계 정리 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/396">#396</a></li>
                    </ul>
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td align="center"><B>이수아</B></td>
    </tr>
    <tr>
        <td align="center"><img src="https://github.com/Immmii.png?size=100" width="100"></td>
    </tr>
    <tr>
        <td align="center"><a href="https://github.com/Immmii"><I>Immmii</I></a></td>
    </tr>
</table>

### 2026.05 ~ 2026.09 : 신고 · 차단 · 이벤트
<table>
    <tr>
        <td align="center" width="160"><B>Backend</B></td>
        <td rowspan="4">
            <ul>
                <li><B>신고 · 차단</B>
                    <ul>
                        <li>신고 이력 처리 · 정지 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/84">#84</a> · <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/125">#125</a></li>
                        <li>사용자 간 차단 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/94">#94</a></li>
                        <li>토픽룸 제목 · 프로필 금칙어 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/117">#117</a></li>
                    </ul>
                </li>
                <li><B>이벤트</B>
                    <ul>
                        <li>출석 이벤트 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/145">#145</a></li>
                        <li>오늘의 스토리 카드 · 출석 추첨 관리자 API — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/190">#190</a></li>
                    </ul>
                </li>
                <li><B>성인인증</B>
                    <ul>
                        <li>성인인증 도입 이후 필터링 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/266">#266</a></li>
                    </ul>
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td align="center"><B>이윤지</B></td>
    </tr>
    <tr>
        <td align="center"><img src="https://github.com/yooniicode.png?size=100" width="100"></td>
    </tr>
    <tr>
        <td align="center"><a href="https://github.com/yooniicode"><I>yooniicode</I></a></td>
    </tr>
</table>

### 2026.05 ~ 2026.07 : 추천 · 토픽룸 · 관리자
<table>
    <tr>
        <td align="center" width="160"><B>Backend</B></td>
        <td rowspan="4">
            <ul>
                <li><B>추천 · 취향</B>
                    <ul>
                        <li>해시태그 추천 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/72">#72</a></li>
                        <li>취향 탐색 리뷰한 작품 제외 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/86">#86</a></li>
                    </ul>
                </li>
                <li><B>토픽룸 · 관리자</B>
                    <ul>
                        <li>채팅 미리보기 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/79">#79</a></li>
                        <li>관리자 유저 관리 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/105">#105</a></li>
                        <li>생일 테마 — <a href="https://github.com/Team-STORIX/STORIX-BE-2.0/pull/66">#66</a></li>
                    </ul>
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td align="center"><B>변호영</B></td>
    </tr>
    <tr>
        <td align="center"><img src="https://github.com/Hoyoung027.png?size=100" width="100"></td>
    </tr>
    <tr>
        <td align="center"><a href="https://github.com/Hoyoung027"><I>Hoyoung027</I></a></td>
    </tr>
</table>

<br>
<br>

<h1>✨ STORIX 1.0 </h1>

신촌 IT 창업 동아리인 CEOS 22기 Team-STORIX에서 <br>
2025.10. ~ 2026.01. 3개월의 기간동안 개발한 웹 애플리케이션입니다.

베타 서비스 출시 3일 만에 1천명이 넘는 유저 분들께서 찾아주셨으며,      
🏆CEOS 22기 DEMODAY 최우수상, 베스트 프로덕트상🏆을 받았습니다.

<br>

1.0 의 팀원 · 기술 스택 · 프로젝트 구조 · 서버 인프라는 👉 [STORIX-BE 1.0 README](https://github.com/Team-STORIX/STORIX-BE#readme) 에 있습니다.
