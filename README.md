# spring-jpa-restful-core
jpa的Entity自动资源化

## 本项目为maven项目，基于spring-data-jpa 和 spring-jpa-mysql-smart-query

### 使用方法：
1. 下载源码后，mvn install到本地仓库
2. 在Application 上面加上注解@EnableJpaRestful
3. 在application.yml或者properties上面添加设置 spring.jpa.restful.path(访问前缀)，spring.jpa.restful.structure-path(结构查看器，调试用)
4. 启动访问/structure-path查看效果

### 使用扩展：
1. 框架提供了get，post，delete的拦截器，方便用户控制权限和修改结果
