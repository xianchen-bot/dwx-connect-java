package com.xianchen.dwxconnect.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xianchen.dwxconnect.handler.EventHandler;
import com.xianchen.dwxconnect.utils.Helpers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


/*Client class

This class includes all the functions for communication with MT4/MT5.


JSON jar from here:
https://mvnrepository.com/artifact/org.json/json
or:
https://github.com/stleary/JSON-java

*/

/**
 * @author xianchen-bot
 */
public class Client {

    private EventHandler eventHandler;
    private String MetaTraderDirPath;
    private int sleepDelay;
    private int maxRetryCommandSeconds;
    private boolean loadOrdersFromFile;
    private boolean verbose;
    
    private String pathOrders;
    private String pathMessages;
    private String pathMarketData;
    private String pathBarData;
    private String pathHistoricData;
	private String pathHistoricTrades;
    private String pathOrdersStored;
    private String pathMessagesStored;
    private String pathCommandsPrefix;
    
    private int maxCommandFiles = 20;
    private int commandID = 0;
    private long lastMessagesMillis = 0;
    private String lastOpenOrdersStr = "";
    private String lastMessagesStr = "";
    private String lastMarketDataStr = "";
    private String lastBarDataStr = "";
    private String lastHistoricDataStr = "";
	private String lastHistoricTradesStr = "";
    
    public JSONObject openOrders = new JSONObject();
	public JSONObject accountInfo = new JSONObject();
    public JSONObject marketData = new JSONObject();
    public JSONObject barData = new JSONObject();
    public JSONObject historicData = new JSONObject();
	public JSONObject historicTrades = new JSONObject();
    
    private JSONObject lastBarData = new JSONObject();
    private JSONObject lastMarketData = new JSONObject();
    
    public boolean ACTIVE = true;
    private boolean START = false;
    
	private Thread openOrdersThread;
    private Thread messageThread;
    private Thread marketDataThread;
    private Thread barDataThread;
    private Thread historicDataThread;
	

