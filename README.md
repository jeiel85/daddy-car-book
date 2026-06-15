# 아빠 차 차계부 — Daddy Car Book

> **4050 아빠들을 위한 쉽고 직관적인 차량 주유비 및 정비 내역 관리 가계부 앱**

복잡한 내비게이션 연동, 블루투스 OBD 연결이나 어지러운 차트 대신, 아빠들이 가장 필요로 하는 **이번 달 누적 주유비**와 **주요 소모품 교체 리마인더**를 직관적이고 깔끔하게 관리해 주는 로컬 차계부 애플리케이션입니다.

---

<p align="center">
  <img src="docs/assets/feature-graphic.png" alt="아빠 차 차계부 Key Visual" width="700">
</p>

---

## 💵 핵심 가치 & 브랜딩

- **직관성 최우선 (Simple Car Care)**: 복잡한 기능 없이, 이번 달 지출한 주유 금액과 평균 연비를 큼직하게 보여주어 즉각적인 유류비 모니터링을 돕습니다.
- **철저한 개인정보 보호 (100% Local-First)**: 주유한 주유소 위치 정보, 차량 소모품 정비 메모 등 모든 데이터는 외부 클라우드 서버로 송신되지 않고 오직 기기 내부의 보안 로컬 데이터베이스에만 안전하게 저장됩니다.
- **안전한 정비 리마인더 (Maintenance Reminder)**: 엔진오일, 브레이크 패드, 타이어 등 주요 소모품의 교체 예정 시점을 홈 화면의 경고 배너 및 시스템 푸시 알림을 통해 미리 안내하여 불필요한 고장을 예방합니다.

---

## 🛠️ 기술 스택

| 영역 | 선택 기술 |
|---|---|
| **언어 (Language)** | Kotlin |
| **UI 프레임워크** | Jetpack Compose, Material 3 |
| **아키텍처 (Architecture)** | MVVM 아키텍처 |
| **로컬 데이터베이스** | Room Database |
| **비동기 처리** | Kotlin Coroutines & Flow |
| **빌드 구성** | Gradle Kotlin DSL + 버전 카탈로그 (`libs.versions.toml`) |
| **의존성 주입** | 수동 DI (Manual DI) |

---

## 📦 저장소 구조

```text
daddy-car-book/
  app/
    src/main/
      java/com/jeiel85/daddycarbook/
        data/         # Room Database, DAO 및 Entity 데이터 레이어
        ui/
          screens/    # 홈, 주유 등록, 정비 내역 등록, 리마인더 설정, 차량 관리 화면
          theme/      # Charcoal & Turquoise 테마 디자인 시스템 (Theme.DaddyCarBook)
          viewmodel/  # 애플리케이션 상태 관리 및 알림 로직 ViewModel
      res/            # 다국어 리소스 및 드로어블 자산 (런처 아이콘 포함)
  docs/               # 개인정보처리방침 및 랜딩 페이지 자산 (GitHub Pages 호스팅)
  store-graphics/     # Google Play 스토어 등록용 그래픽 및 소개글
```

---

## 🚀 빠른 시작 (Quick Start)

### 빌드 및 실행 요구조건

- **JDK**: 17 이상
- **Android SDK**: Platform 36
- **Android Build Tools**: 36.0.0

### 빌드 방법

프로젝트 루트 디렉토리에서 아래 명령어로 로컬 빌드 및 테스트를 수행할 수 있습니다.

**Windows PowerShell**:
```powershell
# 빌드 및 컴파일
.\gradlew.bat compileDebugSources
# 유닛 테스트 실행
.\gradlew.bat test
# 디버그 APK 빌드
.\gradlew.bat assembleDebug
```

**Linux / macOS / CI**:
```bash
./gradlew compileDebugSources
./gradlew test
./gradlew assembleDebug
```

---

## 🔐 릴리즈 빌드 및 서명 정책

릴리즈 APK 및 AAB는 보안을 위해 로컬 환경 변수 주입을 지원합니다. Play Console 업로드 전에는 로컬 `.keystore/my-upload-key.jks`를 백업해두거나 아래 환경 변수를 명시해 실제 업로드 키로 서명해야 합니다.

- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

```bash
# signed release APK 및 AAB 동시 빌드
./gradlew assembleRelease bundleRelease

# Play Console 제출용 AAB 및 다국어 릴리즈 노트를 바탕화면 Build 폴더로 내보내기
./gradlew :app:exportReleaseToDesktop
```
