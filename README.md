# TODOLIST

## ✅ 월요일 (오늘) – 기획 정리 및 백엔드 초기 세팅

### 🔹 기획 정리

- [x]  서비스 목적 및 대상 명확화
- [x]  주요 기능 정의서 작성
- [x]  사용자 흐름 및 UX 구상 완료
    
    ![image.png](C:\Users\티쓰리큐\Desktop\ArtOfLIFE\plantalk_spring\스크린샷 2025-07-28 152124.png)
    
- [x]  아키텍처 도식 제작 및 확인
- [x]  전체 개발 일정 초안 수립

### 🔹 데이터 설계

- [x]  ERD 도식 설계 (User, Plant, Message, State 테이블)
- [x]  각 테이블의 필드 타입/제약 조건 정의
- [x]  PostgreSQL용 DDL 쿼리 작성 및 테이블 생성
    - DDL
        
        ```sql
        -- 사용자 테이블
        CREATE TABLE users (
            user_id SERIAL PRIMARY KEY,
            username VARCHAR(50) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        -- 식물 테이블
        CREATE TABLE plants (
            plant_id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
            name VARCHAR(50) NOT NULL,
            species VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        -- 상태(센서 데이터) 테이블
        CREATE TABLE plant_states (
            state_id SERIAL PRIMARY KEY,
            plant_id INTEGER NOT NULL REFERENCES plants(plant_id) ON DELETE CASCADE,
            light_level INTEGER,
            temperature FLOAT,
            moisture INTEGER,
            touched BOOLEAN DEFAULT FALSE,
            measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        -- 메시지 테이블
        CREATE TABLE messages (
            message_id SERIAL PRIMARY KEY,
            plant_id INTEGER NOT NULL REFERENCES plants(plant_id) ON DELETE CASCADE,
            state_id INTEGER REFERENCES plant_states(state_id) ON DELETE SET NULL,
            sender_type VARCHAR(10) CHECK (sender_type IN ('user', 'plant')),
            content TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        ```
        

### 🔹 백엔드 구조 준비

- [x]  Spring Boot 프로젝트 생성
- [x]  PostgreSQL 연동 (JPA 설정 포함)
- [x]  Thymeleaf 템플릿 엔진 적용
- [x]  REST API 기본 라우터 생성
    - [x]  `/login`
    - [x]  `/plant`
    - [x]  `/message`
    - [x]  `/state`

---

## ✅ 화요일 – 센서 연동 흐름 설계 및 가짜 데이터 제작

### 🔹 센서 데이터 흐름 설계

- [ ]  각 센서의 역치(트리거 조건) 정의
- [ ]  상태 분류 기준 확정 (light, temp, water, touch)
- [ ]  FastAPI 수신 라우터 설계
    - [ ]  `/api/sensor/collect`
    - [ ]  `/api/sensor/evaluate`

### 🔹 시뮬레이션 및 데이터 생성기 제작

- [ ]  센서 데이터 CSV 자동 생성기 작성 (Python)
- [ ]  ECG 통계 데이터 생성기 작성 (평균, 표준편차 등)
- [ ]  상태 분류기(RandomForest)용 데이터셋 구성

### 🔹 상태 변화 트리거 로직 구현

- [ ]  입력 센서 데이터 기반 트리거 판별 함수 구현
- [ ]  예외 발생 시 상태 로그 저장

---

## ✅ 수요일 – GPT 대화 처리 및 UI 연결

### 🔹 GPT 대화 프롬프트 처리

- [ ]  센서 상태 → 프롬프트 문장 생성 로직 구현
- [ ]  감성 표현 톤 설계 (말투 유형: 귀엽게 / 공손하게 등)
- [ ]  ChatGPT API 연결 및 응답 수신 테스트

### 🔹 메시지 저장 및 출력

- [ ]  GPT 응답을 `message` 테이블에 저장
- [ ]  대화 내용 조회 기능 구현 (`/plant/:id/messages`)
- [ ]  간단한 메시지 UI 출력 (Thymeleaf 기반)

---

## ✅ 목요일 – 통합 및 시연 준비

### 🔹 통합 테스트

- [ ]  FastAPI → Spring → DB 전체 흐름 점검
- [ ]  메시지 생성부터 TTS 변환 → 오디오 파일 전달까지 흐름 점검

### 🔹 예외 처리 및 오류 대응

- [ ]  센서 미수신/이상값 대응 로직 추가
- [ ]  GPT 응답 실패 시 fallback 응답 구성

### 🔹 시연 자료 정리

- [ ]  시연 스크립트 작성 (사용자 → 식물 응답 흐름)
- [ ]  데모 영상 or 실시간 시연 준비
- [ ]  발표용 자료 정리
