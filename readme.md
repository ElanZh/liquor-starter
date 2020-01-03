* 是指生产白酒时，从蒸馏甑冷凝流出的第一道酒。味烈、劲大、上头。
# 项目名：酒头

项目目的：采集springboot项目的接口请求信息

* 本项目依赖 ```spring-boot-starter-actuator```

使用方法：
1. 获取项目源码到本地，并执行
    ```shell 
    mvn clean install
    ```
2. 引入maven依赖：
    ```xml
    <dependency>
        <groupId>personal.elan.liquor</groupId>
        <artifactId>liquor-starter</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    ```
   
3. 项目配置加入：
    ```yaml
    management:
      trace:
        http:
          enabled: true
    ```
    同时将 ```elan.liquor.starter.SimpleTrace```的日志级别设置为 ```debug```
    
    这样项目日志中就会增加请求记录：
    ```java
    2019-12-29 20:58:34.056 DEBUG 6148 --- [nio-8080-exec-2] elan.liquor.starter.SimpleTrace         : http-trace-log| method: GET, path: /hello/elan, query: name=elan, rawQuery: name=elan, timeTaken: 2ms, time: 2019-12-29 20:58:34.053691000 .
    ```
   
### 注意：**该方法无法获取请求体的内容，是因为要兼顾springMVC和react**

---
## 使用另外一种配置可以收集http所有信息，包括请求体，但是只限于springMVC，react/webflux则失效：

1. 引入maven依赖之后使用如下配置：

    ```yaml
    liquor:
      http-full-trace:
        # 默认false
        enabled: true
    ```
   同时将```elan.liquor.starter.FullTraceConfig```的日志级别调整为```debug```，日志中就会收集到完整的http信息
   
2. ```FullLog```中的```customTag```为自定义标志，默认取 ```${spring.application.name}```
3. 如果对这种采集方式或者是采集内容不满意，可以去 ```implemets LogCollector```

    ```java
    @Slf4j
    @Component
    public class LogCollectConfig implements LogCollector {
        @Override
        public void collect(FullLog fullLog, ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
            // do something
        }
    }
    ```
   
4. 如果因为网络或代理问题导致```FullLog```中采集的请求IP不对，同样可以覆盖掉```LogCollector```中的```getIp```方法
5. ```excludeUri```和```includeUri```配置为排除的uri和包含的uri，使用```AntPathMatcher```做路径的正则匹配，下面举例说明配置规则：

    假定目前系统中有三个请求路径：
    
    ```/hello/world```
    
    ```/hello/liquor```
    
    ```/other/path```
    
    * 配置 甲：
        ```yaml
         liquor:
           http-full-trace:
             enabled: true
             customTag: MyApp
             excludeUri: 
               - /hello/*
        ```
        > 此时，```/hello/world```，```/hello/liquor```的信息都不被采集
        
        > 并且 customTag = MyApp

    * 配置 乙：
        ```yaml
         liquor:
           http-full-trace:
             enabled: true
             includeUri: 
               - /hello/*
        ```
        > 此时，有且只有```/hello/world```和```/hello/liquor```的信息被采集
        
        > 并且 customTag 为 spring.application.name的值（也没有配则为空）

    * 配置 丙：
        ```yaml
         spring:
           application:
             name: MyWebApp
         liquor:
           http-full-trace:
             enabled: true
             customTag: MyApp
             excludeUri: 
               - /hello/world
             includeUri: 
               - /hello/*
        ```
        > 此时，除了```/hello/world```以外，其他路径都将被采集
        
        > 并且 customTag = MyApp
    
    * exclude和include都没有配 则所有路径都将采集


