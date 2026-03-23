package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.FeedbackFormDto;
import com.danceclub.club_system.dto.FeedbackStatsDto;
import com.danceclub.club_system.dto.FeedbackSubmitRequest;
import com.danceclub.club_system.model.*;
import com.danceclub.club_system.model.enums.QuestionType;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import com.danceclub.club_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackFormRepository formRepo;
    private final FeedbackQuestionRepository questionRepo;
    private final FeedbackQuestionOptionRepository optionRepo;
    private final FeedbackResponseRepository responseRepo;
    private final FeedbackAnswerRepository answerRepo;
    private final ActivityRepository activityRepo;
    private final UserRepository userRepo;
    private final RegistrationRepository registrationRepo;

    public FeedbackService(FeedbackFormRepository formRepo,
                           FeedbackQuestionRepository questionRepo,
                           FeedbackQuestionOptionRepository optionRepo,
                           FeedbackResponseRepository responseRepo,
                           FeedbackAnswerRepository answerRepo,
                           ActivityRepository activityRepo,
                           UserRepository userRepo,
                           RegistrationRepository registrationRepo) {
        this.formRepo = formRepo;
        this.questionRepo = questionRepo;
        this.optionRepo = optionRepo;
        this.responseRepo = responseRepo;
        this.answerRepo = answerRepo;
        this.activityRepo = activityRepo;
        this.userRepo = userRepo;
        this.registrationRepo = registrationRepo;
    }

    // ─── 建立表單 ────────────────────────────────────────────────

    @Transactional
    public FeedbackFormDto createForm(Long activityId, String title, String description, Integer pointsReward) {
        if (formRepo.existsByActivityId(activityId)) {
            throw new IllegalStateException("此活動已有回饋表單");
        }
        FeedbackForm form = new FeedbackForm();
        form.setActivityId(activityId);
        form.setTitle(title);
        form.setDescription(description);
        form.setIsOpen(false);
        form.setPointsReward(pointsReward != null ? pointsReward : 5);
        formRepo.save(form);
        return toDto(form, null);
    }

    // ─── 取得表單（含題目）────────────────────────────────────────

    public FeedbackFormDto getFormByActivityId(Long activityId, String currentUserId) {
        FeedbackForm form = formRepo.findByActivityId(activityId)
                .orElseThrow(() -> new NoSuchElementException("此活動尚未建立回饋表單"));
        return toDto(form, currentUserId);
    }

    public FeedbackFormDto getFormById(Long formId, String currentUserId) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));
        return toDto(form, currentUserId);
    }

    // ─── 更新表單設定 ─────────────────────────────────────────────

    @Transactional
    public FeedbackFormDto updateForm(Long formId, String title, String description, Integer pointsReward) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));
        if (title != null && !title.isBlank()) form.setTitle(title);
        if (description != null) form.setDescription(description);
        if (pointsReward != null) form.setPointsReward(pointsReward);
        formRepo.save(form);
        return toDto(form, null);
    }

    // ─── 開啟/關閉表單 ────────────────────────────────────────────

    @Transactional
    public FeedbackFormDto toggleForm(Long formId) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));
        form.setIsOpen(!form.getIsOpen());
        formRepo.save(form);
        return toDto(form, null);
    }

    // ─── 題目 CRUD ────────────────────────────────────────────────

    @Transactional
    public FeedbackFormDto addQuestion(Long formId, String questionText, QuestionType questionType,
                                       Boolean isRequired, Integer maxRating, List<String> optionTexts) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));

        List<FeedbackQuestion> existing = questionRepo.findByFormIdOrderByOrderIndex(formId);
        int nextIndex = existing.size();

        FeedbackQuestion q = new FeedbackQuestion();
        q.setFormId(formId);
        q.setQuestionText(questionText);
        q.setQuestionType(questionType);
        q.setIsRequired(isRequired != null ? isRequired : false);
        q.setMaxRating(maxRating != null ? maxRating : 10);
        q.setOrderIndex(nextIndex);
        questionRepo.save(q);

        if ((questionType == QuestionType.SINGLE_CHOICE || questionType == QuestionType.MULTIPLE_CHOICE)
                && optionTexts != null) {
            for (int i = 0; i < optionTexts.size(); i++) {
                FeedbackQuestionOption opt = new FeedbackQuestionOption();
                opt.setQuestionId(q.getId());
                opt.setOptionText(optionTexts.get(i));
                opt.setOrderIndex(i);
                optionRepo.save(opt);
            }
        }
        return toDto(form, null);
    }

    @Transactional
    public FeedbackFormDto updateQuestion(Long formId, Long questionId, String questionText,
                                          Boolean isRequired, Integer maxRating, List<String> optionTexts) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));
        FeedbackQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new NoSuchElementException("找不到題目：" + questionId));

        if (questionText != null && !questionText.isBlank()) q.setQuestionText(questionText);
        if (isRequired != null) q.setIsRequired(isRequired);
        if (maxRating != null) q.setMaxRating(maxRating);
        questionRepo.save(q);

        if ((q.getQuestionType() == QuestionType.SINGLE_CHOICE || q.getQuestionType() == QuestionType.MULTIPLE_CHOICE)
                && optionTexts != null) {
            optionRepo.deleteByQuestionId(questionId);
            for (int i = 0; i < optionTexts.size(); i++) {
                FeedbackQuestionOption opt = new FeedbackQuestionOption();
                opt.setQuestionId(questionId);
                opt.setOptionText(optionTexts.get(i));
                opt.setOrderIndex(i);
                optionRepo.save(opt);
            }
        }
        return toDto(form, null);
    }

    @Transactional
    public FeedbackFormDto deleteQuestion(Long formId, Long questionId) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));
        optionRepo.deleteByQuestionId(questionId);
        questionRepo.deleteById(questionId);

        // 重新排序
        List<FeedbackQuestion> remaining = questionRepo.findByFormIdOrderByOrderIndex(formId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderIndex(i);
        }
        questionRepo.saveAll(remaining);
        return toDto(form, null);
    }

    // ─── 提交回饋 ─────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> submitFeedback(Long formId, FeedbackSubmitRequest request, String userId) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));

        if (!form.getIsOpen()) {
            throw new IllegalStateException("此表單目前未開放填寫");
        }

        // 已登入會員必須有簽到紀錄（status = ATTENDED）才能填寫
        if (userId != null) {
            // JWT subject 是 email，先找出 userId（用戶 ID）
            String registrationUserId = userRepo.findByEmail(userId)
                    .map(u -> u.getId())
                    .orElseThrow(() -> new NoSuchElementException("找不到使用者"));

            boolean hasAttended = registrationRepo
                    .findByActivityIdAndUserId(form.getActivityId(), registrationUserId)
                    .map(r -> r.getStatus() == RegistrationStatus.ATTENDED)
                    .orElse(false);

            if (!hasAttended) {
                throw new IllegalStateException("僅限已簽到的會員填寫此回饋表單");
            }

            if (responseRepo.existsByFormIdAndUserId(formId, userId)) {
                throw new IllegalStateException("您已填寫過此表單");
            }
        }

        FeedbackResponse response = new FeedbackResponse();
        response.setFormId(formId);
        response.setUserId(userId); // null = 匿名
        responseRepo.save(response);

        if (request.getAnswers() != null) {
            for (FeedbackSubmitRequest.AnswerRequest ar : request.getAnswers()) {
                FeedbackAnswer answer = new FeedbackAnswer();
                answer.setResponseId(response.getId());
                answer.setQuestionId(ar.getQuestionId());
                answer.setTextAnswer(ar.getTextAnswer());
                answer.setRatingAnswer(ar.getRatingAnswer());
                if (ar.getSelectedOptionIds() != null && !ar.getSelectedOptionIds().isEmpty()) {
                    answer.setSelectedOptionIds(
                        ar.getSelectedOptionIds().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))
                    );
                }
                answerRepo.save(answer);
            }
        }

        int pointsAwarded = 0;
        if (userId != null) {
            // JWT subject 是 email，用 findByEmail 查詢
            userRepo.findByEmail(userId).ifPresent(user -> {
                user.setCreditPoints(user.getCreditPoints() + form.getPointsReward());
                userRepo.save(user);
            });
            pointsAwarded = form.getPointsReward();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "感謝您的回饋！");
        result.put("pointsAwarded", pointsAwarded);
        return result;
    }

    // ─── 統計摘要 ─────────────────────────────────────────────────

    public FeedbackStatsDto getStats(Long formId) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new NoSuchElementException("找不到表單：" + formId));

        List<FeedbackResponse> responses = responseRepo.findByFormId(formId);
        long totalResponses = responses.size();
        List<Long> responseIds = responses.stream().map(FeedbackResponse::getId).toList();

        List<FeedbackQuestion> questions = questionRepo.findByFormIdOrderByOrderIndex(formId);

        List<FeedbackStatsDto.QuestionStats> statsList = new ArrayList<>();
        for (FeedbackQuestion q : questions) {
            List<FeedbackAnswer> answers = answerRepo.findByQuestionId(q.getId())
                    .stream()
                    .filter(a -> responseIds.contains(a.getResponseId()))
                    .toList();

            FeedbackStatsDto.QuestionStats qs = new FeedbackStatsDto.QuestionStats();
            qs.setQuestionId(q.getId());
            qs.setQuestionText(q.getQuestionText());
            qs.setQuestionType(q.getQuestionType());
            qs.setAnsweredCount((long) answers.size());

            switch (q.getQuestionType()) {
                case RATING -> {
                    List<Integer> ratings = answers.stream()
                            .map(FeedbackAnswer::getRatingAnswer)
                            .filter(Objects::nonNull)
                            .toList();
                    double avg = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    qs.setAverageRating(Math.round(avg * 10.0) / 10.0);

                    Map<Integer, Long> dist = new TreeMap<>();
                    for (int i = 1; i <= q.getMaxRating(); i++) dist.put(i, 0L);
                    ratings.forEach(r -> dist.merge(r, 1L, Long::sum));
                    qs.setRatingDistribution(dist);
                }
                case SINGLE_CHOICE, MULTIPLE_CHOICE -> {
                    List<FeedbackQuestionOption> opts = optionRepo.findByQuestionIdOrderByOrderIndex(q.getId());
                    Map<Long, Long> countMap = new HashMap<>();
                    opts.forEach(o -> countMap.put(o.getId(), 0L));

                    answers.forEach(a -> {
                        if (a.getSelectedOptionIds() != null) {
                            Arrays.stream(a.getSelectedOptionIds().split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .forEach(s -> {
                                        try {
                                            Long optId = Long.parseLong(s);
                                            countMap.merge(optId, 1L, Long::sum);
                                        } catch (NumberFormatException ignored) {}
                                    });
                        }
                    });

                    long totalSelections = answers.size();
                    List<FeedbackStatsDto.OptionCount> optCounts = opts.stream().map(o -> {
                        FeedbackStatsDto.OptionCount oc = new FeedbackStatsDto.OptionCount();
                        oc.setOptionId(o.getId());
                        oc.setOptionText(o.getOptionText());
                        long cnt = countMap.getOrDefault(o.getId(), 0L);
                        oc.setCount(cnt);
                        oc.setPercentage(totalSelections > 0 ? Math.round(cnt * 1000.0 / totalSelections) / 10.0 : 0.0);
                        return oc;
                    }).toList();
                    qs.setOptionCounts(optCounts);
                }
                case TEXT -> {
                    List<String> texts = answers.stream()
                            .map(FeedbackAnswer::getTextAnswer)
                            .filter(t -> t != null && !t.isBlank())
                            .toList();
                    qs.setTextAnswers(texts);
                }
            }
            statsList.add(qs);
        }

        FeedbackStatsDto dto = new FeedbackStatsDto();
        dto.setFormId(formId);
        dto.setFormTitle(form.getTitle());
        dto.setTotalResponses(totalResponses);
        dto.setQuestions(statsList);
        return dto;
    }

    // ─── 全體活動綜合滿意度 ───────────────────────────────────────

    public Map<String, Object> getOverallSatisfaction() {
        List<FeedbackQuestion> ratingQuestions = questionRepo.findAll().stream()
                .filter(q -> q.getQuestionType() == QuestionType.RATING)
                .toList();

        List<Integer> allRatings = ratingQuestions.stream()
                .flatMap(q -> answerRepo.findByQuestionId(q.getId()).stream())
                .map(FeedbackAnswer::getRatingAnswer)
                .filter(Objects::nonNull)
                .toList();

        double avg = allRatings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double score = Math.round(avg * 10.0) / 10.0;

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("totalRatings", allRatings.size());
        return result;
    }

    // ─── 私有轉換方法 ─────────────────────────────────────────────

    private FeedbackFormDto toDto(FeedbackForm form, String currentUserId) {
        FeedbackFormDto dto = new FeedbackFormDto();
        dto.setId(form.getId());
        dto.setActivityId(form.getActivityId());
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setIsOpen(form.getIsOpen());
        dto.setPointsReward(form.getPointsReward());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());
        dto.setResponseCount(responseRepo.countByFormId(form.getId()));

        activityRepo.findById(form.getActivityId())
                .ifPresent(a -> dto.setActivityTitle(a.getTitle()));

        if (currentUserId != null) {
            dto.setHasSubmitted(responseRepo.existsByFormIdAndUserId(form.getId(), currentUserId));
        }

        List<FeedbackQuestion> questions = questionRepo.findByFormIdOrderByOrderIndex(form.getId());
        List<FeedbackFormDto.QuestionDto> questionDtos = questions.stream().map(q -> {
            FeedbackFormDto.QuestionDto qd = new FeedbackFormDto.QuestionDto();
            qd.setId(q.getId());
            qd.setQuestionText(q.getQuestionText());
            qd.setQuestionType(q.getQuestionType());
            qd.setIsRequired(q.getIsRequired());
            qd.setMaxRating(q.getMaxRating());
            qd.setOrderIndex(q.getOrderIndex());

            if (q.getQuestionType() == QuestionType.SINGLE_CHOICE
                    || q.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                List<FeedbackQuestionOption> opts = optionRepo.findByQuestionIdOrderByOrderIndex(q.getId());
                List<FeedbackFormDto.OptionDto> optDtos = opts.stream().map(o -> {
                    FeedbackFormDto.OptionDto od = new FeedbackFormDto.OptionDto();
                    od.setId(o.getId());
                    od.setOptionText(o.getOptionText());
                    od.setOrderIndex(o.getOrderIndex());
                    return od;
                }).toList();
                qd.setOptions(optDtos);
            }
            return qd;
        }).toList();
        dto.setQuestions(questionDtos);
        return dto;
    }
}
