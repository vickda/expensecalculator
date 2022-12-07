package com.example.expensecalculator;

public class Statement {

    String date, paymentType, category, money;

    Statement(String date, String paymentType, String category, String money){
        this.date = date;
        this.paymentType = paymentType;
        this.category = category;
        this.money = money;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
