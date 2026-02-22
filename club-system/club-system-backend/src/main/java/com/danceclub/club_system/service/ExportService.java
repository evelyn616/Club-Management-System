package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.RegistrationWithUserDTO;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



@Service
public class ExportService {

    @Autowired
    private RegistrationService registrationService;

    @Autowired ActivityService activityService;

    /**
     * 匯出活動報名表單為Excel
     */

    public byte[] exportRegistrationsToExcel(Long activityId){
        try{
            Activity activity = activityService.getActivityById(activityId);
            List<RegistrationWithUserDTO> registrations = registrationService.getActivityRegistrations(activityId);

            //建立Excel的Workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("報名表單");

            //建立表單樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDataStyle(workbook);

            //建立表頭
            int rowNum =0;

            //活動資訊
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("活動名稱"+ activity.getTitle());

            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("活動時間"+activity.getStartTime());

            Row locationRow = sheet.createRow(rowNum++);
            locationRow.createCell(0).setCellValue("活動地點"+(activity.getLocation() != null ? activity.getLocation(): ""));

            //統計資訊
            long totalCount = registrations.size();
            long paidCount = registrations.stream().filter(r -> PaymentStatus.PAID.equals(r.getPaymentStatus())).count();
            long pendingCount = registrations.stream().filter(r -> PaymentStatus.PENDING.equals(r.getPaymentStatus())).count();

            Row statsRow = sheet.createRow(rowNum++);
            statsRow.createCell(0).setCellValue(String.format("報名統計：總計 %d 人 | 已繳費 %d 人 | 待繳費 %d 人", totalCount,paidCount,pendingCount));

            //空一行
            rowNum++;

            //欄位標題
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"編號", "會員ID", "會員姓名", "報名時間", "報名狀態" ,"繳費狀態"};

            for(int i = 0; i < headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            //填入資料
            int index = 1;
            for (RegistrationWithUserDTO registration : registrations){
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(registration.getUserId());
                row.createCell(2).setCellValue(registration.getUserName() != null ? registration.getUserName() : "未知");
                row.createCell(3).setCellValue(formatDateTime(registration.getRegistrationTime()));
                row.createCell(4).setCellValue(getStatusText(registration.getStatus()));
                row.createCell(5).setCellValue(getPaymentStatusText(registration.getPaymentStatus()));

                for (int i = 0; i<headers.length; i++){
                    row.getCell(i).setCellStyle(dateStyle);
                }
            }

            //自動調整欄位寬度
            for (int i = 0; i<headers.length; i++){
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i)+1000);
            }

            //轉為Byte[]
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        }catch (Exception e){
            throw new RuntimeException("匯出Excel表單失敗："+ e.getMessage(), e);
        }
    }

    /**
     * 匯出空白簽到表單
     */
    public byte[] exportCheckInSheetToExcel(Long activityId){
        try {
            Activity activity = activityService.getActivityById(activityId);
            List<RegistrationWithUserDTO> registrations = registrationService.getActivityRegistrations(activityId);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("簽到表");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;

            //活動資訊
            sheet.createRow(rowNum++).createCell(0).setCellValue("活動名稱"+ activity.getTitle());
            sheet.createRow(rowNum++).createCell(0).setCellValue("活動時間"+activity.getStartTime());
            sheet.createRow(rowNum++).createCell(0).setCellValue("活動地點"+(activity.getLocation() != null ? activity.getLocation(): ""));

            rowNum++;

            //欄位標題
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"編號", "會員ID", "姓名", "簽到時間","簽名"};

            for(int i = 0; i < headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            //填入資料(只把簽到時間以及簽到欄留白)

            int index = 1;
            for (RegistrationWithUserDTO registration : registrations){
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(registration.getUserId());
                row.createCell(2).setCellValue(registration.getUserName() != null ? registration.getUserName() : "未知");
                row.createCell(4).setCellValue("");//簽到時間
                row.createCell(5).setCellValue("");//簽名

                for (int i = 0; i<headers.length; i++){
                    if (row.getCell(i) == null) row.createCell(i);
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            //自動調整欄位寬度
            for (int i = 0; i<headers.length; i++){
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i)+1000);
            }
            //簽到時間以及簽名區加寬
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4,5000);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        }catch (Exception e){
            throw new RuntimeException("匯出簽到表失敗："+ e.getMessage(), e);
        }
    }

    /**
     * 建立標題樣式
     */
    private CellStyle createHeaderStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 建立資料欄樣式
     */
    private CellStyle createDataStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 格式化時間樣式
     */
    private String formatDateTime(LocalDateTime dateTime){
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * 取得報名狀態文字
     */
    private String getStatusText(RegistrationStatus status){
        if (status == null) return "";

        switch (status){
            case REGISTERED -> {
                return "已報名";
            }
            case CANCELLED -> {
                return "已取消";
            }
            case ABSENT -> {
                return "未出席";
            }
            case ATTENDED -> {
                return "已簽到";
            }
            default -> {
                return status.name();
            }
        }
    }

    /**
     * 取得繳費狀態文字
     */
    private String getPaymentStatusText(PaymentStatus paymentStatus){
        if (paymentStatus == null) return "";

        switch (paymentStatus){
            case PENDING -> {
                return "待繳費";
            }
            case PAID -> {
                return "已繳費";
            }
            case CANCELLED -> {
                return "已取消";
            }
            case NOT_REQUIRED -> {
                return "不須繳費";
            }
            case REFUNDED -> {
                return "已退費";
            }
            case PARTIAL_REFUNDED -> {
                return "已部分退費";
            }
            default -> {
                return paymentStatus.name();
            }
        }
    }
}
