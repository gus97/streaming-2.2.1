bin/spark-shell \
--name fixed999 \
--master spark://172.18.111.3:7078 \
--total-executor-cores 4 \
--executor-memory 1g \
--conf spark.eventLog.dir=hdfs://172.18.111.3:9000/spark-2.2.1/applicationHistory \
--conf spark.eventLog.enabled=true \
--conf spark.sql.shuffle.partitions=20  \
--conf spark.default.parallelism=50 \
--conf spark.shuffle.sort.bypassMergeThreshold=20

bin/spark-sql \
--name gus-sql \
--master spark://172.18.111.3:7078 \
--total-executor-cores 10 \
--executor-memory 8g

1、消费详情查询
 bin/kafka-consumer-groups.sh --bootstrap-server 172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092 --describe --group g1

2、topic各个partition对应的offset
bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092 --topic k01 --time -1

3、发送信息
bin/kafka-console-producer.sh --broker-list 172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092 --topic gxc200

4、消费信息
bin/kafka-console-consumer.sh --bootstrap-server 172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092 --new-consumer --topic gxc200
--from-beginning

5、创建topic
bin/kafka-topics.sh --create --zookeeper 172.18.111.7:2181,172.18.111.8:2181,172.18.111.9:2181 --replication-factor 3 --partitions 3 --topic gus200

6.为consumer设置offset
bin/kafka-consumer-groups.sh --bootstrap-server 172.18.111.7:9092,172.18.111.8:9092,172.18.111.7:9092  --describe  --group test01

 bin/kafka-consumer-groups.sh --bootstrap-server 172.18.111.7:9092,172.18.111.8:9092,172.18.111.7:9092  --reset-offsets --all-topics  --to-latest --execute  --group gus001

offsets.retention.minutes，默认值24小时，针对一个offset的消费记录的最长保留时间。
尝试减少offsets.retention.minutes值通常会加速这个topic过期消息删除的速度
log.cleaner.enable参数就是清除compact型topic的开关，默认就是true，不建议调整此参数

但是请注意:.9.0.0这个版本log.cleaner.enable默认是false,确保log.cleaner.enable为true

compact也不代表老的日志就一定会被删除，需要满足一定的条件，默认情况下可清理的部分至少要占50%以上才会尝试清除日志段

建议:
cleanup.policy=compact 或者 cleanup.policy=delete,compact

# 针对压缩文件的清理,默认false !!!!!!前置 必须设置  cleanup.policy=compact
log.cleaner.enable=true

#对于压缩的日志保留的最长时间，也是客户端消费消息的最长时间，同log.retention.minutes的区别在于一个控制未压缩数据，一个控制压缩后的数据
log.cleaner.delete.retention.ms=

#文件大小检查的周期时间，触发 log.cleanup.policy中设置的策略
log.retention.check.interval.ms=


empno   ename   hire_data   sal deptno
1000,g1000,2018-01-02 01:20:12,1000,10


./t1.sh shanpao ff14af92-6b26-4e9d-afa1-56e557c91a1e 22ebbf65-f8ea-4126-b13e-bc3b4c27fb9c spark://172.18.111.3:7078 hdfs://172.18.111.3:9000 10004 sp_ugc_new.jar smm 1 1024 10 null







