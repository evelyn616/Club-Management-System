package com.danceclub.club_system.model.enums;
/*繳費狀態列舉

 */
public enum PaymentStatus {
    NOT_REQUIRED("無須繳費"),
    PENDING("待繳費"),
    PAID("已繳費");

    /*暫存中文顯示
    */
    private final String displayname;

    /*建構子
     */
    PaymentStatus(String displayname){
        this.displayname = displayname;
    }

    /*顯示中文方法
    */
    public String getDisplayname(){return displayname;}

}
