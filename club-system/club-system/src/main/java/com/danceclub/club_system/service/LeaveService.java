package com.example.demo.service;

import com.example.demo.entity.LeaveEntity;
import com.example.demo.entity.MemberEntity;
import com.example.demo.entity.ActivityEntity;
import com.example.demo.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    @Autowired
    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    /**
     * 新增：取得所有請假單 (供 Controller 使用)
     */
    public List<LeaveEntity> getAllLeaves() {
        return leaveRepository.findAll();
    }

    /**
     * 1. INSERT (新增/提交請假單)
     */
    @Transactional
    public LeaveEntity submitNewRequest(LeaveEntity request) {
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(request);
    }

    /**
     * 2. UPDATE/ALTER (修改/審核請假單)
     */
    @Transactional
    public Optional<LeaveEntity> reviewRequest(Long requestId, MemberEntity reviewer, String newStatus, String reviewNote) {
        return leaveRepository.findById(requestId).map(request -> {
            // 業務邏輯：更新狀態與審核資訊
            request.setStatus(newStatus);
            request.setReviewedBy(reviewer);
            request.setReviewNote(reviewNote);
            request.setReviewedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            return leaveRepository.save(request);
        });
    }

    /**
     * 3. DELETE: 刪除請假單
     * 為了配合你目前的 Controller，我們先簡化成只靠 ID 刪除
     */
    @Transactional
    public void deleteRequest(Long id) {
        Optional<LeaveEntity> request = leaveRepository.findById(id);
        if (request.isPresent()) {
            LeaveEntity leave = request.get();
            // 僅限 PENDING 狀態可刪除
            if (!"PENDING".equals(leave.getStatus())) {
                throw new IllegalStateException("Only PENDING requests can be deleted.");
            }
            leaveRepository.deleteById(id);
        }
    }

    // =======================================================
    // 查詢方法 (對應 Repository 的自定義查詢)
    // =======================================================

    public List<LeaveEntity> getRequestsByMember(MemberEntity member) {
        return leaveRepository.findLeaveRequestsByMemberQuery(member);
    }

    public List<LeaveEntity> getRequestsByStatus(String status) {
        return leaveRepository.findLeaveRequestsByStatusQuery(status);
    }

    public List<LeaveEntity> getRequestsByActivity(ActivityEntity activity) {
        return leaveRepository.findLeaveRequestsByActivityQuery(activity);
    }

    public List<LeaveEntity> getRecentRequests(int days) {
        LocalDateTime sinceTime = LocalDateTime.now().minusDays(days);
        return leaveRepository.findByCreatedAtAfter(sinceTime);
    }
}