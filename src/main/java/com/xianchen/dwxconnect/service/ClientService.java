package com.xianchen.dwxconnect.service;

import com.xianchen.dwxconnect.client.Client;
import com.xianchen.dwxconnect.requests.OpenOrderRequest;
import com.xianchen.dwxconnect.handler.DefaultEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author xianchen-bot
 */
@Slf4j
@Service
public class ClientService {

    // 这项目前如果走配置的话，会提示找不到目录，先代码里写死吧
//    @Value("${meta_trader_dir_path}")
    private final static String MEAT_TRADE_DIR_PATH = "/Users/admin/Documents/dev/dwx-connect-java/MQL4/Files/";

    @Value("${sleep_delay}")
    private int sleepDelayMs;

    @Value("${max_retry_command_seconds}")
    private int maxRetryCommandSeconds;

    @Value("${load_orders_from_file}")
    private boolean loadOrdersFormFile;

    @Value("${verbose}")
    private boolean verbose;

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
            client.openOrder(openOrderRequest.getSymbol(), openOrderRequest.getOrderType(), openOrderRequest.getLots(),
                    openOrderRequest.getPrice(), openOrderRequest.getStopLoss(), openOrderRequest.getTakeProfit(),
                    openOrderRequest.getMagic(), openOrderRequest.getComment(), openOrderRequest.getExpiration());
            log.info("openOrder finished.");
        } catch (Exception e) {
            log.error("openOrder failed.", e);
        }
    }

}