    public Client(EventHandler eventHandler, String MetaTraderDirPath, int sleepDelay, int maxRetryCommandSeconds, boolean loadOrdersFromFile, boolean verbose) throws Exception {
        
        this.eventHandler = eventHandler;
        this.MetaTraderDirPath = MetaTraderDirPath;
        this.sleepDelay = sleepDelay;
        this.maxRetryCommandSeconds = maxRetryCommandSeconds;
        this.loadOrdersFromFile = loadOrdersFromFile;
        this.verbose = verbose;   
        
        File f = new File(MetaTraderDirPath);
        if(!f.exists()) {
            Helpers.print("ERROR: MetaTraderDirPath does not exist!");
            System.exit(1);
        }

        Path filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Orders.txt");
        this.pathOrders = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Messages.txt");
        this.pathMessages = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Market_Data.txt");
        this.pathMarketData = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Bar_Data.txt");
        this.pathBarData = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Historic_Data.txt");
        this.pathHistoricData = filePath.toString();
		filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Historic_Trades.txt");
        this.pathHistoricTrades = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Orders_Stored.txt");
        this.pathOrdersStored = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Messages_Stored.txt");
        this.pathMessagesStored = filePath.toString();
        filePath = Paths.get(MetaTraderDirPath, "DWX", "DWX_Commands_");
        this.pathCommandsPrefix = filePath.toString();
        
        loadMessages();
        
        if (loadOrdersFromFile) {
            loadOrders();
        }
        
        // old way via runnable:
        // this.messageThread = new Thread(new CheckMessages());
        // this.messageThread.start();
        
        this.openOrdersThread = new Thread(() -> checkOpenOrders());
        this.openOrdersThread.start();
        
        this.messageThread = new Thread(() -> checkMessages());
        this.messageThread.start();
        
        this.marketDataThread = new Thread(() -> checkMarketData());
        this.marketDataThread.start();
        
        this.barDataThread = new Thread(() -> checkBarData());
        this.barDataThread.start();
		
		this.historicDataThread = new Thread(() -> checkHistoricData());
        this.historicDataThread.start();

        resetCommandIDs();
        
        // no need to wait. 
        if (eventHandler == null) {
            start();
        } else {
            Helpers.sleep(1000);
            start();
            eventHandler.start(this);
        }
    }
    
	
	/*START can be used to check if the client has been initialized.  
	*/
    public void start() {
        START = true;
    }
	
    
	/*Regularly checks the file for open orders and triggers
    the eventHandler.onOrderEvent() function.
	*/
    private void checkOpenOrders() {
        while (ACTIVE) {

            Helpers.sleep(sleepDelay);
            
            if (!START) {
                continue;
            }
            
            String text = Helpers.tryReadFile(pathOrders);
            
            if (text.length() == 0 || text.equals(lastOpenOrdersStr)) {
                continue;
            }
            
            lastOpenOrdersStr = text;
            
            // JSONParser parser = new JSONParser();
            JSONObject data;
            
            try {
                data = JSON.parseObject(text);
            } catch (Exception e) {
                 continue;
            }
            
            if (data == null) {
                continue;
            }
            
			JSONObject dataOrders = data.getJSONObject("orders");
            boolean newEvent = false;
            for (String ticket : openOrders.keySet()) {
                if (!dataOrders.containsKey(ticket)) {
                    newEvent = true;
                    if (verbose) {
                        Helpers.print("Order removed: " + openOrders.get(ticket));
                    }
                }
            }
			
            for (String ticket : dataOrders.keySet()) {
                if (!openOrders.containsKey(ticket)) {
                    newEvent = true;
                    if (verbose) {
                        Helpers.print("New order: " + dataOrders.get(ticket));
                    }
                }
            }
            
            openOrders = dataOrders;
			accountInfo = data.getJSONObject("account_info");
            
            if (loadOrdersFromFile) {
                Helpers.tryWriteToFile(pathOrdersStored, data.toString());
            }
            
            if (eventHandler != null && newEvent) {
                eventHandler.onOrderEvent(this);
            }
        }
    }
	
    
	/*Regularly checks the file for messages and triggers
    the eventHandler.onMessage() function.
	*/
    private void checkMessages() {
        while (ACTIVE) {

            Helpers.sleep(sleepDelay);
            
            if (!START) {
                continue;
            }
            
            String text = Helpers.tryReadFile(pathMessages);
            
            if (text.length() == 0 || text.equals(lastMessagesStr)) {
                continue;
            }
            
            lastMessagesStr = text;
            
            JSONObject data;
            
            try {
                data = JSON.parseObject(text);
            } catch (Exception e) {
                 continue;
            }
            
            if (data == null) {
                continue;
            }
            
			// the objects are not ordered. because of (millis > lastMessagesMillis) it would miss messages if we just looped through them directly. 
			ArrayList<String> millisList = new ArrayList<>();
            for (String millisStr : data.keySet()) {
                if (data.get(millisStr) != null) {
                    millisList.add(millisStr);
                }
            }
			Collections.sort(millisList);
			for (String millisStr : millisList) {
				if (data.get(millisStr) != null) {
					long millis = Long.parseLong(millisStr);
					if (millis > lastMessagesMillis) {
						lastMessagesMillis = millis;
						if (eventHandler != null) {
                            eventHandler.onMessage(this, data.getJSONObject(millisStr));
                        }
					}
				}
			}
            Helpers.tryWriteToFile(pathMessagesStored, data.toString());
        }
    }
	
    
	/*Regularly checks the file for market data and triggers
    the eventHandler.onTick() function.
	*/
    private void checkMarketData() {
        while (ACTIVE) {
            
            Helpers.sleep(sleepDelay);
            
            if (!START) {
                continue;
            }
            
            String text = Helpers.tryReadFile(pathMarketData);
            
            if (text.length() == 0 || text.equals(lastMarketDataStr)) {
                continue;
            }
            
            lastMarketDataStr = text;
            
            JSONObject data;
            
            try {
                data = JSON.parseObject(text);
            } catch (Exception e) {
                 continue;
            }
            
            if (data == null) {
                continue;
            }
            
            marketData = data;
            
            if (eventHandler != null) {
                for (String symbol : marketData.keySet()) {
                    
                    if (!lastMarketData.containsKey(symbol) || !marketData.get(symbol).equals(lastMarketData.get(symbol))) {
                        JSONObject jo = marketData.getJSONObject(symbol);
                        eventHandler.onTick(this, symbol, jo.getString("bid"), jo.getString("ask"));
                    }
                }
            }
            lastMarketData = data;
        }
    }
	

