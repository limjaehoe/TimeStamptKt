# 패키지 구조

```text
com.androidkotlin.timestampkt/
├── _docs/                         # 프로젝트 문서
│   ├── ARCHITECTURE.md             # 아키텍처와 개발 원칙
│   ├── PackageStructure.md         # 패키지 구조
│   └── ROADMAP.md                  # 남은 기능과 작업 순서
│
├── TimeStampKtApplication.kt       # Hilt Application 클래스
│
├── data/                           # 데이터 레이어
│   ├── local/                      # Room 로컬 DB
│   │   ├── dao/
│   │   │   └── TimeRecordDao.kt    # 기록 조회/추가/수정/삭제 DAO
│   │   ├── database/
│   │   │   └── TimeRecordDatabase.kt
│   │   └── entity/
│   │       └── TimeRecordEntity.kt
│   │
│   ├── preferences/
│   │   └── SettingsManager.kt      # DataStore 기반 설정 관리
│   │
│   └── repository/
│       └── TimeRecordRepositoryImpl.kt
│
├── di/                             # 의존성 주입
│   └── module/
│       └── AppModule.kt
│
├── domain/                         # 도메인 레이어
│   ├── model/
│   │   └── TimeRecord.kt
│   │
│   ├── repository/
│   │   └── TimeRecordRepository.kt
│   │
│   └── usecase/
│       ├── AddTimeRecordUseCase.kt
│       ├── DeleteAllTimeRecordsUseCase.kt
│       ├── DeleteTimeRecordUseCase.kt
│       ├── ExportTimeRecordsBackupUseCase.kt
│       ├── GetPagedTimeRecordsUseCase.kt
│       ├── GetTimeRecordsUseCase.kt
│       ├── ImportTimeRecordsBackupUseCase.kt
│       └── UpdateTimeRecordUseCase.kt
│
├── presentation/                   # Compose UI와 ViewModel
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── MainScreen.kt
│   │   └── MainViewModel.kt
│   │
│   ├── navigation/
│   │   └── TimeStampNavigation.kt
│   │
│   └── settings/
│       └── SettingsScreen.kt
│
└── ui/
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## 현재 패키지별 역할

### `_docs`

코드와 함께 관리하는 프로젝트 문서 폴더입니다.
기능 추가 계획, 구조 설명, 진행 상황을 이곳에서 관리합니다.

### `data`

Room DB, DataStore, Repository 구현처럼 실제 저장소와 외부 구현 세부사항을 담당합니다.

### `domain`

앱의 핵심 모델과 UseCase를 둡니다.
Android UI, Room, DataStore에 직접 의존하지 않는 방향을 유지합니다.

### `presentation`

Compose 화면, ViewModel, Navigation을 둡니다.
화면에서 직접 DB를 호출하지 않고 ViewModel과 UseCase를 통해 처리합니다.

### `di`

Hilt 모듈을 둡니다.
새 DAO, Repository, UseCase, Manager를 추가하면 이곳에서 주입 구성을 정리합니다.

### `ui`

앱 테마, 색상, Typography 등 공통 UI 테마를 관리합니다.

## 다음 패키지 후보

```text
presentation/calendar/              # 월별/날짜별 기록 화면
presentation/leave/                 # 연차/휴가 관리 화면
presentation/statistics/            # 근무 시간 통계 화면

domain/model/LeaveRecord.kt         # 연차/휴가 모델
domain/usecase/calendar/            # 날짜별 기록 조회
domain/usecase/leave/               # 연차 계산/관리

data/local/entity/LeaveRecordEntity.kt
data/local/dao/LeaveRecordDao.kt
data/repository/LeaveRepositoryImpl.kt
```

## 나중에 정리하면 좋은 항목

- 화면별 Preview 추가
- 테스트 패키지 구조 추가
- 캘린더/연차 기능 패키지 추가
