* 是指生产白酒时，从蒸馏甑冷凝流出的第一道酒。味烈、劲大、上头。
# 项目名：酒头

项目目的：采集springboot项目的接口请求日志

* 本项目依赖 ```spring-boot-starter-actuator```

使用方法：

1. 引入maven依赖：
    ```xml
    <dependency>
        <groupId>personal.elan.liquor</groupId>
        <artifactId>liquor-starter</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    ```
   
2. 项目配置加入：
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
   
* 如果要将日志输出到其他，比如redis，mongo则需要自己继承 ```HttpTraceRepository``` 并实现其 ```add```方法，但是该方法无法获取请求体中的参数，是因为要兼顾springMVC和react

使用另外一种配置可以收集http所有信息，但是只限于springMVC，react/webflux则失效。





