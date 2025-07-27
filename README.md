# 👔 Marico

## 📌 프로젝트 소개

- 패션에 관심 있지만 스타일링은 미흡한 20대 남성에게
- 👉스타일리스트 매칭을 통한 개인 맞춤형 스타일링 제공'

## 팀원
- 기획 : 박현진
- 디자인 : 조영우 천후민
- 개발(FE) : 이세형 채기중
- 개발(BE) : 박준형 이지은

## 주요 화면 소개
### 회원가입
https://github.com/user-attachments/assets/9157ee92-7fa7-417c-bdb2-c1b2c4c3f5bc

### 아이디/비번 찾기
https://github.com/user-attachments/assets/946d8918-0856-450b-9547-4a18b8b68f11

### 로그인
https://github.com/user-attachments/assets/62348d2f-5920-4b61-87ad-dcfb3cb80afd

### 스타일리스트 추천/탐색 및 서비스 신청
https://github.com/user-attachments/assets/bbda18a7-e367-45a1-bc09-f8e20361abea

### 프로필 관리(고객)
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/b79fc0e4-2e13-430a-b572-31c06a28d84e" />

### 프로필 관리(스타일리스트)
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/f25d8f0f-52ce-41d5-8a7e-153ad29aea60" />


## ⏰ 개발 기간
2023.09 ~ 2024.01

## 🛠 기술 스택

### 개발 환경
- JDK17
- Gradle
- Springboot 3.0.5
- Spring Data JPA

### 보안
- SpringSecurity
- JWT

### 데이터베이스
- MySQL + Docker(개발 환경)
- AWS RDS + MySQL(배포 환경)
- AWS S3 (이미지 저장)
## 🧭 Service Architecture

![marico](https://github.com/JunHyeong-99/apptive-19th-malico-backend/assets/64734115/c8cad465-e895-4629-a198-1e7a7a2dea85)

## 🚀 주요 기능
### 회원 가입  
- 스타일리스트, 고객을 분리하여 회원가입 기능 구현  
- 메일 인증 시 smtp 이용한 메일 전송 기능 구현  
### Login    
- 스프링 시큐리티 + jwt를 이용한 로그인 기능 구현  
### Mypage  
- 고객의 Mypage에서 개인 정보 설정 및 관심 스타일리스트 등록 기능 구현  
- 스타일리스트의 Mypage에서 자신의 서비스 등록 기능 구현    
### 공지 사항  
- 관리자의 공지 사항이 있을 때 공지 사항 전송 기능 구현  
- 공지 사항을 읽었는지 표시하는 기능 구현  
### 알림  
- 고객의 스타일리스트 서비스 문의, 스타일리스트의 문의 답변 시 알람기능 구현  
### 추천 기능  
- 고객이 등록한 관심 스타일과 지역을 위치기반 스타일리스트 추천 기능 구현  
### 스타일리스트 탐색  
- 스타일, 지역, 성별에 따른 스타일리스트 필터 기능 구현  
