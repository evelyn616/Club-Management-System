package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.FeedbackFormDto;
import com.danceclub.club_system.dto.FeedbackStatsDto;
import com.danceclub.club_system.dto.FeedbackSubmitRequest;
import com.danceclub.club_system.model.enums.QuestionType;
import com.danceclub.club_system.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /** 建立表單（管理員） */
    @PostMapping("/forms")
    public ResponseEntity<?> createForm(@RequestBody Map<String, Object> body) {
        try {
            Long activityId = Long.parseLong(body.get("activityId").toString());
            String title = (String) body.get("title");
            String description = (String) body.getOrDefault("description", null);
            Integer pointsReward = body.containsKey("pointsReward")
                    ? Integer.parseInt(body.get("pointsReward").toString()) : null;
            FeedbackFormDto dto = feedbackService.createForm(activityId, title, description, pointsReward);
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /** 取得活動的回饋表單（公開；登入者可查看是否已提交） */
    @GetMapping("/forms/activity/{activityId}")
    public ResponseEntity<?> getFormByActivity(
            @PathVariable Long activityId,
            Authentication authentication) {
        try {
            String userId = authentication != null ? resolveUserId(authentication) : null;
            FeedbackFormDto dto = feedbackService.getFormByActivityId(activityId, userId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 取得表單（by formId） */
    @GetMapping("/forms/{formId}")
    public ResponseEntity<?> getForm(
            @PathVariable Long formId,
            Authentication authentication) {
        try {
            String userId = authentication != null ? resolveUserId(authentication) : null;
            FeedbackFormDto dto = feedbackService.getFormById(formId, userId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 更新表單基本資訊（管理員） */
    @PutMapping("/forms/{formId}")
    public ResponseEntity<?> updateForm(
            @PathVariable Long formId,
            @RequestBody Map<String, Object> body) {
        try {
            String title = (String) body.getOrDefault("title", null);
            String description = (String) body.getOrDefault("description", null);
            Integer pointsReward = body.containsKey("pointsReward")
                    ? Integer.parseInt(body.get("pointsReward").toString()) : null;
            return ResponseEntity.ok(feedbackService.updateForm(formId, title, description, pointsReward));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 開啟/關閉表單（管理員） */
    @PutMapping("/forms/{formId}/toggle")
    public ResponseEntity<?> toggleForm(@PathVariable Long formId) {
        try {
            return ResponseEntity.ok(feedbackService.toggleForm(formId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 新增題目（管理員） */
    @PostMapping("/forms/{formId}/questions")
    public ResponseEntity<?> addQuestion(
            @PathVariable Long formId,
            @RequestBody Map<String, Object> body) {
        try {
            String questionText = (String) body.get("questionText");
            QuestionType questionType = QuestionType.valueOf((String) body.get("questionType"));
            Boolean isRequired = (Boolean) body.getOrDefault("isRequired", false);
            Integer maxRating = body.containsKey("maxRating")
                    ? Integer.parseInt(body.get("maxRating").toString()) : null;
            @SuppressWarnings("unchecked")
            List<String> optionTexts = (List<String>) body.getOrDefault("options", null);
            return ResponseEntity.ok(feedbackService.addQuestion(formId, questionText, questionType,
                    isRequired, maxRating, optionTexts));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 更新題目（管理員） */
    @PutMapping("/forms/{formId}/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> body) {
        try {
            String questionText = (String) body.getOrDefault("questionText", null);
            Boolean isRequired = body.containsKey("isRequired") ? (Boolean) body.get("isRequired") : null;
            Integer maxRating = body.containsKey("maxRating")
                    ? Integer.parseInt(body.get("maxRating").toString()) : null;
            @SuppressWarnings("unchecked")
            List<String> optionTexts = (List<String>) body.getOrDefault("options", null);
            return ResponseEntity.ok(feedbackService.updateQuestion(formId, questionId,
                    questionText, isRequired, maxRating, optionTexts));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 刪除題目（管理員） */
    @DeleteMapping("/forms/{formId}/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId) {
        try {
            return ResponseEntity.ok(feedbackService.deleteQuestion(formId, questionId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 提交回饋（公開；若有 JWT 則記錄 userId 並給積分） */
    @PostMapping("/forms/{formId}/submit")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long formId,
            @RequestBody FeedbackSubmitRequest request,
            Authentication authentication) {
        try {
            String userId = authentication != null ? resolveUserId(authentication) : null;
            Map<String, Object> result = feedbackService.submitFeedback(formId, request, userId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** 統計摘要（管理員） */
    @GetMapping("/forms/{formId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long formId) {
        try {
            FeedbackStatsDto stats = feedbackService.getStats(formId);
            return ResponseEntity.ok(stats);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String resolveUserId(Authentication authentication) {
        // JWT subject 是 email，需要透過 UserRepository 轉換為 userId
        // 這裡直接用 email 當作 principal 傳入 Service，Service 內部處理
        // 注意：FeedbackResponse 的 userId 存的是 email（作為唯一識別），與 FeedbackService 保持一致
        return authentication.getName();
    }
}
