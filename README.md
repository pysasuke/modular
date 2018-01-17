modular
===
## 项目介绍
简单地多模块项目，模块间通过kafka进行消息传递，使用Mysql作临时存储、Redis作缓存、MongoDB作最终数据存储。

## 项目结构

- modular-common(公用模块)
    - modular-common-base(简单公用类)
    - modular-common-database(Mysql相关模块)
    - modular-common-kafka(Kafka相关模块)
    - modular-common-mongo(MongoDB相关模块)
    - modular-common-redis(Redis相关模块)
- modular-handler(请求处理模块)
- modular-first(第一部分服务模块)
- modular-second(第二部分服务模块)
- modular-third(第三部分服务模块)
- modular-exhandler(异常数据处理模块)(补偿机制)

## 基本流程
由于只是demo，故只用简单地first、second、third指代具体服务，切以third服务为最终服务,handler也是使用伪造数据
- 请求发起
- 经handler模块后
    - 向Mysql写入一条数据
    - 向Redis写入一条数据
    - 向Kafka发送一条消息
- first模块收到handler模块发送的消息后
    - 从Redis取出对应数据并校验
    - 成功后修改Mysql数据
    - 修改Redis数据
    - 向Kafka发送一条消息
- second模块接收到first模块的发送的消息后
    - 从Redis取出对应数据并校验
    - 成功后修改Mysql数据
    - 修改Redis数据
    - 向Kafka发送一条消息
- third模块接收到second模块的发送的消息后
    - 从Redis取出对应数据并校验
    - 成功后向MongoDB写入一条数据
    - 删除Redis数据
    - 删除Mysql数据
- exhandler模块为补偿机制
    - 按照一定的策略处理Mysql中遗留的错误数据(正常情况所有数据都将转移到MongoDB中)
## 项目启动
- 执行deploy中update.sql创建record表
- 替换modular-common-database中application.properties配置文件中的相关参数为自己的Mysql参数
- 替换modular-common-redis中application.properties配置文件中的相关参数为自己的Redis参数
- 替换modular-common-mongo中application.properties配置文件中的相关参数为自己的MongoDB参数
- 修改modular-common-base中KafkaConstants类中BOOTSTRAP_SERVERS常量为自己Kafka地址(由于只是demo，故未做成配置文件)
- 启动相关服务
    - 正常流程
        - modular-handler(请求处理模块)
        - modular-first(第一部分服务模块)
        - modular-second(第二部分服务模块)
        - modular-third(第三部分服务模块)
    - 补偿机制
        - modular-exhandler(异常数据处理模块)

## 结果预测
- modular-first:输出 first service complete
- modular-second:输出 second service complete
- modular-third:输出 third service complete

