package com.xianchen.dwxconnect.handler;

import com.alibaba.fastjson.JSONObject;
import com.xianchen.dwxconnect.client.Client;

/**
 * @author xianchen-bot
 */
public interface EventHandler {
    
    void start(Client dwx);
    
    void onTick(Client dwx, String symbol, double bid, double ask);
    
    void onBarData(Client dwx, String symbol, String timeFrame, String time, double open, double high, double low, double close, int tickVolume);
    
	void onHistoricData(Client dwx, String symbol, String timeFrame, JSONObject data);
	
	void onHistoricTrades(Client dwx);
	
	void onMessage(Client dwx, JSONObject message);
    
    void onOrderEvent(Client dwx);
    
}