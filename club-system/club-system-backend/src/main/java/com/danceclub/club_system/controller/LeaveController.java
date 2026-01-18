package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.LeaveRequestDTO;
import com.danceclub.club_system.dto.LeaveResponseDTO;
import com.danceclub.club_system.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 請假管理控制層
 * 統一路徑為 /api/leaves
 */
@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*") 
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // --- 1. GET: 取得所有請假單 (回傳 DTO 列表) ---
    @GetMapping
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // --- 2. POST: 新增請假單 (接收 RequestDTO, 回傳 ResponseDTO) ---
    @PostMapping
    public ResponseEntity<LeaveResponseDTO> createLeave(@RequestBody LeaveRequestDTO dto) {
        try {
            LeaveResponseDTO newLeave = leaveService.submitNewRequest(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newLeave);
        } catch (Exception e) {
            // 這裡可以根據異常訊息判斷是找不到用戶還是活動
            return ResponseEntity.badRequest().build();
        }
    }

    // --- 3. PUT: 審核請假單 (回傳 ResponseDTO) ---
    @PutMapping("/review/{id}")
    public ResponseEntity<LeaveResponseDTO> reviewLeave(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reviewerId,
            @RequestParam(required = false, defaultValue = "Checked by Admin") String note) {
        
        return leaveService.reviewRequest(id, reviewerId, status, note)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- 4. GET: 查詢特定使用者的請假紀錄 ---
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByMember(@PathVariable String memberId) {
        // 注意：這裡 Service 回傳的是 List<LeaveResponseDTO>
        List<LeaveResponseDTO> leaves = leaveService.getLeavesByMemberId(memberId);
        return ResponseEntity.ok(leaves);
    }

    // --- 5. DELETE: 刪除請假單 ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        try {
            leaveService.deleteRequest(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
