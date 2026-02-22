package com.danceclub.club_system.controller;

import com.danceclub.club_system.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    @Autowired
    private ExportService exportService;

    /**
     * 匯出活動表單(Excel)
     */

    @GetMapping("/activities/{activityId}/registrations/excel")
    public ResponseEntity<byte[]> exportRegistrationsToExcel(@PathVariable Long activityId){
        try {
            byte[] excelData = exportService.exportRegistrationsToExcel(activityId);

            String filename = URLEncoder.encode("報名表單"+ activityId + ".xlsx", StandardCharsets.UTF_8.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",filename);
            headers.setContentLength(excelData.length);

            return ResponseEntity.ok().headers(headers).body(excelData);
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 匯出活動簽到表(Excel)
     */
    @GetMapping("/activities/{activityId}/check-in-sheet/excel")
    public ResponseEntity<byte[]> exportCheckInSheetToExcel(@PathVariable Long activityId){
        try {
            byte[]  excelData = exportService.exportCheckInSheetToExcel(activityId);

            String filename = URLEncoder.encode("簽到表"+ activityId + ".xlsx", StandardCharsets.UTF_8.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",filename);
            headers.setContentLength(excelData.length);

            return ResponseEntity.ok().headers(headers).body(excelData);
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
