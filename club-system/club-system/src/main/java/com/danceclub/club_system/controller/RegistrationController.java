package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.CreateRegistrationRequest;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.service.RegistrationService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService){
        this.registrationService = registrationService;
    }
    //====會員端API====//
    //====1.建立報名====//
    //POST /api/registrations
    /**
     * 建立報名
     *
     * @param request 建立報名請求
     * @return 報名紀錄
     */
    @PostMapping
    public ResponseEntity<Registration> createRegistration(
            @Valid @RequestBody CreateRegistrationRequest request
            ){

        Registration registration = registrationService.createRegistration(
                request.getActivityId(),
                request.getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    //====2.取消報名====//
    //DELETE /api/registrations/{id}
    /**
     * 取消報名
     * @param id 報名id
     * @return 200 OK+取消報名紀錄
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Registration> cancelRegistration(
            @PathVariable Long id
    ){
        Registration registration = registrationService.cancelRegistration(id);
        return ResponseEntity.ok(registration);
    }

    //====查詢報名紀錄====//
    //GET /api/registrations/my?userId=M001

    /**
     * 查詢會員報名紀錄
     * @param userId 會員ID
     * @return 會員的報名紀錄表+200 ok
     */
    @GetMapping("/my")
    public ResponseEntity<List<Registration>> getMyRegistrations(@RequestParam String userId){
        List<Registration> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    //====查詢待繳費活動====//
    //GET /api/registrations/pending-payments?userId=M001

    /**
     * 待繳費活動列表
     * @param userId 會員ID
     * @return 待繳費列表+200 ok
     */

    @GetMapping("/pending-payments")
    public ResponseEntity<List<Registration>> getPendingPayments(@RequestParam String userId){
        List<Registration> registrations = registrationService.getPendingPayments(userId);
        return ResponseEntity.ok(registrations);
    }

    //====更新繳費狀態====//
    //PUT /api/registrations/{id}/payment

    /**
     * 更新繳費狀態
     * @param id 報名ID
     * @return 200 OK +更新到已繳款
     */

    @PutMapping("/{id}/payment")
    public ResponseEntity<Registration> updatePaymentStatus(@PathVariable Long id){
        Registration registration = registrationService.updatePaymentStatus(id);
        return ResponseEntity.ok(registration);
    }

    //====簽到====//
    //PUT /api/registrations/{id}/checkin

    /**
     * 簽到
     * @param id 報名ID
     * @return 200 ok+更改報名狀態到出席狀態
     */
    @PutMapping("/{id}/checkin")
    public ResponseEntity<Registration> checkIn(@PathVariable Long id){
        Registration registration = registrationService.checkIn(id);
        return ResponseEntity.ok(registration);
    }

    //====管理端API====//
    //====查詢活動的報名名單====//
    //GET /api/registrations/activity/{activityId}
    /**
     * 查詢活動報名名單
     * @param activityId 活動ID
     * @return 200 OK+某活動報名名單
     */

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<Registration>> getActivityRegistrations(@PathVariable() Long activityId){
        List<Registration> registrations = registrationService.getActivityRegistration(activityId);
        return ResponseEntity.ok(registrations);
    }

    //====統計報名人數====//
    //GET /api/registrations/activity/{activityId}/count
    /**
     * 統計單活動參加人數
     * @param activityId 活動ID
     * @return 200 OK+總報名人數
     */
    @GetMapping("/activity/{activityId}/count")
    public ResponseEntity<Long> countRegistrations(@PathVariable Long activityId){
        Long registrations = registrationService.countValidRegistrations(activityId);
        return ResponseEntity.ok(registrations);
    }

    //====統計簽到人數====//
    //GET /api/registrations/activity/{activityId}/checkin-count
    /**
     * 統計活動簽到人數
     * @param activityId 活動ID
     * @return 200 OK+單活動總簽到人數
     */

    @GetMapping("/activity/{activityId}/checkin-count")
    public ResponseEntity<Long> countCheckedIn(@PathVariable Long activityId){
        Long registrations = registrationService.countCheckedIn(activityId);
        return ResponseEntity.ok(registrations);
    }
}

