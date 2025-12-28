package com.example.demo.controller;

import com.example.demo.entity.LeaveEntity;
import com.example.demo.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveController {

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // --- 1. GET: 取得所有請假單 ---
    @GetMapping("/all")
    public ResponseEntity<List<LeaveEntity>> getAllLeaves() {
        return new ResponseEntity<>(this.leaveService.getAllLeaves(), HttpStatus.OK);
    }

    // --- 2. INSERT: 新增請假單 ---
    @PostMapping("/add")
    public ResponseEntity<LeaveEntity> createLeave(@RequestBody LeaveEntity leaveEntity) {
        LeaveEntity newLeave = leaveService.submitNewRequest(leaveEntity);
        return new ResponseEntity<>(newLeave, HttpStatus.CREATED);
    }

    // --- 3. ALTER/UPDATE: 修改請假單狀態 ---
    // 這裡我們將名稱改為 reviewRequest 以對應你的 Service
    @PutMapping("/review/{id}")
    public ResponseEntity<LeaveEntity> reviewLeave(
            @PathVariable Long id, 
            @RequestParam String status) {
        
        // 注意：因為你的 Service 裡的 reviewRequest 需要審核人和備註
        // 測試初期，我們可以先在 Service 裡寫一個簡化版的更新，或者在這裡傳 null
        return leaveService.reviewRequest(id, null, status, "System Auto-updated")
                .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- 4. DELETE: 刪除請假單 ---
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        try {
            leaveService.deleteRequest(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
