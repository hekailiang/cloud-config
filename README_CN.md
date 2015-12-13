## cloud-config简介
cloud-config是一个基于Zookeeper的集中化管理的应用配置中心，并与Spring框架紧密集成，支持多租户配置。cloud-config解决了分布式应用或者集群环境下，以硬编码/配置文件/环境变量管理应用配置所造成的应用开发/运维的繁琐工作。  

## cloud-config-client主要功能介绍  
* [系统属性配置](#系统属性配置)
* [数据库资源配置](#数据库资源配置)
	* [单一数据库配置] (#单一数据库配置)
	* [多租户数据库配置] (#多租户数据库配置)
* [数据库路由器配置] (#数据库路由器配置)
	* [读写分离数据库路由配置] (#读写分离数据库路由配置)
	* [水平分库路由配置] (#水平分库路由配置)

## cloud-config-server主要功能介绍

### 系统属性配置
cloud-config扩展了Spring的*PropertySourcesPlaceholderConfigurer*, 用户在开发过程中可以很方便的引用Zookeeper中配置的键值。具体在使用时，cloud-config要求用户将所有全局系统属性置于Zookeeper的/{namespace}/properties节点下。例如，在Zookeeper中节点配置如下：  
root  
|-- /properties    
|---|--/query  
|---|--|--/dev  
|---|--|--/prod    
|---|--/mail  
|---|--|--/dev  
|---|--|--/prod  

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



### 数据库路由器配置