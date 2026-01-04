package com.club.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Entity - 用戶實體
 * 用於存儲所有用戶（社員和管理員）的基本信息
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用戶 ID（主鍵），格式：m0001, m0002, m0003...
     */
    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String id;

    /**
     * 用戶姓名
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 電子郵件（唯一）
     */
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    /**
     * 密碼雜湊值（使用 bcrypt）
     */
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    /**
     * 電話號碼
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 生日
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 角色（'member' 或 'admin'）
     */
    @Column(name = "role", length = 20, nullable = false)
    private String role = "member";

    /**
     * 創建時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 在持久化之前自動設置創建時間和更新時間
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 在更新之前自動設置更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
