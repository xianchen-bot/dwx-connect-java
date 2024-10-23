package com.xianchen.dwxconnect.controller;

import com.alibaba.fastjson.JSON;
import com.xianchen.dwxconnect.requests.OpenOrderRequest;
import com.xianchen.dwxconnect.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xianchen-bot
 */
@Slf4j
@RestController
public class DwxConnectController {

    @Resource
    private ClientService clientService;

    @GetMapping(value = "/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/open_order")
    public String openOrder(@RequestBody String request) {
        //log.info("controller#openOrder with request: {}", request);
        if (StringUtils.isEmpty(request)) {
            return "request can not be empty.";
        }
        OpenOrderRequest openOrderRequest = JSON.parseObject(request, OpenOrderRequest.class);
        clientService.openOrder(openOrderRequest);

        return "success";
    }

}
