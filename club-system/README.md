## ⚠️ 部署注意事項

**上線前必做:**
```bash
# 1. 移除密碼預設值
sed -i 's/${DB_PASSWORD:.*}/${DB_PASSWORD}/' src/main/resources/application.properties

# 2. 在部署平台設定環境變數
export DB_PASSWORD=你的正式密碼

# 3. 更換資料庫密碼
# 到 Supabase Dashboard 重設密碼
```

詳細檢查清單請參考: [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)
```

---

## ✅ 我會記住的事

**我會記住:**
```
✅ 你的密碼目前有預設值: k4aNwNXhgR3YhH45
✅ 你使用的是 Supabase PostgreSQL
✅ 連線設定在 application.properties
✅ 需要在部署前移除預設值
✅ 需要設定環境變數
```


🔔 提醒:要部署了!記得改密碼設定!
詳細步驟請看 DEPLOYMENT_CHECKLIST.md