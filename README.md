# Jetpack Compose Projects

Android 앱 개발 학습 과정에서 Jetpack Compose를 활용한 프로젝트 모음입니다.

---

## 목차

- [Week 5 - Counter & Stopwatch](#week-5---counter--stopwatch)
- [Week 6 - Bubble Game](#week-6---bubble-game)

---

## 5주차 - Counter & Stopwatch

### 프로젝트 개요

기본적인 상태 관리와 타이머 기능을 학습하기 위한 카운터 및 스톱워치 앱입니다.

### 기존 코드

- Jetpack Compose 기본 예제 형태
- 카운터 증가 / 리셋 버튼 구현
- 단순한 `while(true)` 루프 기반 스톱워치
- 상태 관리: `mutableStateOf`, `remember` 사용

### 개선 사항

- **Material3 UI** 적용 (Card, RoundedCornerShape 등)
- **LaunchedEffect** 기반 조건부 타이머 실행
- `formatTime()` 함수로 시간 계산 로직 분리
- **Button** 배치 및 정렬 개선으로 직관성 향상
- 코드 구조 단순화 및 UI 일관성 확보

### 주요 학습 내용

- Compose 상태 관리 (`remember`, `mutableStateOf`)
- Side Effect 처리 (`LaunchedEffect`)
- Material3 디자인 시스템 적용
- 시간 포맷팅 및 타이머 구현

---

## 6주차 - Bubble Game

### 프로젝트 개요

물리 효과와 애니메이션을 적용한 인터랙티브 버블 게임입니다.

### 기존 코드

- 랜덤 버블 생성 및 단순 이동
- 클릭 시 점수 +1
- 60초 타이머 종료 후 게임 종료
- 물리 효과나 애니메이션 없음

### 개선 사항

- **물리 반동 및 중력 시스템** 추가 → 버블의 움직임이 자연스러움
- **터치 시 폭발 이펙트** 및 입자 표현 추가
- **GamePhase(START / PLAYING / GAMEOVER)** 도입으로 게임 루프 구조화
- **Restart 기능** 추가로 반복 플레이 가능
- 콤보 점수 및 터치 피드백 강화로 인터랙션 향상

### 주요 학습 내용

- Canvas를 이용한 커스텀 드로잉
- 물리 시뮬레이션 구현 (중력, 충돌 감지)
- 게임 상태 관리 및 페이즈 전환
- 애니메이션 및 사용자 인터랙션 처리

---

## 기술 스택

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design**: Material3
- **Architecture**: MVVM (상태 관리)

---

## 📝 라이선스

이 프로젝트는 학습 목적으로 작성되었습니다.

---

## 👤 작성자

학습 과정에서 작성한 프로젝트입니다.
