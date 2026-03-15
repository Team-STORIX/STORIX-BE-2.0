<h1>✨ STORIX 1.0 </h1>

신촌 IT 창업 동아리인 CEOS 22기 Team-STORIX에서 <br>
2025.10. ~ 2026.01. 3개월의 기간동안 개발한 웹 애플리케이션입니다.

베타 서비스 출시 3일 만에 1천명이 넘는 유저 분들께서 찾아주셨으며,
🏆CEOS 22기 DEMODAY 최우수상, 베스트 프로덕트상🏆을 받았습니다.

<br>

## 🌐 서버 인프라 1.0
Self-hosted Runner를 사용하여 EC2 인스턴스 내부에서 CI/CD를 진행합니다.

<img width="5760" height="3240" alt="백엔드 (스프링) 기술 스택" src="https://github.com/user-attachments/assets/4011340b-184e-4b87-bd59-e34a58c4dded" />

## 📂 프로젝트 구조 1.0
DDD 계층 구조와 단일 모듈 방식으로 설계하였습니다.
도메인 별로 생성주기가 비슷한 객체끼리만 묶어서 연관관계를 맺고 다른 도메인과의 결합도를 낮추었습니다.
또한 기존 MVC 구조에 Facade 패턴과 헥사고날 아키텍처를 적용하여,
도메인 중심의 아키텍처를 구축하고 계층 간 책임과 의존성을 명확히 분리하였습니다.
```
STORIX-BE/
  │
  ├── domains/
  │   ├── domain/
  │   │   ├── adaptor/
  │   │   ├── application/port, usecase/
  │   │   ├── controller/
  │   │   ├── domain/
  │   │   ├── dto/
  │   │   ├── repository/
  │   │   └── service/
  │   │
  └── global/
```

## 🛠️ Developers
<table>
    <tr align="center">
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
    </tr>
    <tr align="center">
        <td><B>서가영</B></td>
        <td><B>이수아</B></td>
    </tr>
    <tr align="center">
        <td>
            <img src="https://github.com/caminobelllo.png?size=100">
            <br>
            <a href="https://github.com/caminobelllo"><I>caminobello</I></a>
        </td>
        <td>
            <img src="https://github.com/Immmii.png?size=100" width="100">
            <br>
            <a href="https://github.com/Immmii"><I>Immmii</I></a>
        </td>
    <tr align="center">
        <td><B>배포·토픽룸·취향분석<br>·작품검색·크롤링</B></td>
        <td><B>인증/인가·온보딩·피드<br>·리뷰·서재·프로필</B></td>
    </tr>
</table>
