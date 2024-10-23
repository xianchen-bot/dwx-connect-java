package com.xianchen.dwxconnect.service;

import com.alibaba.fastjson.JSON;
import com.xianchen.dwxconnect.client.Client;
import com.xianchen.dwxconnect.entity.AccountInfo;
import com.xianchen.dwxconnect.requests.OpenOrderRequest;
import com.xianchen.dwxconnect.handler.DefaultEventHandler;
import com.xianchen.dwxconnect.utils.Helpers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author xianchen-bot
 */
@Slf4j
@Service
public class ClientService {

    // 这项目前如果走配置的话，会提示找不到目录，先代码里写死吧
//    @Value("${meta_trader_dir_path}")
    private final static String MEAT_TRADE_DIR_PATH = "C:\\Users\\Administrator\\AppData\\Roaming\\MetaQuotes\\Terminal\\5D49F47D1EA1ECFC0DDC965B6D100AC5\\MQL4\\Files";

    @Value("${sleep_delay}")
    private int sleepDelayMs;

    @Value("${max_retry_command_seconds}")
    private int maxRetryCommandSeconds;

    @Value("${load_orders_from_file}")
    private boolean loadOrdersFormFile;

    @Value("${verbose}")
    private boolean verbose;

    @Value("${floatMoney}")
    private double floatMoney;

    @Value("${k}")
    private double k;

    private Client client = null;

    @Resource
    private DefaultEventHandler defaultEventHandler;

    @PostConstruct
    public void init() {
        // 在项目启动后执行的代码
        try {
            client = new Client(defaultEventHandler, MEAT_TRADE_DIR_PATH, sleepDelayMs, maxRetryCommandSeconds, loadOrdersFormFile, verbose);
        } catch (Exception e) {
            log.error("Service initialize failed.", e);
        }
        log.info("Service initialized!");
    }

    public void openOrder(OpenOrderRequest openOrderRequest) {
        if (Objects.isNull(client)) {
            log.error("client is null. can not execute.");
            return;
        }
        try {
            if (StringUtils.isEmpty(client.accountInfo)) {
                log.error( "accountInfo is empty.");
            }
            AccountInfo accountInfo = JSON.parseObject(String.valueOf(client.accountInfo), AccountInfo.class);
            double rpt = openOrderRequest.getRpt();
            if(rpt >= 1) rpt /= 100;
            //计算手数：手数 = (资金 * rpt * 0.01) / (单位浮动金额 * k * 价差)， 其中，k = ticks / 价差
            double lots = (accountInfo.getBalance() * rpt * 0.01)/(floatMoney * k * Math.abs(openOrderRequest.getPrice() - openOrderRequest.getStopLoss()));
            client.openOrder(openOrderRequest.getSymbol(), openOrderRequest.getOrderType(), lots,
                    openOrderRequest.getPrice(), openOrderRequest.getStopLoss(), openOrderRequest.getTakeProfit(),
                    openOrderRequest.getMagic(), openOrderRequest.getComment(), openOrderRequest.getExpiration());
            // log.info("openOrder finished.");
        } catch (Exception e) {
            log.error("openOrder failed.", e);
        }
    }

    public String accountInfo(){
        if (Objects.isNull(client)) {
            log.error("client is null. can not execute.");
            return "client is null";
        }
        try {
            return client.accountInfo.toString();
//            Helpers.print("\nAccount info:\n" + client.accountInfo + "\n");
        } catch (Exception e) {
            log.error("openOrder failed.", e);
        }
        return "error";
    }

}
