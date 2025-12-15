# 송금 서비스 구현
## 프로젝트 실행
### Dependency
- `Docker Compose 1.29+`
- `JDK 17`

## 프로젝트 개요
### 데이터 모델링
- 계좌(Account)를 상위 자원으로 거래(Transaction)를 하위 자원으로 계층화
### 관심사
- account 모듈: 계좌 관리
  - 생성
  - 삭제
  - 조회
- transaction 모듈: 거래 관리 
  - 입금
  - 출금
  - 송금

```text
com.example.banktransfer
├── account
│   ├── Account.java (엔티티)
│   ├── AccountService.java (CRUD)
│   └── AccountRepository.java
│
└── transaction
    ├── Transaction.java (엔티티)
    ├── TransactionService.java (입금/출금/이체)
    └── TransactionRepository.java
```