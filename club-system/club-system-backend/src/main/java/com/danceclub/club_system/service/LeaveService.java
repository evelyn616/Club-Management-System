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

    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<LeaveResponseDTO> getLeavesByUserId(String userId) {
        // 先找出 User，再找請假紀錄
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶: " + userId));
        return leaveRepository.findLeaveRequestsByUserQuery(user).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponseDTO submitNewRequest(LeaveRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("找不到該用戶: " + dto.getUserId()));

        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new RuntimeException("找不到該活動: " + dto.getActivityId()));

        LeaveEntity leaveEntity = LeaveEntity.builder()
                .user(user) // 這裡用了改名後的 .user()
                .activity(activity)
                .leaveType(dto.getLeaveType())
                .reason(dto.getReason())
                .status("PENDING")
                .build();

        return convertToResponseDTO(leaveRepository.save(leaveEntity));
    }

    @Transactional
    public Optional<LeaveResponseDTO> reviewRequest(Long requestId, String reviewerId, String newStatus, String reviewNote) {
        return leaveRepository.findById(requestId).map(request -> {
            if (reviewerId != null) {
                User reviewer = userRepository.findById(reviewerId)
                        .orElseThrow(() -> new RuntimeException("找不到審核人"));
                request.setReviewedBy(reviewer);
            }
            request.setStatus(newStatus);
            request.setReviewNote(reviewNote);
            request.setReviewedAt(LocalDateTime.now());
            return convertToResponseDTO(leaveRepository.save(request));
        });
    }

    private LeaveResponseDTO convertToResponseDTO(LeaveEntity entity) {
        return LeaveResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId()) // 安全調用
                .userName(entity.getUser().getName()) // 讀取對方的 User.name
                .activityId(entity.getActivity().getId())
                .activityTitle(entity.getActivity().getTitle())
                .leaveType(entity.getLeaveType())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .reviewedByName(entity.getReviewedBy() != null ? entity.getReviewedBy().getName() : null)
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