## cloud-config简介
cloud-config是一个基于Zookeeper的集中化管理的应用配置中心，并与Spring框架紧密集成，支持多租户配置。cloud-config解决了分布式应用或者集群环境下，以硬编码/配置文件/环境变量管理应用配置所造成的应用开发/运维的繁琐工作。  

## cloud-config-client主要功能介绍  
* [系统属性配置](#系统属性配置)
* [数据库资源配置](#数据库资源配置)
	* [单一数据源配置] (#单一数据源配置)
	* [多租户数据源配置] (#多租户数据源配置)
* [数据库路由器配置] (#数据库路由器配置)
	* [读写分离数据库路由配置] (#读写分离数据库路由配置)
	* [水平分库路由配置] (#水平分库路由配置)
* [自定义资源配置] (#自定义资源配置)

## cloud-config-server主要功能介绍

### 系统属性配置
cloud-config扩展了Spring的*PropertySourcesPlaceholderConfigurer*, 用户在开发过程中可以很方便的引用Zookeeper中配置的键值。具体在使用时，cloud-config要求用户将所有全局系统属性置于Zookeeper的/{namespace}/properties节点下。例如，在Zookeeper中节点配置如下：  
root  
|---/properties    
|---|--/query  
|---|--|--/dev  
|---|--|--/prod    
|---|--/mail  
|------|--/dev  
|------|--/prod  

`/root`：namespace节点，在系统启动时通过-Dnamesapce=root指定  
`/root/properties`: 系统属性配置根节点，所有系统属性相关配置都定义在这个节点下  
`/root/properties/query`: query模块属性配置节点，与query模块相关的属性定义以json格式在这个节点中  
`/root/properties/query/dev`: profile节点，在系统启动时通过-Dconfig.profile=dev指定。当系统的config profile是dev时，dev节点中定义的属性会与父节点属性**打平**合并，并且覆盖掉与父节点属性键相同的配置值。

`/root/property/mail`中配置内容如下：
```json
"mail": {
    "host": "smtp.sina.com",
    "port": "25"
}
```
`/root/property/mail/dev`中配置内容如下：
```json
"mail": {
    "username": "dev_mail",
    "password": "dev1234"
}
```
合并后得到的属性键值为：
```
mail.host=smtp.sina.com
mail.port=25
mail.username=dev_mail@sina.cn
mail.password=dev1234
```
效果等同于将以上内容定义在mail-config.properties中。

在Spring中使用时需配置如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--指定cloud-config namesapce和schema-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cc="http://www.squirrelframework.org/schema/config"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.squirrelframework.org/schema/config
            http://www.squirrelframework.org/schema/config/cloud-config.xsd">

    <!--配置用于读取配置信息的zookeeper client，connection string在系统启动时，可通过指定-Dconfig.center.url覆盖-->    
    <cc:zk-client connection-string="127.0.0.1:1234"/>

    <!--zk-property-placeholder中可以在path属性上同时指定多个配置路径（相对于{namesapce}/properties)，路径在前者优先-->
    <!--zk-property-placeholder中可以在location中引入本地配置文件，local-override为true时本地配置将覆盖zk中的配置-->    
    <cc:zk-property-placeholder path="/mail, /query" location="classpath:query-server.properties" local-override="true"/>
    
    <!--使用时与Spring原生property相同-->
    <bean id="mailBean" class="org.squirrelframework.cloud.spring.ZkPropertyPlaceholderConfigurerTest$SampleBean">
        <property name="host" value="${mal.host}"/>
        <property name="port" value="${mail.port}"/>
    </bean>
```

### 数据库资源配置
不同于属性配置，系统资源配置均定义在/{namespace}/config节点下。目前对于数据库资源抽象仅支持JDBC数据源。

#### 单一数据源配置  
对于单一数据源配置，Zookeeper中节点配置如下：  
root  
|---/config    
|---|--/database  
|------|--/mail  
|---------|--/dev  
|---------|--/prod

`/root/config`: 系统资源配置根节点，所有系统资源相关配置都定义在这个节点下  
`/root/config/database`：数据库资源配置节点  
`/root/config/database/mail`: mail模块数据库资源配置节点，资源定义以json格式在这个节点中 
`/root/config/database/mail/dev`: mail模块数据库资源dev profile配置节点

`/root/config/database`中配置内容如下：  
```json
{
    "driverClassName" : "com.mysql.jdbc.Driver",
    
    "idleMaxAgeInMinutes" : 240,
    "idleConnectionTestPeriodInMinutes" : 60,
    "maxConnectionsPerPartition" : 10,
    "minConnectionsPerPartition" : 1,
    "partitionCount" : 2,
    "acquireIncrement" : 5,
    "statementsCacheSize" : 100
}
```

`/root/config/database/mail`中配置内容如下：  
```json
{
    "userName" : "root",
    "password" : "root"
}
```

`/root/config/database/mail/dev`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/mail-dev?useUnicode=true"
}
```

`/root/config/database/mail/prod`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/mail-prod?useUnicode=true"
}
```
以上配置内容基于BoneCP Datasource配置（参见org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig)

在Spring中使用时需配置如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cc="http://www.squirrelframework.org/schema/config"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.squirrelframework.org/schema/config
            http://www.squirrelframework.org/schema/config/cloud-config.xsd">

    <cc:zk-client connection-string="127.0.0.1:1234"/>
    <!-- path指定相对于/{namespace}/config的数据源配置路径 -->
    <!-- resource-type指定Pool DataSource类型（BoneCP，C3P0，Druid）-->
    <!-- auto-reload指定当当前profile中配置内容发生变化时，是否重新创建数据源 -->
    <cc:zk-jdbc-datasource id="dataSource" path="/database/mail" resource-type="BoneCP" auto-reload="true"/>
</beans>
```
**注意：** Cloud-Config创建数据源时会合并当前路径(/database/mail)与其路径父节点(/database)及其profile子节点(/database/mail/dev)的内容，合并优先级dev>mail>database。当auto-reload为true，并且dev节点中配置内容发生变化时，对应数据源将自动重新创建。

#### 多租户数据源配置  
对于多租户数据源配置，Zookeeper中节点配置如下：  
root  
|---/config    
|------/database  
|------|--/mail  
|---------|--/tenant1  
|---------|--|--/dev  
|---------|--|--/prod  
|---------|--/tenant2  
|---------|--|--/dev  
|---------|--|--/prod  
|---------|--/unknown  

`/root/database/mail`: mail模块数据库资源配置节点  
`/root/database/mail/tenant1`: 租户ID为tenant1的mail模块数据库资源配置节点  
`/root/database/mail/tenant1/dev`: dev profile配置节点  

`/root/config/database/mail`中配置内容如下：  
```json
{
    "driverClassName" : "com.mysql.jdbc.Driver",
    
    "idleMaxAgeInMinutes" : 240,
    "idleConnectionTestPeriodInMinutes" : 60,
    "maxConnectionsPerPartition" : 10,
    "minConnectionsPerPartition" : 1,
    "partitionCount" : 2,
    "acquireIncrement" : 5,
    "statementsCacheSize" : 100
}
```

`/root/config/database/mail/tenant1`中配置内容如下：  
```json
{
    "userName" : "root",
    "password" : "root"
}
```

`/root/config/database/mail/tenant1/dev`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/mail-t1-dev?useUnicode=true"
}
```

`/root/config/database/mail/tenant1/prod`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/mail-t1-prod?useUnicode=true"
}
```

在Spring中使用时需配置如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cc="http://www.squirrelframework.org/schema/config"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.squirrelframework.org/schema/config
            http://www.squirrelframework.org/schema/config/cloud-config.xsd">

    <cc:zk-client connection-string="127.0.0.1:1234"/>
    <!-- 配置默认数据库路由键值解析器 -->
	<bean id="zk-default-resolver" class="org.squirrelframework.cloud.TestRoutingKeyResolver"/>
    <!-- path指定相对于/{namespace}/config的数据源配置路径 -->
    <!-- resource-type指定Pool DataSource类型（BoneCP，C3P0，Druid）-->
    <!-- auto-reload指定当当前profile中配置内容发生变化时，是否重新创建数据源 -->
    <!-- multi-tenancy-aware指定该路径下的数据库资源是多租户数据源 -->
    <!-- fallback指定当解析出来的数据库路由键值无法匹配租户ID时，回退数据源的配置路径 -->
    <cc:zk-jdbc-datasource id="dataSource"
                           path="/database/mail"
                           multi-tenancy-aware="true"
                           fallback="/database/mail/unknown"
                           auto-reload="true"
    />
</beans>
```
**注意：** 默认数据库路由键值解析器的id必须是zk-default-resolver，否则就需要在zk-jdbc-datasource中通过resolver-ref指定对应的resolver。RoutingKeyResolver用于获取当前的数据库路由键值，用户需要实现对应的routing key resolver（e.g. TenantIdResolver）返回对应租户ID，且模块路径下的配置节点名为对应租户ID，才能保证数据源的正确路由。

### 数据库路由器配置

## 未来计划
* 生产环境配置的权限控制及监管  
* 敏感数据加密
* 完善cloud-config-server展现  

