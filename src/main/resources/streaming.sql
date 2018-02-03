DROP TABLE streaming_task;
DROP TABLE streaming_conf;
DROP TABLE streaming_sql;
DROP TABLE streaming_col;

CREATE TABLE streaming_task (
  id                INT NOT NULL PRIMARY KEY,
  streaming_conf_id INT NOT NULL, /*映射的数据配置表*/
  streaming_col_id  INT NOT NULL, /*映射的列名表*/
  streaming_sql_id  INT NOT NULL, /*顺序执行的sql表*/
  process_id        INT NOT NULL, /*任务启动的进程号*/
  process_status    INT NOT NULL /*任务启动状态 0停止 1启动成功*/
);

CREATE TABLE streaming_conf (
  id                   INT          NOT NULL,
  topic_name           VARCHAR(64)  NOT NULL, /*topic名称*/
  bootstrap_server     VARCHAR(64)  NOT NULL, /*连接串*/
  calss                VARCHAR(128) NOT NULL, /*streaming 主类*/
  app_name             VARCHAR(64)  NOT NULL UNIQUE,
  master_url           VARCHAR(64)  NOT NULL,
  executor_memory      VARCHAR(8)   NOT NULL,
  total_executor_cores VARCHAR(8)   NOT NULL,
  driver_cores         VARCHAR(8)   NOT NULL,
  driver_memory        VARCHAR(8)   NOT NULL,
  conf                 VARCHAR(1024), /*具体配置信息 --conf key1=value1 --conf key2=value2 ....*/
  depend_jars_path     VARCHAR(4096), /* 额外依赖的jar  hdfs:// local:/意味着没有IO，所有worker节点必须存在 */
  main_jar_path        VARCHAR(128) NOT NULL /*主jar路径*/
);

CREATE TABLE streaming_col (
  id              INT          NOT NULL,
  name            VARCHAR(32)  NOT NULL, /*列名*/
  pattern         VARCHAR(128) NOT NULL, /*正则表达(.*)*/
  temp_view       VARCHAR(32)  NOT NULL, /*注册的临时表名*/
  drop_dirty      INT          NOT NULL, /*被清洗的数据是否丢弃 0：丢弃 1：保存*/
  dirty_save_path VARCHAR(128) /*远程 hdfs://xx/xx 本地file:///xx/xx */
);

CREATE TABLE streaming_sql (
  id        INT           NOT NULL,
  spark_sql VARCHAR(4096) NOT NULL,
  res_table VARCHAR(32),
  res_save_type int /*0:hive 1:mysql 2:redis 3:local*/
)


SHOW TABLES;

select dno,FROM_UNIXTIME((UNIX_TIMESTAMP(hdate) DIV 300)*300) gb,sum(sal) count from emp group by dno,gb

select d.dno,FROM_UNIXTIME((UNIX_TIMESTAMP(e.hdate) DIV 300)*300) gb,sum(e.sal) count from gus.dept d, emp e where d.dno = e.dno group by e.dno,gb