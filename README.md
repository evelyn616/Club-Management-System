# 社團管理系統 (Club Management System)

一個基於 Spring Boot 和 Vue.js 3 的社團管理系統，提供活動管理、報名、繳費、請假等功能。

## 技術棧

### 後端
- **框架**: Spring Boot 3.2.0
- **語言**: Java 17
- **資料庫**: MySQL 8.0 / PostgreSQL
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **建構工具**: Maven
- **資料庫遷移**: Flyway

### 前端
- **框架**: Vue.js 3
- **建構工具**: Vite
- **狀態管理**: Pinia
- **路由**: Vue Router
- **HTTP 客戶端**: Axios

## 專案結構

```
club-management-system/
├── backend/                    # Spring Boot 後端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/club/management/
│   │   │   │   ├── config/           # 配置類
│   │   │   │   ├── controller/       # REST 控制器
│   │   │   │   ├── dto/              # 數據傳輸對象
│   │   │   │   ├── entity/           # JPA 實體
│   │   │   │   ├── repository/       # 數據訪問層
│   │   │   │   ├── security/         # 安全配置
│   │   │   │   └── service/          # 業務邏輯層
│   │   │   └── resources/
│   │   │       ├── application.yml   # 應用配置
│   │   │       └── db/migration/     # Flyway 遷移腳本
│   │   └── test/                     # 測試
│   ├── pom.xml                       # Maven 配置
│   └── Dockerfile
│
├── frontend/                   # Vue.js 前端
│   ├── src/
│   │   ├── components/        # 可重用組件
│   │   ├── views/             # 頁面組件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 狀態管理
│   │   ├── services/          # API 服務
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
└── docker-compose.yml         # Docker Compose 配置
```

## 快速開始

### 前置需求

- Java 17+
- Node.js 20+
- Maven 3.9+
- MySQL 8.0+ 或 PostgreSQL
- Docker & Docker Compose (可選)

### 使用 Docker Compose (推薦)

1. 複製環境變數文件：
```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

2. 啟動所有服務：
```bash
docker-compose up -d
```

3. 訪問應用：
- 前端: http://localhost:5173
- 後端 API: http://localhost:8080/api

### 本地開發

#### 後端設置

1. 配置資料庫：
```bash
cd backend
cp .env.example .env
# 編輯 .env 文件，設置資料庫連接信息
```

2. 啟動後端：
```bash
mvn spring-boot:run
```

後端將在 http://localhost:8080 運行

#### 前端設置

1. 安裝依賴：
```bash
cd frontend
npm install
```

2. 啟動開發服務器：
```bash
npm run dev
```

前端將在 http://localhost:5173 運行

## 環境變數配置

### 後端 (.env)

```env
# 資料庫配置
DB_URL=jdbc:mysql://localhost:3306/club_management
DB_USERNAME=root
DB_PASSWORD=password

# JWT 配置
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# 郵件配置
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS 配置
CORS_ORIGINS=http://localhost:5173
```

### 前端 (.env)

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## API 文檔

### 身份驗證
- `POST /api/auth/register` - 用戶註冊
- `POST /api/auth/login` - 會員登入
- `POST /api/auth/admin/login` - 管理員登入
- `GET /api/auth/me` - 獲取當前用戶信息

### 用戶管理
- `GET /api/users/:id` - 獲取用戶資料
- `PUT /api/users/:id` - 更新用戶資料
- `PUT /api/admin/users/:id/role` - 更新用戶角色 (管理員)

### 活動管理
- `POST /api/admin/activities` - 創建活動 (管理員)
- `GET /api/activities` - 獲取活動列表
- `GET /api/activities/:id` - 獲取活動詳情
- `PUT /api/admin/activities/:id` - 更新活動 (管理員)
- `DELETE /api/admin/activities/:id` - 刪除活動 (管理員)

### 報名管理
- `POST /api/activities/:id/register` - 報名活動
- `DELETE /api/registrations/:id` - 取消報名
- `POST /api/registrations/:id/checkin` - 點名
- `GET /api/registrations/my` - 獲取我的報名記錄

### 繳費管理
- `GET /api/payments/my` - 獲取我的繳費記錄
- `POST /api/payments/:id/pay` - 線上支付
- `GET /api/admin/payments/pending` - 獲取待審核繳費 (管理員)
- `PUT /api/admin/payments/:id/approve` - 審核繳費 (管理員)
- `PUT /api/admin/payments/:id/refund` - 處理退費 (管理員)

### 請假管理
- `POST /api/leaves` - 提交請假申請
- `GET /api/leaves/my` - 獲取我的請假記錄
- `GET /api/admin/leaves/pending` - 獲取待審核請假 (管理員)
- `PUT /api/admin/leaves/:id/approve` - 批准請假 (管理員)
- `PUT /api/admin/leaves/:id/reject` - 拒絕請假 (管理員)

## 開發指南

### 資料庫遷移

使用 Flyway 管理資料庫遷移：

1. 在 `backend/src/main/resources/db/migration/` 創建遷移腳本
2. 命名格式: `V{version}__{description}.sql`
3. 例如: `V1__create_user_table.sql`

### 添加新功能

1. 創建 Entity (實體類)
2. 創建 Repository (數據訪問接口)
3. 創建 Service (業務邏輯)
4. 創建 Controller (REST API)
5. 創建 DTO (數據傳輸對象)
6. 編寫測試

## 測試

### 後端測試
```bash
cd backend
mvn test
```

### 前端測試
```bash
cd frontend
npm run test
```

## 部署

### 使用 Docker Compose

```bash
docker-compose up -d
```

### 手動部署

1. 構建後端：
```bash
cd backend
mvn clean package
java -jar target/club-management-1.0.0.jar
```

2. 構建前端：
```bash
cd frontend
npm run build
# 將 dist/ 目錄部署到 Web 服務器
```

## 授權

MIT License

## 聯繫方式

如有問題或建議，請聯繫開發團隊。