	/*Regularly checks the file for bar data and triggers
    the eventHandler.onBarData() function.
	*/
    private void checkBarData() {
        
        while (ACTIVE) {

            Helpers.sleep(sleepDelay);
            
            if (!START) {
                continue;
            }
            
            String text = Helpers.tryReadFile(pathBarData);
            
            if (text.length() == 0 || text.equals(lastBarDataStr)) {
                continue;
            }
            
            lastBarDataStr = text;
            
            JSONObject data;
            
            try {
                data = JSON.parseObject(text);
            } catch (Exception e) {
                 continue;
            }
            
            if (data == null) {
                continue;
            }
            
            barData = data;
            
            if (eventHandler != null) {
                for (String st : barData.keySet()) {
                    
                    if (!lastBarData.containsKey(st) || !barData.get(st).equals(lastBarData.get(st))) {
                        String[] stSplit = st.split("_");
                        if (stSplit.length != 2) {
                            continue;
                        }
                        JSONObject jo = barData.getJSONObject(st);
                        eventHandler.onBarData(this, stSplit[0], stSplit[1], jo.getString("time"), jo.getString("open"), jo.getString("high"), jo.getString("low"), jo.getString("close"), jo.getString("tick_volume"));
                    }
                }
            }
            lastBarData = data;
        }
    }
	
    
	/*Regularly checks the file for historic data and triggers
    the eventHandler.onHistoricData() function.
	*/
    private void checkHistoricData() {
        
		while (ACTIVE) {

            Helpers.sleep(sleepDelay);
            
            if (!START) {
                continue;
            }
            
            String text = Helpers.tryReadFile(pathHistoricData);
            
            if (text.length() > 0 && !text.equals(lastHistoricDataStr)) {
            
				lastHistoricDataStr = text;
				
				JSONObject data;
				
				try {
					data = JSON.parseObject(text);
				} catch (Exception e) {
					 data = null;
				}
				
				if (data != null) {
				
					for (String st : data.keySet()) {
						historicData.put(st, data.get(st));
					}

                    Helpers.tryDeleteFile(pathHistoricData);
					
					if (eventHandler != null) {
						for (String st : data.keySet()) {
							String[] stSplit = st.split("_");
							if (stSplit.length != 2) {
                                continue;
                            }
							eventHandler.onHistoricData(this, stSplit[0], stSplit[1], data.getJSONObject(st));
						}
					}
				}
			}
			
			// also check historic trades in the same thread. 
			text = Helpers.tryReadFile(pathHistoricTrades);
			
			if (text.length() > 0 && !text.equals(lastHistoricTradesStr)) {
            
				lastHistoricTradesStr = text;
				
				JSONObject data;
				
				try {
					data = JSON.parseObject(text);
				} catch (Exception e) {
					 data = null;
				}
				
				if (data != null) {
					historicTrades = data;
					
					if (eventHandler != null) {
                        eventHandler.onHistoricTrades(this);
                    }
				}
			}
        }
    }
	
    
	/*Loads stored orders from file (in case of a restart). 
	*/
    private void loadOrders() {
        
        String text = Helpers.tryReadFile(pathOrdersStored);
        
        if (text.length() == 0) {
            return;
        }
        
        JSONObject data;
        
        try {
            data = JSON.parseObject(text);
        } catch (Exception e) {
             return;
        }
        
        if (data == null) {
            return;
        }
        
        lastOpenOrdersStr = text;
		openOrders = data.getJSONObject("orders");
		accountInfo = data.getJSONObject("account_info");
    }
	
    
	/*Loads stored messages from file (in case of a restart). 
	*/
    private void loadMessages() {
        
        String text = Helpers.tryReadFile(pathMessagesStored);
        
        if (text.length() == 0) {
            return;
        }
        
        JSONObject data;
        
        try {
            data = JSON.parseObject(text);
        } catch (Exception e) {
             return;
        }
        
        if (data == null) {
            return;
        }
        
        lastMessagesStr = text;
        
		// here we don't have to sort because we just need the latest millis value. 
        for (String millisStr : data.keySet()) {
            if (data.containsKey(millisStr)) {
                long millis = Long.parseLong(millisStr);
                if (millis > lastMessagesMillis) {
                    lastMessagesMillis = millis;
                }
            }
        }
    }
	
    
	/*Sends a SUBSCRIBE_SYMBOLS command to subscribe to market (tick) data.

    Args:
        symbols (String[]): List of symbols to subscribe to.
    
    Returns:
        null

        The data will be stored in marketData. 
        On receiving the data the eventHandler.onTick() 
        function will be triggered. 
	*/
    public void subscribeSymbols(String[] symbols) {
        sendCommand("SUBSCRIBE_SYMBOLS", String.join(",", symbols));
    }
	
    
	/*Sends a SUBSCRIBE_SYMBOLS_BAR_DATA command to subscribe to bar data.

    Args:
        symbols (String[][]): List of lists containing symbol/time frame 
        combinations to subscribe to. For example:
        String[][] symbols = {{"EURUSD", "M1"}, {"USDJPY", "H1"}};
    
    Returns:
        null

        The data will be stored in barData. 
        On receiving the data the eventHandler.onBarData() 
        function will be triggered. 
	*/
    public void subscribeSymbolsBarData(String[][] symbols) {
        String content = "";
        for (int i=0; i<symbols.length; i++) {
            if (i != 0) {
                content += ",";
            }
            content += symbols[i][0] + "," + symbols[i][1];
        }
        sendCommand("SUBSCRIBE_SYMBOLS_BAR_DATA", content);
    }
	
    
	/*Sends a GET_HISTORIC_DATA command to request historic data.
    
    Args:
        symbol (String): Symbol to get historic data.
        timeFrame (String): Time frame for the requested data.
        start (long): Start timestamp (seconds since epoch) of the requested data.
        end (long): End timestamp of the requested data.
    
    Returns:
        null

        The data will be stored in historicData. 
        On receiving the data the eventHandler.onHistoricData() 
        function will be triggered. 
	*/
    public void getHistoricData(String symbol, String timeFrame, long start, long end) {
        String content = symbol + "," + timeFrame + "," + start + "," + end;
        sendCommand("GET_HISTORIC_DATA", content);
    }
	
	
	/*Sends a GET_HISTORIC_TRADES command to request historic trades.
    
    Kwargs:
        lookbackDays (int): Days to look back into the trade history. 
		                    The history must also be visible in MT4. 
    
    Returns:
        None

        The data will be stored in historicTrades. 
        On receiving the data the eventHandler.onHistoricTrades() 
        function will be triggered. 
    */
    public void getHistoricTrades(int lookbackDays) {
        sendCommand("GET_HISTORIC_TRADES", String.valueOf(lookbackDays));
	}
	

