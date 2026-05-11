# TimeStamptKt 아키텍처 문서

## 개요

TimeStamptKt는 출근/업무 시간 기록을 빠르게 남기고 관리하기 위한 Android Kotlin 앱입니다.
현재 구현은 Jetpack Compose 기반 UI, MVVM, Hilt, Room, DataStore, Paging을 사용합니다.

이 문서는 프로젝트의 현재 구조와 앞으로 기능을 확장할 때 지켜야 할 기준을 정리합니다.

## 현재 구현 상태

### 구현됨

- 메인 화면에서 현재 시간 표시
- 타임스탬프 기록 추가
- 기록 목록 표시
- 기록 메모 수정
- 개별 기록 삭제
- 전체 기록 삭제
- JSON 백업 파일 저장
- JSON 백업 파일 불러오기
- 연차/휴가 기록 추가, 수정, 삭제
- 연도별 지급/사용/잔여 연차 계산
- `일자 기간 사유` 형식 연차 기록 붙여넣기 가져오기
- 사유 미입력 시 `개인사유` 자동 저장
- 시작 날짜와 총 사용 일수 기반 연차 자동 분할 등록
- 반차가 섞인 기간은 첫날/마지막날 반차 위치 선택
- 캘린더에서 출근 기록과 연차 기록 표시
- 앱 시작 시 하루 1회 자동 기록 옵션
- 하단 내비게이션 기본 구조
- 설정 화면

### 정리 필요

- PowerShell 기본 출력에서는 한글이 깨져 보일 수 있으므로 UTF-8로 확인합니다.
- 기존 설치 앱의 데이터 유지를 위해 `applicationId`는 `com.androidkotlin.timestampkt.data`를 유지합니다.

## 아키텍처 원칙

1. **Clean Architecture 유지**
   - `domain`은 Android 프레임워크와 DB 구현에 의존하지 않습니다.
   - `data`는 Room/DataStore 등 실제 저장소 구현을 담당합니다.
   - `presentation`은 Compose UI와 ViewModel을 담당합니다.

2. **MVVM 유지**
   - Compose 화면은 상태를 표시하고 사용자 이벤트를 ViewModel에 전달합니다.
   - ViewModel은 UseCase를 호출하고 UI에 필요한 Flow/State를 제공합니다.

3. **기능 단위 확장**
   - 캘린더, 연차, 통계, 내보내기 같은 기능은 presentation/domain/data에 같은 기능 축으로 추가합니다.
   - UI에서 바로 DAO나 DataStore를 호출하지 않습니다.

4. **로컬 우선**
   - 현재 앱의 핵심 데이터는 Room과 DataStore에 저장합니다.
   - 백업/복원 또는 내보내기는 별도 기능으로 분리합니다.

## 레이어 구조

### Presentation Layer

사용자 화면과 UI 상태를 담당합니다.

- `presentation/main`
  - 현재 시간 표시
  - 기록 목록
  - 기록 추가/수정/삭제
- `presentation/navigation`
  - 하단 탭 내비게이션
- `presentation/settings`
  - 자동 기록 설정
  - 백업 파일 저장/불러오기
  - 전체 데이터 삭제
- `presentation/leave`
  - 연차 요약
  - 연차 기록 목록
  - 연차 기록 추가/수정/삭제
  - 엑셀 기록 붙여넣기 가져오기
- `presentation/calendar`
  - 월별 달력
  - 출근 기록 표시
  - 연차 기록 표시

앞으로 추가할 후보:

- `presentation/calendar`
- `presentation/leave`
- `presentation/statistics`
- `presentation/export`

### Domain Layer

앱의 핵심 모델과 비즈니스 규칙을 담당합니다.

- `domain/model/TimeRecord.kt`
- `domain/repository/TimeRecordRepository.kt`
- `domain/usecase/*TimeRecord*UseCase.kt`

앞으로 추가할 후보:

- 날짜별 기록 조회 UseCase
- 월별 근무 시간 계산 UseCase
- 연차 사용/잔여 계산 UseCase
- CSV/Excel 내보내기 UseCase
- JSON 백업/복원 UseCase
- 연차 기록 모델
- 연도별 지급 연차 모델

### Data Layer

실제 데이터 저장과 변환을 담당합니다.

- Room
  - `TimeRecordDatabase`
  - `TimeRecordDao`
  - `TimeRecordEntity`
- DataStore
  - `SettingsManager`
- Repository 구현
  - `TimeRecordRepositoryImpl`

앞으로 추가할 후보:

- 월/기간별 쿼리
- 검색 쿼리
- 백업/복원 데이터 소스
- 연차/휴가 Entity와 DAO

현재 추가된 연차 관련 저장소:

- `LeaveRecordEntity`
- `LeaveYearSettingEntity`
- `LeaveDao`
- `LeaveRepository`

### DI Layer

Hilt를 통해 의존성을 제공합니다.

- `di/module/AppModule.kt`

새 Repository, DAO, Manager, UseCase가 추가되면 이곳에 제공 함수를 추가합니다.

## 주요 데이터 흐름

### 기록 추가

1. 사용자가 메인 화면에서 기록 추가 버튼을 누름
2. `MainViewModel.addRecord()` 호출
3. `AddTimeRecordUseCase` 실행
4. `TimeRecordRepository` 호출
5. `TimeRecordRepositoryImpl`이 `TimeRecordDao.insert()` 실행
6. Room Flow/PagingSource 갱신
7. 화면 목록이 자동 갱신

### 자동 기록

1. 앱 시작 시 `MainScreen`에서 `SettingsManager.shouldAddAutoRecord()` 확인
2. 자동 기록이 켜져 있고 오늘 아직 기록하지 않았으면 기록 추가
3. `SettingsManager.updateLastAutoRecordDate()`로 마지막 자동 기록 날짜 저장

### 백업 저장

1. 설정 화면에서 백업 파일 저장 버튼을 누름
2. `MainViewModel.createBackupJson()` 호출
3. `ExportTimeRecordsBackupUseCase`가 전체 기록을 JSON 문자열로 변환
4. Android 문서 저장 창에서 사용자가 저장 위치를 선택
5. 선택한 파일에 JSON 백업을 저장

### 백업 불러오기

1. 설정 화면에서 백업 파일 불러오기 버튼을 누름
2. Android 문서 선택 창에서 JSON 백업 파일 선택
3. `MainViewModel.restoreBackup()` 호출
4. `ImportTimeRecordsBackupUseCase`가 JSON을 파싱
5. 기존 출근 기록을 백업 파일 내용으로 교체

백업 파일에는 출근 기록뿐 아니라 연차 기록과 연도별 지급 연차 설정도 함께 포함됩니다.

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM
- Kotlin Coroutines / Flow
- Hilt
- Room
- Paging 3
- DataStore Preferences

## 다음 개발 기준

- 새 기능은 먼저 `ROADMAP.md`에 작업 단위로 적습니다.
- 기능 구현 전 패키지 위치를 `PackageStructure.md`에 반영합니다.
- 기능 확장 전에 기존 화면 동작을 한 번 빌드로 확인합니다.
