package com.androidkotlin.timestampkt.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.androidkotlin.timestampkt.presentation.settings.SettingsScreen
import com.androidkotlin.timestampkt.ui.theme.TimeStampKtTheme
import dagger.hilt.android.AndroidEntryPoint
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import com.androidkotlin.timestampkt.presentation.navigation.TimeStampNavigation
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeStampKtTheme {
                //TimeStampApp(viewModel, settingsManager)

                // 기존 TimeStampApp 대신 TimeStampNavigation 사용
                TimeStampNavigation(viewModel, settingsManager)

            }
        }
    }
}

/**
 * TimeStampKt 앱 향후 개발 로드맵
 *
 * 1. 캘린더 기능 (Calendar Screen)
 *    1) 기본 월간/주간 캘린더 뷰 구현
 *    2) 출근 기록 시각화 - 날짜에 출근 시간 표시
 *    3) 퇴근 시간 기록 기능 추가 및 캘린더에 표시
 *    4) 공휴일 및 특별일 표시 (공공데이터 API 활용)
 *    5) 이벤트 카테고리별 색상 코드화:
 *       - 출근일: 기본색
 *       - 휴가/연차: 녹색
 *       - 공휴일: 빨간색
 *       - 회사 특별일(창립기념일 등): 파란색
 *
 * 2. 연차관리 기능 (Leave Screen)
 *    1) 입사일 기준 연차 계산 시스템:
 *       - 1년 미만: 한 달 만근 시 1일 발생
 *       - 1년 이상: 15일 기본 부여 + 2년마다 1일 추가 (최대 25일)
 *       - 반차 사용 기능 (0.5일 차감)
 *    2) 연차 신청 및 관리 인터페이스:
 *       - 남은 연차 일수 표시
 *       - 사용한 연차 기록 (날짜, 사유 등)
 *       - 연차 신청 기능 (기간, 사유 입력)
 *    3) 캘린더와 연동하여 사용한 연차일 표시
 *    4) 연차 만료일 알림 기능 (12월 말 등)
 *
 * 3. 알림 및 리마인더 시스템
 *    1) 출퇴근 시간 알림:
 *       - 설정된 퇴근 시간 사전 알림 (5분, 10분, 30분 전)
 *       - 출근 시간 사전 알림 옵션
 *    2) 푸시 알림 구현 (FCM 활용):
 *       - 알림 채널 및 우선순위 설정
 *       - 백그라운드/포그라운드 알림 처리
 *    3) 출퇴근 기록 외부 연동:
 *       - 이메일 보고 옵션 (일간/주간 근무 요약)
 *       - 캘린더 앱과 연동 (Google 캘린더 등)
 *    4) 데스크톱 알림 연동 검토 (웹 서비스 확장 시)
 *
 * 4. 메인 화면 개선 (Home Screen)
 *    1) 출퇴근 기록 UI/UX 개선:
 *       - 오늘 날짜 기록 중심 표시 (확장형 카드)
 *       - 과거 기록은 축소하여 표시하고 탭하면 상세 정보 표시
 *    2) 지능형 자동 기록:
 *       - 같은 날짜에 중복 자동 기록 방지
 *       - 출근/퇴근 시간 자동 인식 로직 추가
 *    3) 통계 요약 섹션 추가:
 *       - 이번 주/월 근무 시간 합계
 *       - 평균 출퇴근 시간 그래프
 *    4) 다크 모드 개선 및 테마 커스터마이징
 *
 * 5. 사용자 인증 및 데이터 백업
 *    1) Firebase 인증 통합:
 *       - Google 로그인 지원
 *       - 사용자 프로필 관리
 *    2) 클라우드 동기화:
 *       - Firestore를 활용한 데이터 백업
 *       - 다중 기기 동기화 지원
 *    3) 데이터 내보내기/가져오기:
 *       - CSV/Excel 형식 지원
 *       - 백업 및 복원 기능
 *
 * 6. 성능 및 안정성 개선
 *    1) 데이터베이스 최적화:
 *       - 인덱싱 개선
 *       - 대용량 데이터 처리 전략
 *    2) 배터리 소모 최적화
 *    3) 단위/통합 테스트 추가
 *    4) 에러 로깅 및 크래시 리포팅 통합
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeStampApp(
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> {
            MainScreen(
                viewModel = viewModel,
                settingsManager = settingsManager,
                onNavigateToSettings = { currentScreen = "settings" }
            )
        }
        "settings" -> {
            SettingsScreen(
                viewModel = viewModel,
                settingsManager = settingsManager,
                onBackClick = { currentScreen = "main" }
            )
        }
    }
}