# 🏅 GoalBuddy
> 친구들과 하루하루의 목표를 나누고 성취해 나가며 매일매일 하나 이상의 목표를 함께 달성해 보자! <br>
> 오늘의 소소한 약속을 친구들과 공유하며 서로를 응원하고, 목표를 꾸준히 달성할 수 있는 원동력을 키워 보자! 


<br><br>

## 🛠 Tech Stack
### **Backend**
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-6DB33F?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-gray?style=flat-square)

### **Database & Infrastructure**
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle)

### **Frontend**
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript)

<br><br>

## ⚙️ Configuration
| 항목 | 상세 내용 | 관련 기술 |
| :--- | :--- | :--- |
| **보안 (CSRF)** | 모든 비동기 요청 헤더에 **CSRF 토큰**을 포함하여 보안 강화 | `Spring Security` |
| **인증** | `@AuthenticationPrincipal`을 활용한 현재 로그인 사용자 정보 조회 | `CustomUserDetails` |
| **파일 관리** | `application.properties`의 설정으로 물리적 저장 경로 관리 및 **UUID 난독화** | `FileService`, `WebConfig` |
| **트랜잭션** | 데이터 일관성 유지를 위해 생성/수정/삭제 로직에 **`@Transactional`** 적용 | `Spring Framework` |
| **무결성** | 유저 탈퇴 시 연관 데이터(목표, 친구, 좋아요) **`ON DELETE CASCADE`** 처리 | `JPA / MySQL` |

<br><br>

## 📱 Key Features

### 1. 회원 관리 및 보안
* **회원가입**
  * Email 아이디 체계
  * 이메일/닉네임 중복 방지(`unique`)
  * 정규표현식 기반 패스워드 검증 (영어 대/소문자, 숫자, 특수문자 중 2개 이상 포함하여 8자 이상)<br><br>  
* **인증/인가**
  * `CustomUserDetails` 연동 보안 체계 구축
  * 로그인된 유저만 전체 서비스 이용 가능<br><br>
* **계정 보호**
  * 패스워드에 `BCrypt` 암호화 적용
  * 로그인 5회 실패 시 계정 자동 잠금
  * 이메일 토큰 기반 비밀번호 재설정 통한 계정 잠금 해제 가능 <br><br> 
* **정보 수정**
  * 엔티티의 `FK`의 `ON DELETE CASCADE` 설정을 통해 유저 탈퇴 시 연관 데이터(목표, 친구 좋아요) 연쇄 삭제
  * 닉네임 변경: 이미 사용 중인 닉네임이 아닌지 검증 후 변경 진행
  * 프로필 사진 변경: 사용자가 업로드한 파일명 UUID를 사용해 난독화 및 외부 저장소/웹 서비스에서 조회할 경로 매핑
  * 패스워드 재설정: 이메일 인증 통해 가능 <br><br><br>

### 2. 오늘의 목표 관리 (TODAY)
* **목표 엔티티**
  * `User` 엔티티와 `N:1` 매핑 및 `CASCADE` 삭제<br><br>  
* **날짜별 관리**
  * 특정 날짜 목표 조회 및 오늘 날짜에 한정한 등록/수정/삭제 제어<br><br> 
* **수정 정책**
  * 과거 날짜 목표는 `readOnly`<br><br>  
* **실시간 반영**
  * `Fetch API`를 활용해 새로고침 없는 목표 상태 업데이트<br><br> 
* **제약 사항**
  * 하루 최대 **3개**의 목표만 등록 가능<br><br><br>

### 3. 피드 (FEED)
* **소셜 대시보드**
  * 나와 친구인 유저들의 '오늘' 목표 달성 현황 실시간 확인<br><br>
* **좋아요**
  * 하루 한 사용자에게 1회의 좋아요만 가능하도록 DB 유니크 제약 조건 설정<br><br>
* **관계 설계**
  * `FeedLike` 엔티티와 `User` 엔티티의 `N:1` 매핑 및 `CASCADE` 삭제<br><br><br>

### 4. 마이페이지 및 친구 관리
* **프로필 관리**
  * 닉네임, 프로필 사진, 패스워드 재설정 기능 제공<br><br>
* **친구 네트워크**
  * `from_user`와 `to_user` 구분에 의한 중복 관계 방지 DB 유니크 제약 조건<br><br>
* **관계 설계**
  * `Friend` 엔티티와 `User` 엔티티의 `N:1` 매핑 및 `CASCADE` 삭제<br><br>
* **친구 추가/삭제**
  * 닉네임 키워드 기반 사용자 검색 및 비동기 친구 관계 관리

<br><br>

## 📂 Project Structure
```text
src/main/java/goalbuddy/main/
├── config/             # Security, Auth Handler, Web Config
├── controller/         # Page & API Controllers
├── dto/                # Data Transfer Objects
├── entity/             # JPA Entities
├── repository/         # Spring Data JPA Repositories
└── service/            # Business Logic

