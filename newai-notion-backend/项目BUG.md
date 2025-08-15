## 1. 依赖冲突问题：
SpringBoot3.x和mybatis-plus中存在版本冲突问题，在[官网](https://baomidou.com/getting-started/install/#spring-boot3)中可以看到他们兼容的对应版本。

在本个项目中因为存在其他依赖，无法使用官方的SpringBoot版本推荐。

SpringBoot 3.3.5 对应Mybatis3.5.8可用

### 解决跨域问题
- 类上加@CrossOrigin(origins = "http://localhost:5173")
- 前端使用vite代理
  ```angular2html
  proxy: {
  '/api': {
  target: 'http://localhost:8080',
  ...
  }
  ```



## 启动日志

```
2025-08-09 10:04:10 [RMI TCP Connection(4)-192.168.31.204] INFO  com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@68ab7ef3
```

- Added connection com.mysql.cj.jdbc.ConnectionImpl代表数据库已连接

```
Logging initialized using 'class org.apache.ibatis.logging.stdout.StdOutImpl' adapter.
```

- 代表Mybatis-plus使用成功

