package com.xianchen.dwxconnect.entity;

import lombok.Data;

/**
 * @author liaozhongxiang
 */
@Data
public class AccountInfo {
    private int number = 0;
    private int leverage = 1;
    private double balance = 0.000001d;
    private double free_margin = 0.000001d;
    private String name = "";
    private String currency = "USD";
    private double equity = 0.000001d;
}
