# Club System — Deployment Notes

> 最後更新：2026-03-22

---

## 技術棧

| 層 | 技術 |
|----|------|
| 前端 | Vue 3 + Vite + Pinia + Vue Router |
| 後端 | Spring Boot 3.5.7 + Spring Security + JWT (jjwt 0.12.3) |
| 資料庫 | Supabase PostgreSQL（Session Pooler） |
| ORM | Spring Data JPA / Hibernate |
| 郵件 | Spring Mail + Gmail SMTP |
| 支付 | 綠界 ECPay（測試環境） |

---

## 環境變數（後端）

在 `application.properties` 裡以 `${VAR:default}` 形式宣告，部署時需設定以下變數：

| 變數名 | 用途 | 備註 |
|--------|------|------|
| `DB_PASSWORD` | Supabase 資料庫密碼 | 預設值已硬寫，**正式環境務必替換** |
| `JWT_SECRET` | JWT 簽名金鑰（Hex 字串） | 預設值已硬寫，**正式環境務必替換** |
| `MAIL_USERNAME` | Gmail 寄件帳號 | 目前使用 `way911206@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | 需至 Google 帳號產生「應用程式密碼」|
| `ECPAY_MERCHANT_ID` | 綠界商店代號 | 預設為測試環境 `3002607` |
| `ECPAY_HASH_KEY` | 綠界 HashKey | 預設為測試值 |
| `ECPAY_HASH_IV` | 綠界 HashIV | 預設為測試值 |
| `ECPAY_API_URL` | 綠界 AIO 結帳 URL | 預設指向 stage 環境 |
| `ECPAY_QUERY_URL` | 綠界查詢 URL | 預設指向 stage 環境 |

### ECPay 回調 URL（ngrok）

目前寫死在 `application.properties`：

```
ecpay.notify-url=https://nondefensively-octamerous-nohemi.ngrok-free.dev/api/payments/ecpay/notify
ecpay.return-url=https://nondefensively-octamerous-nohemi.ngrok-free.dev/api/payments/ecpay/return
```

本機開發時需啟動 ngrok 並更新這兩個 URL。正式部署時改為實際 domain。

---

## 資料庫

- **主機**：`aws-1-ap-southeast-1.pooler.supabase.com:5432`
- **資料庫**：`postgres`
- **連線池**：HikariCP，最大 5 條連線（Supabase free tier 限制）
- **DDL**：`ddl-auto=update`，啟動自動同步 schema

### 手動 SQL 注意事項

#### DiscountType check constraint

Supabase 的 `activity_registration` 表有 check constraint，新增 DiscountType 時需手動更新：

```sql
ALTER TABLE activity_registration
  DROP CONSTRAINT IF EXISTS activity_registration_discount_type_check;

ALTER TABLE activity_registration
  ADD CONSTRAINT activity_registration_discount_type_check
  CHECK (discount_type IN ('NONE','EARLY_BIRD','COUPON','OFFICER','PROMO_CODE'));
```

#### welcomeCouponEnabled 預設值

`discount_config` 表在欄位加入前建立的 record，`welcome_coupon_enabled` 為 null，需手動補：

```sql
UPDATE discount_config SET welcome_coupon_enabled = true WHERE id = 1;
```

---

## 認證架構

### 一般登入

```
POST /api/auth/login
POST /api/auth/register
```

JWT 有效期：**24 小時**（`expiration=86400000` ms）

### 管理員二階段認證（Step-up Auth）

管理員需先以會員身分登入，再通過 Sidebar 的二次驗證才能使用管理功能：

**驗證流程：**
1. 一般登入 → 取得 JWT（role = admin，但 UI 只顯示會員功能）
2. Sidebar 點「進入管理模式」
3. **Step 1**：輸入登入密碼，呼叫 `POST /api/auth/admin/login` 確認
4. **Step 2**：選擇 MFA 方式（EMAIL / 手機簡訊）
5. **Step 3**：輸入 6 位驗證碼，呼叫 `POST /api/auth/admin/mfa/verify`
6. 驗證成功 → `adminElevated = true`（存 sessionStorage，關瀏覽器失效）

**MFA 相關 endpoint：**

| Method | Path | 說明 | 認證 |
|--------|------|------|------|
| POST | `/api/auth/admin/mfa/send` | 發送驗證碼 | 需 JWT |
| POST | `/api/auth/admin/mfa/verify` | 驗證碼驗證 | 需 JWT |

**MFA 安全規格：**
- 驗證碼：6 位數字，in-memory 儲存（`ConcurrentHashMap`）
- 有效期：**5 分鐘**
- 最大嘗試次數：**5 次**（超過自動鎖定，需重新發送）
- 單次使用，驗證成功即刪除

> ⚠️ **重要**：MFA code 存於 JVM 記憶體，**不支援多實例部署**。若未來需水平擴展，需改為 Redis 儲存。

---

## MFA — 手機簡訊（待設定）

目前手機簡訊驗證的後端架構已建立，但尚未接入 SMS 服務。

**啟用步驟：**

1. 在 `pom.xml` 加入 Twilio dependency：

```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>10.x.x</version>
</dependency>
```

2. 在 `application.properties` 加入：

```properties
twilio.account-sid=${TWILIO_ACCOUNT_SID}
twilio.auth-token=${TWILIO_AUTH_TOKEN}
twilio.from-number=${TWILIO_FROM_NUMBER}
```

3. 在 `MfaService.java` 的 `sendCode()` 方法中，找到 `PHONE` 分支，替換 `throw new UnsupportedOperationException(...)` 為實際的 Twilio 發送邏輯。

4. 前端 `Sidebar.vue` 的手機選項會自動啟用（目前為 disabled 狀態）。

---

## Gmail App Password 設定

1. 前往 Google 帳號 → 安全性 → 兩步驟驗證（需先開啟）
2. 搜尋「應用程式密碼」→ 建立新密碼（選擇「郵件」）
3. 將產生的 16 位密碼設為 `MAIL_PASSWORD` 環境變數

---

## 前端

- **Dev server**：`http://localhost:5173`
- **API proxy**：直連後端 `http://localhost:8080`
- **CORS**：後端各 Controller 設定 `@CrossOrigin(origins = "http://localhost:5173")`

正式部署需更新所有 `@CrossOrigin` 的 origins 為實際 domain。

---

## 本機啟動流程

```bash
# 後端
cd club-system-backend
./mvnw spring-boot:run

# 前端
cd club-system-frontend
npm install
npm run dev

# ECPay 回調（需要時）
ngrok http 8080
# 更新 application.properties 的 ecpay.notify-url / ecpay.return-url
```
