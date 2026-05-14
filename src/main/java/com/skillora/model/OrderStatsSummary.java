package com.skillora.model;

public class OrderStatsSummary {
    private int orderCount;
    private double totalAmount;
    private int enAttente;
    private int payee;
    private int annulee;

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getEnAttente() {
        return enAttente;
    }

    public void setEnAttente(int enAttente) {
        this.enAttente = enAttente;
    }

    public int getPayee() {
        return payee;
    }

    public void setPayee(int payee) {
        this.payee = payee;
    }

    public int getAnnulee() {
        return annulee;
    }

    public void setAnnulee(int annulee) {
        this.annulee = annulee;
    }
}
