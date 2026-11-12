package com.scc.model;

public class Payment {
    private int paymentId;
    private int impoundmentId;
    private double paymentAmount;
    private String paymentDate;
    private String paymentMethod; // Cash, Bank Deposit, Over-the-Counter
    private String paymentStatus; // Paid, Pending

    public Payment() {}

    public Payment(int paymentId, int impoundmentId, double paymentAmount, String paymentDate,
                   String paymentMethod, String paymentStatus) {
        this.paymentId = paymentId;
        this.impoundmentId = impoundmentId;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getImpoundmentId() { return impoundmentId; }
    public void setImpoundmentId(int impoundmentId) { this.impoundmentId = impoundmentId; }

    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}