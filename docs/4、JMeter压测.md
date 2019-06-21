## 内容

JMeter入门
自定义变量模拟多用户
JMeter命令行使用
Spring Boot打war包
redis压测工具redis-benchmark

 ## 链接

[官网](http://jmeter.apache.org/)
[下载](http://jmeter.apache.org/download jmeter.cgi/)
[用户手册](http://jmeter.apache.org/usermanual/index.html)

## JMeter入门

### 并发

不是说并发量多少，而是并发量为多少的时候，QPS或者TPS为多少

### 使用

1.添加线程组
2.添加HTTP请求默认值
![HTTP请求默认值](.\pic\HTTP请求默认值.png)

3.添加HTTP请求
![HTTP请求](.\pic\HTTP请求.png)

4.添加聚合报告
![聚合报告](.\pic\聚合报告.png)

查看ThroughPut：可以代表QPS

## 测试结果

### 单个用户，样本为10000的情况

1.测试/goods/to_list，不做任何修改，QPS为200左右

2.测试/user/info，不做任何修改，QPS为400左右

3.测试/user/info，增加redis连接池以及数据库连接池的数量，QPS为600左右

4.热身之后，QPS可达到800+；

## linux测试

1.在windows上录好jmx
2.命令行：jmeter.sh -n -t XXX.jmx -I result.jtl
/soft/apache-jmeter-5.1.1/bin/jmeter.sh -n -t goods_list.jmx -l result.jtl
下载结果：sz result.jtl

3.把result.jtl导入到jmeter 

## redis压测

redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -n 100000
100个并发连接，100000个请求

```shell
====== SET ======
  100000 requests completed in 1.85 seconds
  100 parallel clients
  3 bytes payload
  keep alive: 1

26.72% <= 1 milliseconds
92.15% <= 2 milliseconds
98.78% <= 3 milliseconds
99.58% <= 4 milliseconds
99.70% <= 5 milliseconds
99.81% <= 6 milliseconds
99.84% <= 7 milliseconds
99.92% <= 8 milliseconds
99.97% <= 9 milliseconds
99.99% <= 10 milliseconds
100.00% <= 10 milliseconds
54083.29 requests per second
```

```shell
====== GET ======
  100000 requests completed in 1.77 seconds
  100 parallel clients
  3 bytes payload
  keep alive: 1

30.83% <= 1 milliseconds
93.31% <= 2 milliseconds
99.23% <= 3 milliseconds
99.90% <= 4 milliseconds
100.00% <= 4 milliseconds
56625.14 requests per second

====== INCR ======
  100000 requests completed in 1.88 seconds
  100 parallel clients
  3 bytes payload
  keep alive: 1

26.20% <= 1 milliseconds
90.22% <= 2 milliseconds
98.88% <= 3 milliseconds
99.89% <= 4 milliseconds
100.00% <= 6 milliseconds
100.00% <= 11 milliseconds
53304.90 requests per second
```

redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100 
存取大小为100字节的数据包 

```shell
PING_INLINE: 59737.16 requests per second
PING_BULK: 58719.91 requests per second
SET: 55309.73 requests per second
GET: 59241.71 requests per second
INCR: 59665.87 requests per second
LPUSH: 57903.88 requests per second
RPUSH: 57636.89 requests per second
LPOP: 56818.18 requests per second
RPOP: 58309.04 requests per second
SADD: 59988.00 requests per second
HSET: 58072.01 requests per second
SPOP: 59453.03 requests per second
LPUSH (needed to benchmark LRANGE): 53908.36 requests per second
LRANGE_100 (first 100 elements): 22583.56 requests per second
LRANGE_300 (first 300 elements): 7280.67 requests per second
LRANGE_500 (first 450 elements): 4692.63 requests per second
LRANGE_600 (first 600 elements): 3318.40 requests per second
MSET (10 keys): 46554.93 requests per second
```

redis-benchmark -t set,lpush -n 100000 -q
只测试某些操作的性能

```shell
SET: 59206.63 requests per second
LPUSH: 58004.64 requests per second
```

redis-benchmark -n 100000 -q script load "redis.call('set','foo','bar')"
只测试某些数值存取的性能

```shell
script load redis.call('set','foo','bar'): 57670.13 requests per second
```

## Spring Boot打war包
1.添加spring-boot-starter-tomcat的provided依赖
2.添加maven-war-plugin插件 

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
<build>
	  	<finalName>${project.artifactId}</finalName>  
	    <plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-war-plugin</artifactId>
					<configuration>
						<failOnMissingWebXml>false</failOnMissingWebXml>
					</configuration>
				</plugin>
		</plugins>
	</build>
```



3.重写MainApplication

```java
public class MainApplication extends SpringBootServletInitializer{
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(MainApplication.class);
	}
}
```

4.maven打包
mvn clean package
生成war包或者jar

## Spring Boot打war包

添加插件，右键maven工程->runAs ->maven install->复制jar包到linux

```xml
     <build>
        <plugins>
            <plugin>
                <!--该插件主要用途：构建可执行的JAR -->
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```



## linux环境测试

复制jar包到linux

nohup java -jar miaosha.jar &
将结果输出到nohub.out

### 测试商品展示 

热身之前
800

热身之后
1200

### 测试秒杀

热身之前
1100

热身之后
1300