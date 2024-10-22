package com.xianchen.dwxconnect.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author xianchen-bot
 */
@Slf4j
public class Helpers {
    
	/*Prints to console output. 
	
	Args:
		obj (Object): Object to print. 
	
	*/
    public static void print(Object obj) {
        log.info(JSON.toJSONString(obj));
    }
	
    
	/*Tries to sleep for a given time period. 
	
	Args:
		millis (int): milliseconds to sleep. 
	
	*/
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignore) {
        }
    }
	
    
	/*Tries to read a file. 
	
	Args:
		filePath (String): file path of the file.
	
	*/
    public static String tryReadFile(String filePath) {
        
        final File f = new File(filePath);
        if(!f.exists()) {
            // print("ERROR: file does not exist: " + filePath);
            return "";
        }
        
        try{
            Path path = Paths.get(filePath);
            return new String(Files.readAllBytes(path), StandardCharsets.US_ASCII);
        } catch (Exception e) {
			// e.printStackTrace();
            return "";
		}
    }
	
    
    /*Tries to write to a file. 
	
	Args:
		filePath (String): file path of the file.
		text (String): text to write. 
	
	*/
    public static boolean tryWriteToFile(String filePath, String text) {
        try {
            Path path = Paths.get(filePath);
            // 确保父目录存在
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            // 写入文件
            Files.write(path, text.getBytes());
            return true;
        } catch (IOException e) {
            log.error("tryWriteToFile failed.", e);
            return false;
        }
    }
	
    
	/*Tries to delete a file. 
	
	Args:
		filePath (String): file path of the file.
	
	*/
    public static void tryDeleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("delete file success. filePath: {}", filePath);
        } catch (Exception e) {
            log.error("delete file failed.", e);
        }
    }
}
