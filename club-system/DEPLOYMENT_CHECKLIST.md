# 🚀 部署前檢查清單

## ⚠️ 安全性檢查

### 1. 資料庫密碼
- [ ] 移除 `application.properties` 中的密碼預設值
- [ ] 改成: `spring.datasource.password=${DB_PASSWORD}`
- [ ] 在部署平台設定環境變數 `DB_PASSWORD`
- [ ] 更換 Supabase 資料庫密碼

### 2. 環境設定
- [ ] 建立 `application-prod.properties`
- [ ] 設定正式環境的資料庫連線
- [ ] 關閉 SQL 顯示: `spring.jpa.show-sql=false`
- [ ] DDL 改成: `spring.jpa.hibernate.ddl-auto=validate`

### 3. CORS 設定
- [ ] 只允許正式前端網址
- [ ] 移除 `allowedOrigins=*`

### 4. 日誌設定
- [ ] 關閉 DEBUG 級別
- [ ] 只保留 INFO 和 ERROR

### 5. Git 檢查
- [ ] 確認 `.gitignore` 包含所有敏感檔案
- [ ] 檢查沒有提交密碼或 API Key
- [ ] 清理 Git 歷史中的敏感資料(如果有)

## 📝 部署步驟

### Railway / Render 部署
1. 在平台設定環境變數 `DB_PASSWORD`
2. 連接 GitHub Repository
3. 設定建置指令: `mvn clean package`
4. 設定啟動指令: `java -jar target/*.jar`
5. 設定 Port: `8080`

### Docker 部署
```bash
docker run -e DB_PASSWORD=你的密碼 -p 8080:8080 你的映像
```

## ✅ 部署後驗證
- [ ] 測試 API 端點
- [ ] 檢查資料庫連線
- [ ] 確認前端可以正常呼叫
- [ ] 檢查日誌沒有錯誤

---

**記得在上線前逐一完成這些檢查!** 🔒