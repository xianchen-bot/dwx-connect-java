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
    
    # 2. 启动 target 目录下的 .jar 文件
    java -jar target/dwxconnect-0.0.1-SNAPSHOT.jar
    ```
6. 启动成功终端将打印以下内容
    ```
    .   ____          _            __ _ _
    /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
    \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
    '  |____| .__|_| |_|_| |_\__, | / / / /
    =========|_|==============|___/=/_/_/_/
    :: Spring Boot ::        (v2.2.2.RELEASE)
    
    2024-10-22 19:17:12.912  INFO 32387 --- [           main] c.x.dwxconnect.DwxConnectApplication     : Starting DwxConnectApplication v0.0.1-SNAPSHOT on ...
    2024-10-22 19:17:12.914  INFO 32387 --- [           main] c.x.dwxconnect.DwxConnectApplication     : No active profile set, falling back to default profiles: default
    2024-10-22 19:17:13.423  INFO 32387 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
    2024-10-22 19:17:13.429  INFO 32387 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
    2024-10-22 19:17:13.429  INFO 32387 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.29]
    2024-10-22 19:17:13.463  INFO 32387 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
    2024-10-22 19:17:13.463  INFO 32387 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 516 ms
    2024-10-22 19:17:15.074  INFO 32387 --- [           main] com.xianchen.dwxconnect.utils.Helpers    : "\nAccount info:\n{}\n"
    2024-10-22 19:17:15.075  INFO 32387 --- [           main] c.x.dwxconnect.service.ClientService     : Service initialized!
    2024-10-22 19:17:15.144  INFO 32387 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
    2024-10-22 19:17:15.248  INFO 32387 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
    2024-10-22 19:17:15.252  INFO 32387 --- [           main] c.x.dwxconnect.DwxConnectApplication     : Started DwxConnectApplication in 7.564 seconds (JVM running for 7.807)
    ```
7. 验证服务启动与否，浏览器请求以下地址
    ```
    http://127.0.0.1:8080/hello  # 启动成功访问此地址，将会输出 hello
    ```