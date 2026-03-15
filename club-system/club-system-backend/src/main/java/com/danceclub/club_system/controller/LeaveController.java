package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.LeaveRequestDTO;
import com.danceclub.club_system.dto.LeaveResponseDTO;
import com.danceclub.club_system.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
    public ResponseEntity<?> createLeave(@RequestBody LeaveRequestDTO dto) { // 注意這裡改成 <?>
        try {
            LeaveResponseDTO newLeave = leaveService.submitNewRequest(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newLeave);
        } catch (RuntimeException e) {
            // 這裡回傳 Map 是為了讓前端能看到 "請假失敗：您並未報名此活動"
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
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
    @GetMapping("/user/{userId}") // 這裡也改成 /user/
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByUser(@PathVariable String userId) {
        return ResponseEntity.ok(leaveService.getLeavesByUserId(userId));
    }

    // --- 5. DELETE: 刪除請假單 ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeave(@PathVariable Long id) {
        try {
            leaveService.deleteRequest(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 這行非常重要！它會在你的後端控制台印出錯誤原因（例如：SQLIntegrityConstraintViolationException）
            e.printStackTrace(); 
            
            // 暫時回傳 500 和錯誤訊息給前端，方便我們除錯
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("後端刪除出錯: " + e.getMessage());
        }
    }
}