	/*Sends an OPEN_ORDER command to open an order.

    Args:
        symbol (String): Symbol for which an order should be opened. 
        order_type (String): Order type. Can be one of:
            'buy', 'sell', 'buylimit', 'selllimit', 'buystop', 'sellstop'
        lots (double): Volume in lots
        price (double): Price of the (pending) order. Can be zero 
            for market orders. 
        stop_loss (double): SL as absoute price. Can be zero 
            if the order should not have an SL. 
        take_profit (double): TP as absoute price. Can be zero 
            if the order should not have a TP.  
        magic (int): Magic number
        comment (String): Order comment
        expriation (long): Expiration time given as timestamp in seconds. 
            Can be zero if the order should not have an expiration time.  
	*/
    public void openOrder(String symbol, String orderType, double lots, double price, double stopLoss, double takeProfit, int magic, String comment, long expiration) {
        
        String content = symbol + "," + orderType + "," + lots + "," + price + "," + stopLoss + "," + takeProfit + "," + magic + "," + comment + "," + expiration;
        System.out.println(content);
        System.out.flush();
        sendCommand("OPEN_ORDER", content);
    }
	

	/*Sends a MODIFY_ORDER command to modify an order.

    Args:
        ticket (int): Ticket of the order that should be modified.
        price (double): Price of the (pending) order. Non-zero only 
            works for pending orders. 
        stop_loss (double): New stop loss price.
        take_profit (double): New take profit price. 
        expriation (long): New expiration time given as timestamp in seconds. 
            Can be zero if the order should not have an expiration time. 
	*/
    public void modifyOrder(int ticket, double price, double stopLoss, double takeProfit, long expiration) {
        
        String content = ticket + "," + price + "," + stopLoss + "," + takeProfit + "," + expiration;
        sendCommand("MODIFY_ORDER", content);
    }
	

