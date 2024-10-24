# dwx-connect-java
连接dwx的java服务

# 使用指南
1. java 版本 1.8
2. maven 版本 3.8+
3. 基础 client 注册信息可在 application.properties 中进行修改
    ```properties
    # meta_trader 目录（这项目前如果走配置的话，会提示找不到目录，先代码里写死吧）
    meta_trader_dir_path = "/Users/admin/Documents/dev/dwx-connect-java/MQL4/Files/"
    # 读取回调文件间隔（毫秒）ms
    sleep_delay = 5
    # send command 重试最大有效期（秒）s
    max_retry_command_seconds = 10
    
    # 以下两个暂时不知道啥意思
    load_orders_from_file = true
    verbose = true
    ```
4. 【重点】注意，meta_trader_dir_path 需要依据电脑实际安装的 mt 环境进行修改
    ```java
    // 参数位置：com.xianchen.dwxconnect.service.ClientService.MEAT_TRADE_DIR_PATH
   
       // 这项目前如果走配置的话，会提示找不到目录，先代码里写死吧
    //    @Value("${meta_trader_dir_path}")
    private final static String MEAT_TRADE_DIR_PATH = "/Users/admin/Documents/dev/dwx-connect-java/MQL4/Files/";
    ```
5. 启动方式
    ```bash
    # 1. 执行 maven 的打包命令，会在 target 目录生成 dwxconnect-0.0.1-SNAPSHOT.jar 文件
    mvn clean package
    
    # 2. 启动应用：执行 start.sh 脚本（会在当前目录下自动创建logs文件夹，日志信息都会记录在此）
    ./start.sh
   
    # 3. 检查应用运行pid：执行 check.sh 脚本
    ./check.sh
   
    # 4. 终止应用：执行 stop.sh 脚本
    ./stop.sh
    ```
6. 验证服务启动与否，浏览器请求以下地址
    ```
    http://127.0.0.1:8080/hello  # 启动成功访问此地址，将会输出 hello
    ```