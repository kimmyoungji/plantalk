
# TODOLIST

## 프로젝트 개요
이 프로젝트는 외로운 이들에게 식물과의 대화를 통해 위로와 연결감을 제공하는 서비스입니다.

## 프로젝트 핵심 구현 방법
먼저 식물의 상태 데이터, 사람의 입력 대화를 수집한다.
수집한 데이터를 아래와 같은 프롬프트에 담아 chatGPT에게 전달한다.
"너는 외로운 사람을 위로하는 식물이다. 
만약 너의 상태가 ${식물 상태 데이터}이고, 
사람이 너에게 ${사람의 입력 대화}라고 말을 걸었다면, 
너는 무엇이라고 대답할 것인가?"
chatGPT의 응답을 마치 식물의 말인 것 처럼 사용자에게 응답해준다.

## 프로젝트 체크리스트
시간 부족으로 아두이노 센서 설치를 할 수 없어서 현재 단계에서는
처음 식물이 등록 될 때는 랜덤으로 상태가 지정되고, 
등록 후에는 사용자가 식물의 상태를 직접 수정할 수 있도록 구현할 것이다. 

## 프로젝트 스택
- 백엔드
  - Java 17
  - Spring Data JPA
  - Spring WebSocket
  - Spring Validation
  - Thymeleaf
  - Lombok
- DB
  - PostgreSQL
  - Hibernate
- 프론트엔드
  - JavaScript
  - HTML / CSS
  - Fetch API
- 개뱔도구 
  - Maven
  - Spring Boot DevTools


## 프로젝트 TODOLIST


## ✅ 월요일 (오늘) – 기획 정리 및 백엔드 초기 세팅

### 🔹 기획 정리

- [x]  서비스 목적 및 대상 명확화
- [x]  주요 기능 정의서 작성
- [x]  사용자 흐름 및 UX 구상 완료
    
    ![image.png](C:\Users\티쓰리큐\Desktop\ArtOfLIFE\plantalk_spring\plantalk_uxui.png)
    
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
    - 사용자/로그인 
      - [x]  POST `/api/login` : 로그인
      - [x] POST `/api/login/register`: 사용자 등록
      - [x] GET `/api/login/check-email`: 이메일 중복 확인
      - [x] GET `/api/login/check-username`: 사용자명 중복 확인
    - 식물
      - [x]  POST `/api/plant`: 식물 생성
      - [x]  GET `/api/plant/{plantId}`: 식물 조회
      - [x]  GET `/api/plant/user/{userId}`: 사용자의 모든 식물 조회
      - [x]  PUT `/api/plant/{plantId}`: 식물 정보 업데이트
      - [x]  DELETE `/api/plant/{plantId}`: 식물 삭제
    - 채팅
      - WebSocket `/chat.sendMessage/{plantId}`: 채팅 메시지 전송 처리 -> `/topic/public/{plantId}`
      - WebSocket `/chat.addUser/{plantId}`: 사용자 입장 처리 -> `/topic/public/{plantId}`
    - 메세지
    - [x]  POST `/api/message`: 메세지 등록
    - [x]  GET `/api/message/{messageId}`: 메세지 조회
    - [x]  GET `/api/message/plant/{plantId}`: 특정 식물의 모든 메시지 조회
    - [x]  GET `/api/message/plant/{plantId}/recent`: 특정 식물의 최근 메시지 조회
    - [x]  GET `/api/message/plant/{plantId}/sender/{senderType}`: 특정 발신자 유형의 메시지 조회
    - 식물 상태 ( 분류 기준: light, temp, water, touch)
    - [x]  POST `/api/state`: 식물 상태 생성
    - [x]  GET `/api/state/{stateId}`: 식물 상태 조회
    - [x]  GET `/api/state/plant/{plantId}`: 특정 식물의 모든 상태 조회
    - [x]  GET `/api/state/plant/{plantId}/latest`: 특정 식물의 가장 최근 상태 조회
    - [x]  GET `/api/state/plant/{plantId}/period`: 특정 기간 내의 식물 상태 조회
    - [x]  GET `/api/state/plant/{plantId}/evaluate`: 식물 상태 평가
    - [x]  PUT `/api/state/{stateId}`: 식물 상태 업데이트
    - [x]  DELETE `/api/state/{stateId}`: 식물 상태 삭제

### 🔹 GPT 대화 프롬프트 처리

- [x]  센서 상태 → 프롬프트 문장 생성 로직 구현
- [ ]  감성 표현 톤 설계 (말투 유형: 귀엽게 / 공손하게 등)
- [ ]  ChatGPT API 연결 및 응답 수신 테스트

### 🔹 메시지 저장 및 출력

- [ ]  GPT 응답을 `message` 테이블에 저장
- [x]  대화 내용 조회 기능 구현 (`/plant/:id/messages`)
- [x]  간단한 메시지 UI 출력 (Thymeleaf 기반)

