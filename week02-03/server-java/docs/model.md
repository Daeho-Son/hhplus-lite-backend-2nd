# 필요한 정보
## 사용자(user)
- 사용자 식별번호(user_id): PK
- 이메일(email)
- 생성일(created_at)
- 수정일(updated_at)

## 콘서트(concert)
- 콘서트 식별번호(concert_id): PK
- 콘서트 이름(name)
- 생성일(created_at)
- 수정일(updated_at)

## 콘서트 날짜(concert_date)
- 콘서트 날짜 식별번호(concert_date_id): PK
- 콘서트 식별번호(concert_id): FK
- 상영일(show_at)
- 생성일(created_at)
- 수정일(updated_at)

## 예약(reservation)
- 예약 식별번호(reservation_id): PK
- 콘서트 식별번호(concert_id): FK
- 콘서트 날짜 식별번호(concert_date_id): FK
- 사용자 식별번호(user_id): FK
- 멱등키(idempotency_key): UNIQUE
- 예약석 구간(seat_section): (ex. A~Z)
- 예약석 번호(seat_no): (ex. 1~100)
- 생성일(created_at)
- 수정일(updated_at)

## 지갑(wallet)
- 지갑 식별번호(wallet_id): PK
- 사용자 식별번호(user_id): FK
- 잔액(balance)ㅇ
- 생성일(created_at)
- 수정일(updated_at)

## 결제(payment)
- 결제 식별번호(payment_id): PK
- 사용자 식별번호(user_id): FK
- 예약 식별번호(reservation_id): FK
- 멱등키(idempotency_key): UNIQUE
- 금액(amount)
- 결제 상태 (payment_status): []
- 생성일(created_at)
- 수정일(updated_at)