    /*Sends a CLOSE_ORDER command with lots=0 to close an order completely.
	*/
    public void closeOrder(int ticket) {
        
        String content = ticket + ",0";
        sendCommand("CLOSE_ORDER", content);
    }
	
    
	/*Sends a CLOSE_ORDER command to close an order.

    Args:
        ticket (int): Ticket of the order that should be closed.
        lots (double): Volume in lots. If lots=0 it will try to 
            close the complete position. 
	*/
    public void closeOrder(int ticket, double lots) {
        
        String content = ticket + "," + lots;
        sendCommand("CLOSE_ORDER", content);
    }
	
    
    /*Sends a CLOSE_ALL_ORDERS command to close all orders.
	*/
    public void closeAllOrders() {
        
        sendCommand("CLOSE_ALL_ORDERS", "");
    }
	
    
    /*Sends a CLOSE_ORDERS_BY_SYMBOL command to close all orders
    with a given symbol.

    Args:
        symbol (str): Symbol for which all orders should be closed. 
	*/
    public void closeOrdersBySymbol(String symbol) {
        
        sendCommand("CLOSE_ORDERS_BY_SYMBOL", symbol);
    }
	
    
    /*Sends a CLOSE_ORDERS_BY_MAGIC command to close all orders
    with a given magic number.

    Args:
        magic (str): Magic number for which all orders should 
            be closed. 
	*/
    public void closeOrdersByMagic(int magic) {
        
        sendCommand("CLOSE_ORDERS_BY_MAGIC", Integer.toString(magic));
    }


    /*Sends a RESET_COMMAND_IDS command to reset stored command IDs. 
    This should be used when restarting the java side without restarting 
    the mql side.
	*/
    public void resetCommandIDs() {
        
        commandID = 0;

        sendCommand("RESET_COMMAND_IDS", "");

        // sleep to make sure it is read before other commands.
        Helpers.sleep(500);
    }
	

	/*Sends a command to the mql server by writing it to 
    one of the command files. 

    Multiple command files are used to allow for fast execution 
    of multiple commands in the correct chronological order. 
    
    The method needs to be synchronized so that different threads 
    do not use the same commandID or write at the same time.
	*/
    synchronized void sendCommand(String command, String content) {
        
        commandID = (commandID + 1) % 100000;
        
        String text = "<:" + commandID + "|" + command + "|" + content + ":>";
        
        long now = System.currentTimeMillis();
        long endMillis = now + maxRetryCommandSeconds * 1000;
        
        // trying again for X seconds in case all files exist or are 
        // currently read from mql side. 
        while (now < endMillis) {
            
            // using 10 different files to increase the execution speed 
            // for muliple commands. 
            boolean success = false;
            for (int i=0; i<maxCommandFiles; i++) {
                
                String filePath = pathCommandsPrefix + i + ".txt";
				File f = new File(filePath);
                if (!f.exists() && Helpers.tryWriteToFile(filePath, text)) {
                    success = true;
                    break;
                }
            }
            if (success) {
                break;
            }
            Helpers.sleep(sleepDelay);
            now = System.currentTimeMillis();
        }
    }
}
