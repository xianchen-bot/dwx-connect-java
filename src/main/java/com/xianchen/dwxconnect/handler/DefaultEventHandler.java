package com.xianchen.dwxconnect.handler;

import com.alibaba.fastjson.JSONObject;
import com.xianchen.dwxconnect.client.Client;
import com.xianchen.dwxconnect.utils.Helpers;
import org.springframework.stereotype.Service;

/**
 * @author xianchen-bot
 */
@Service
public class DefaultEventHandler implements EventHandler {

    @Override
    public void start(Client dwx) {
        // account information is stored in dwx.accountInfo.
        Helpers.print("\nAccount info:\n" + dwx.accountInfo + "\n");
/*
        // subscribe to tick data:
        String[] symbols = {"EURUSD", "GBPUSD"};
        dwx.subscribeSymbols(symbols);

        // subscribe to bar data:
        String[][] symbolsBarData = {{"EURUSD", "M1"}, {"AUDCAD", "M5"}, {"GBPCAD", "M15"}};
        dwx.subscribeSymbolsBarData(symbolsBarData);

        // request historic data:
        long end = System.currentTimeMillis()/1000;
        long start = end - 10*24*60*60;  // last 10 days
        dwx.getHistoricData("AUDCAD", "D1", start, end);
*/
        // dwx.closeOrdersByMagic(77);
        // sleep(2000);
    }

    /**
     * use synchronized so that price updates and execution updates are not processed one after the other.
     */
    @Override
    public synchronized void onTick(Client dwx, String symbol, double bid, double ask) {
        Helpers.print("onTick: " + symbol + " | bid: " + bid + " | ask: " + ask);
    }

    @Override
    public synchronized void onBarData(Client dwx, String symbol, String timeFrame, String time, double open, double high, double low, double close, int tickVolume) {
        Helpers.print("onBarData: " + symbol + ", " + timeFrame + ", " + time + ", " + open + ", " + high + ", " + low + ", " + close + ", " + tickVolume);
    }

    @Override
    public synchronized void onHistoricData(Client dwx, String symbol, String timeFrame, JSONObject data) {
        // you can also access historic data via: dwx.historicData
        Helpers.print("onHistoricData: " + symbol + ", " + timeFrame + ", " + data);
    }

    @Override
    public synchronized void onHistoricTrades(Client dwx) {
        Helpers.print("onHistoricTrades: " + dwx.historicTrades);
    }

    @Override
    public synchronized void onMessage(Client dwx, JSONObject message) {
        if ("ERROR".equals(message.get("type"))) {
            Helpers.print(message.get("type") + " | " + message.get("error_type") + " | " + message.get("description"));
        } else if ("INFO".equals(message.get("type"))) {
            Helpers.print(message.get("type") + " | " + message.get("message"));
        }
    }

    /**
     * triggers when an order is added or removed, not when only modified.
     */
    @Override
    public synchronized void onOrderEvent(Client dwx) {
        Helpers.print("onOrderEvent:");

        // dwx.openOrders is a JSONObject, which can be accessed like this:
        for (String ticket : dwx.openOrders.keySet()) {
            Helpers.print(ticket + ": " + dwx.openOrders.get(ticket));
        }
    }

}
