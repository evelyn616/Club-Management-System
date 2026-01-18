package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.LeaveRequestDTO;
import com.danceclub.club_system.dto.LeaveResponseDTO;
import com.danceclub.club_system.model.LeaveEntity;
import com.danceclub.club_system.model.User;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.repository.LeaveRepository;
import com.danceclub.club_system.repository.ActivityRepository;
import com.danceclub.club_system.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;      
    private final ActivityRepository activityRepository; 

    public LeaveService(LeaveRepository leaveRepository, 
                        UserRepository userRepository, 
                        ActivityRepository activityRepository) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    /**
     * 獲取所有請假單
     */
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根據 User ID 查詢請假紀錄
     */
    public List<LeaveResponseDTO> getLeavesByMemberId(String userId) {
        // 使用 getReferenceById 避開 User 欄位檢查
        User user = userRepository.getReferenceById(userId);

        List<LeaveEntity> leaves = leaveRepository.findLeaveRequestsByMemberQuery(user);

        return leaves.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 提交新申請 - 這是最容易報錯的地方，已修正為安全模式
     */
    @Transactional
    public LeaveResponseDTO submitNewRequest(LeaveRequestDTO dto) {
        // 1. 使用 getReferenceById：只拿 ID 代理，不觸發 SELECT * FROM "Member"
        User member = userRepository.getReferenceById(dto.getMemberId());

        // 2. Activity 欄位通常沒問題，可以用 findById
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new RuntimeException("找不到該活動: " + dto.getActivityId()));

        // 3. 組裝 Entity
        LeaveEntity leaveEntity = LeaveEntity.builder()
                .member(member)
                .activity(activity)
                .leaveType(dto.getLeaveType())
                .reason(dto.getReason())
                .status("PENDING")
                .build();

        // 4. 儲存
        LeaveEntity saved = leaveRepository.save(leaveEntity);
        
        // 5. 轉回 DTO (注意：轉換過程不能觸發 member.getName())
        return convertToResponseDTO(saved);
    }

    /**
     * 審核請假單
     */
    @Transactional
    public Optional<LeaveResponseDTO> reviewRequest(Long requestId, String reviewerId, String newStatus, String reviewNote) {
        return leaveRepository.findById(requestId).map(request -> {
            if (reviewerId != null) {
                // 審核人也使用 getReferenceById 避開欄位報錯
                User reviewer = userRepository.getReferenceById(reviewerId);
                request.setReviewedBy(reviewer);
            }
            request.setStatus(newStatus);
            request.setReviewNote(reviewNote);
            request.setReviewedAt(LocalDateTime.now());
            return convertToResponseDTO(leaveRepository.save(request));
        });
    }

    /**
     * 安全的轉換方法：絕對不調用 User 的其他屬性
     */
    private LeaveResponseDTO convertToResponseDTO(LeaveEntity entity) {
        return LeaveResponseDTO.builder()
                .id(entity.getId())
                // 這裡只拿 ID 是安全的
                .memberId(entity.getMember().getId()) 
                // ⚠️ 這裡強制給 null 或固定字串，因為你的資料庫 Member 欄位名稱不對
                .memberName("MEMBER") 
                .activityId(entity.getActivity().getId())
                .activityTitle(entity.getActivity().getTitle())
                .leaveType(entity.getLeaveType())
                .reason(entity.getReason())
                .status(entity.getStatus())
                // 同理，審核人名字也先給 null
                .reviewedByName(null) 
                .reviewedAt(entity.getReviewedAt())
                .reviewNote(entity.getReviewNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteRequest(Long id) {
        LeaveEntity leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該請假單"));
        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalStateException("只有待審核狀態可以刪除");
        }
        leaveRepository.deleteById(id);
    }
}