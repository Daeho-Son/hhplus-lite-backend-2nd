# 인가/인증 토큰 발급
## 설명
- 로그인 및 API 요청을 위한 토큰을 발급받는다.
- 특정 API를 제외한 모든 API 요청할 때는 Authorization 헤더에 Bearer 타입으로 토큰을 담아서 요청해야한다.
- 토큰을 포함하지 않아도 되는 API는 아래와 같다.
  - 로그인
  - 회원가입
  - 대기열 상태 폴링
  - 헬스체크
- 인증/인가 토큰은 jwt로 구성되어 있다.

## Request
```text
POST /auth/token
```

## Response
```text
200 OK
Content-Type: application/json

{
    accessToken: string(jwt),
}
```

---
# 대기열 토큰 발급
## 설명
- 트래픽이 많이 몰릴 수 있는 API에 대기열 기능을 적용하기 위한 토큰이다.
- API 요청할 때, X-Queue-Token 헤더에 포함해서 요청한다. 

## Request
```text
POST /queue/token
Authorization: Bearer {accessToken}
```

## Response
```text
Content-Type: application/json

{
    queueToken: string
}
```

---
# 예약 가능 날짜
## 설명
- 특정 콘서트의 예약 가능한 날짜를 조회한다.
- 한 달 이내 기준으로 조회하며, 한 달 이후의 날짜는 조회되지 않는다.
- 대기열 토큰에 대기열 정보가 업데이트된다.

## Request
```text
GET /reservation/date
Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}
```

## Response
```text
Content-Type: application/json

{
    concertId: string,
    dates: [
        datetime,
        ...
    ]
}
```

---
# 예약 가능 좌석
## 설명
- 특정 콘서트에서 선택된 날짜의 예약 가능한 좌석을 조회한다.
- 예약이 불가능한 날짜는 반환하지 않는다.

## Request
```text
GET /reservation/seat
Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}

{
    concertId: string,
    date: string,
}
```

## Response
```text
Content-Type: application/json

{
    availableSeats: [
        {
            seatId: string,
            price: number,
            seatType: string,
            seatRow: string,
            seatColumn: string,
        },
        ...
    ]
}
```

---
# 좌석 예약 요청
## 설명
- 특정 콘서트에서 선택된 날짜의 좌석을 예약한다.
- 예약 시에는 5분간 임시로 배정되며, 이 시간동안에는 다른 사람이 예약할 수 없다.
- 5분이 지나도 예약이 되지 않으면 임시 배정이 해제되며, 다른 사람도 예약할 수 있다.

## Request
```text
POST /reservation
Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}

{
    concertId: string,
    date: string,
    seatId: string
}
```

## Response
```text
Content-Type: application/json

{
    reservationId: string,
    concertId: string,
}
```
---
# 결제
## 설명
- 예약한 날짜와 좌석으로 콘서트 티켓을 결제한다.
- 결제 이후에는 임시 배정이 해제되며, 대기열 토큰이 만료된다.

## Request
```text
POST /payment
Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}
```

## Response
```text
{
    paymentId: string,
    reservationId: string,
    concertId: string,
    concertDateId: string,
    seat: {
        section: string, // 예: "A"
        number: number // 예: 12
    },
    amount: number,
    paymentStatus: "CAPTURED",
    paidAt: datetime,
    queueTokenExpired: true
}
```

---
# 잔액 충전
## Request
```text

Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}
```

## Response
```text

```

---
# 잔액 조회
## Request
```text

Authorization: Bearer {accessToken}
X-Queue-Token: {queueToken}
```

## Response
```text

```