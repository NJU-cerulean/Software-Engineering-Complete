package com.marketplace.models;

import java.util.LinkedList;
import java.util.Queue;

public class Admin {
    private String adminId;
    private String username;
    private final Queue<Complaint> complaintNeedHandle = new LinkedList<>();

    public Admin(String adminId, String username) {
        this.adminId = adminId;
        this.username = username;
    }

    public void manageUsers() {}
    public void manageMerchants() {}
    public void viewReports() {}
    public void banUser(String userPhone) { /* call DAO */ }
    public void unbanUser(String userPhone) { }
    public void banMerchant(String merchantPhone) { }
    public void unbanMerchant(String merchantPhone) { }
}
