package com.xianchen.dwxconnect.requests;

import lombok.Data;

/**
 * @author xianchen
 */
@Data
public class OpenOrderRequest {

    private String symbol;
    private String orderType;
    private double lots = 0d;
    private double price = 0d;
    private double stopLoss = 0d;
    private double takeProfit = 0d;
    private int magic = 0;
    private String comment = "";
    private long expiration = 0L;

    private double rpt = 0d;

}
