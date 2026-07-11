# 배포 아키텍처

이 프로젝트는 Terraform으로 AWS 인프라를 코드로 관리하고, GitHub Actions로 EC2에 자동 배포합니다.

## 전체 구조

```mermaid
flowchart TB
    Dev["👤 Developer"] -->|"git push main"| GH["GitHub Repository"]

    subgraph CICD["GitHub Actions (deploy.yml)"]
        direction TB
        B1["Gradle Build\n(./gradlew build)"] --> B2["Docker Build"]
        B2 --> B3["Push Image → ECR"]
        B3 --> B4["AWS SSM RunCommand\n→ EC2에 새 컨테이너 배포"]
    end
    GH --> B1

    subgraph AWS["AWS Cloud (Terraform으로 프로비저닝)"]
        direction TB

        subgraph VPC["VPC 10.0.0.0/16"]
            direction TB
            subgraph EC2["EC2 t3.micro (Elastic IP, IAM Role)"]
                direction TB
                App["Spring Boot App\n:8080 (Docker)"]
                Prom["Prometheus :9090"]
                Graf["Grafana :3001"]
                Redis["Redis :6379 / Exporter :9121"]
                CWAgent["CloudWatch Agent\n(mem/swap 수집)"]
            end
            RDS[("RDS MySQL 8.0\ndb.t3.micro\nEnhanced Monitoring")]
        end

        ECR["ECR\nbackend-portfolio\n(image scan on push)"]
        CWDash["CloudWatch Dashboard\nEC2 CPU/Mem, RDS CPU/Conn"]
        IAM["IAM Role\n(SSM + ECR + CloudWatchAgent policy)"]
    end

    subgraph External["외부 서비스"]
        S3[("S3 Bucket\n프로필/스타일링 이미지 저장")]
        SMTP["Gmail SMTP\n비동기 이메일 발송"]
    end

    B4 -.->|SSM| EC2
    ECR -.->|docker pull| App
    App -->|JDBC| RDS
    App -->|AWS SDK| S3
    App -.->|@Async 이벤트| SMTP
    Prom -->|scrape| App
    Prom -->|scrape| Redis
    Graf -->|query| Prom
    CWAgent -->|metrics| CWDash
    IAM -. attached .- EC2

    Client["🌐 Client (Web/App)"] -->|HTTP :8080| App
```

## 컴포넌트별 정리 (포트폴리오용 요약)

### 1. Infrastructure as Code — Terraform
- VPC, Public Subnet(2개, Multi-AZ), Internet Gateway, Route Table 구성
- EC2(t3.micro) + Elastic IP, IAM Role(SSM/ECR/CloudWatchAgent 정책 연결)
- RDS(MySQL 8.0, Enhanced Monitoring), ECR(이미지 스캔), CloudWatch Dashboard까지 `main.tf` 하나로 관리
- `user_data`로 EC2 부팅 시 Docker, AWS CLI, CloudWatch Agent 자동 설치 → 서버 세팅 자동화

### 2. CI/CD — GitHub Actions
- `main` 브랜치 push 시 자동 트리거
- Gradle 빌드 → Docker 이미지 빌드 → ECR push → **AWS SSM Send-Command**로 EC2에 원격 배포
- SSH 키 없이 SSM만으로 배포 (보안상 22번 포트 노출 최소화 관점에서 언급 가능)

### 3. Containerization — Docker
- Multi-stage build (`eclipse-temurin:17-jdk-alpine` 빌드 → `17-jre-alpine` 런타임)로 이미지 경량화
- `docker-compose.yml`로 로컬/모니터링 스택(App, Prometheus, Grafana, Redis, Redis Exporter) 구성, 리소스(cpus/memory) 제한 설정

### 4. Monitoring
- Actuator + Micrometer(Prometheus) → Prometheus → Grafana 시각화
- CloudWatch Agent + Dashboard로 EC2/RDS 인프라 지표(CPU, 메모리, DB 커넥션) 수집

### 5. AWS 연동 서비스
- **S3**: `AwsConfig`에서 `AmazonS3Client` 빈 등록, 이미지 업로드에 사용
- **RDS(MySQL)**: 운영 DB
- **SES 대신 Gmail SMTP + `@Async`**: 이메일 발송을 비동기 이벤트로 처리해 API 응답 지연 방지

## 참고
- 다이어그램은 [Mermaid Live Editor](https://mermaid.live)에 위 코드 블록을 붙여넣으면 PNG/SVG로 내보낼 수 있습니다.
- GitHub에 이 파일을 올리면 README/PR 등에서 자동으로 렌더링됩니다.
