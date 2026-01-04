package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;

@Entity
@Table(name = "leave_request")
public class LeaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 提交請假的人
    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    // 2. 關聯的活動
    @ManyToOne
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity; 

    private String reason;

    @Column(name = "leave_type")
    private String leaveType;

    private String status;

    // 3. 審核人
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private MemberEntity reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Getter & Setter 修正區 ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }


    public ActivityEntity getActivity() { return activity; }
    public void setActivity(ActivityEntity activity) { this.activity = activity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public MemberEntity getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(MemberEntity reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}