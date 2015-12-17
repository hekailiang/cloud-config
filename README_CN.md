## cloud-config简介
cloud-config是一个基于Zookeeper的集中式应用配置中心，并与Spring框架紧密集。cloud-config解决了分布式应用或者集群环境中，以硬编码、配置文件、环境变量管理应用配置所造成的应用开发、系统运维工作的繁琐，支持`配置一次，处处使用`。     
cloud-confg在开发过程中受到 [Centralized Application Configuration with Spring and Apache ZooKeeper](http://www.infoq.com/presentations/spring-apache-zookeeper) 的启发。

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
        <property name="host" value="${mail.host}"/>
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
**注意：**   
1. Cloud-Config创建数据源时会合并当前路径(/database/mail)与其父节点(/database)及其profile子节点(/database/mail/dev)的内容，合并优先级dev>mail>database。  
2. 当auto-reload为true，并且dev节点中配置内容发生变化时，对应数据源将自动重新创建。

#### 多租户数据源配置  
支持多租户数据源需要使用到org.squirrelframework.cloud.resource.database.RoutingDataSourceFactoryBean来创建对应数据源。这里需要用户指定RoutingKeyResolver，需要配置与RoutingKey对应的目录结构。

对于多租户数据源配置，Zookeeper中节点配置如下：  
root  
|---/config    
|------/database  
|------|--/mail  
|---------|--/tenant1  
|---------|--|--/dev  
|---------|--|--/&172.31.201.0-99@dev  
|---------|--|--/prod  
|---------|--/tenant2  
|---------|--|--/dev  
|---------|--|--/prod  
|---------|--/unknown  

`/root/database/mail`: mail模块数据库资源配置节点  
`/root/database/mail/tenant1`: 租户ID为tenant1的mail模块数据库资源配置节点  
`/root/database/mail/tenant1/dev`: dev profile配置节点  
`/root/database/mail/tenant1/dev/&172.31.201.0-99@dev`:  条件节点，当本机IP地址在172.31.201.0-99之间，并且系统运行在dev profile上时生效，条件节点的配置会覆盖dev节点的配置。条件节点可以应用于针对某几台server启用某些配置项。条件节点必须同时指明IP地址范围(&)和生效Profile名称(@)。   

`/root/config/database/mail`中配置内容如下：  
```json
{
    "driverClassName" : "${mysql.driver.name}",
    
    "idleMaxAgeInMinutes" : 240,
    "idleConnectionTestPeriodInMinutes" : 60,
    "maxConnectionsPerPartition" : 10,
    "minConnectionsPerPartition" : 1,
    "partitionCount" : 2,
    "acquireIncrement" : 5,
    "statementsCacheSize" : 100
}
```
注：系统属性可以在资源配置中引用，例如在/{namespace}/properties/variables中定义了"{"mysql.driver.name":"com.mysql.jdbc.Driver"}"

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
    <!-- routing-support指定该路径下的数据库资源是支持路由的 -->
    <!-- fallback指定当解析出来的数据库路由键值无法匹配租户ID时，回退数据源的配置路径 -->
    <cc:zk-jdbc-datasource id="dataSource"
                           path="/database/mail"
                           routing-support="true"
                           fallback="/database/mail/unknown"
                           auto-reload="true"
    />
</beans>
```
**注意：**   
1. 默认数据库路由键值解析器的id必须是zk-default-resolver，否则就需要在zk-jdbc-datasource中通过resolver-ref指定对应的resolver。RoutingKeyResolver用于获取当前的数据库路由键值，用户需要实现对应的routing key resolver（e.g. TenantIdThreadLocalResolver）返回对应租户ID，且模块路径下的配置节点名为对应租户ID，才能保证数据源的正确路由。   
2. Cloud-Config创建数据源时会合并当前租户路径（/database/mail/tenant1）与父节点(/database/mail/)及其profile子节点(/database/mail/tenant1/dev)的内容，合并优先级dev>mail>tenant1。  
2. 当auto-reload为true，并且dev节点及其兄弟节点中配置内容发生变化，增加或删除profile节点，对应数据源将自动重新创建。

### 数据库路由器配置

#### 读写分离数据库路由配置
cloud-config通过在应用层做多数据源路由（嵌套路由）来支持读写分离，一写多读等应用场景。

对于读写分离数据源配置，Zookeeper中节点配置如下：
root  
|---/config    
|------/database  
|------|--/user.................................. _TenantIdThreadLocalResolver    (tenant1/tenant2)_    
|------------/tenant1............................ _MajorProfileRoutingKeyResolver (dev/prod)_   
|------------|--/dev  
|------------|--/prod............................ _DeclarativeRoutingKeyResolver  (write/read)_  
|------------|--|--/write  
|------------|--|--/read......................... _DispatchableRoutingKeyResolver (01/02/03)_       
|------------|--|--|--/01  
|------------|--|--|--/02    
|------------|--|--|--/03      
|------------/tenant2  
|------------|--/dev  
|------------|--/prod  
注：user模块目录结构展现了四层数据源嵌套路由。  
* 第一层是在模块节点(/database/user)上通过TenantIdThreadLocalResolver定位到指定的租户配置节点上。   
* 第二层是在租户节点(/database/user/tenant1)上通过MajorProfileRoutingKeyResolver对应的profile节点上。  
* 第三层是在profile节点(/database/user/tenant1/prod)上通过DeclarativeRoutingKeyResolver对应的读或写节点上。  
* 第四层是在读节点(/database/user/tenant1/prod/write)上通过DispatchableRoutingKeyResolver读节点下的子节点(01, 02, 03)进行Round-Robin选择。   
* 如果对应节点无子节点，则路由到该节点结束。例如，在/database/user/tenant1/dev下无读写节点，则所有的读写请求都路由到dev节点所对应的同一数据源。 
* 当路由到对应叶子节点时，例如/database/user/tenant1/prod/write，cloud-config仅合并该节点与其父节点(/database/user/tenant1/prod)上的配置信息创建数据源。  

在Spring中使用时需配置如下： 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cc="http://www.squirrelframework.org/schema/config"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/tx
            http://www.springframework.org/schema/tx/spring-tx.xsd
            http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context.xsd
            http://www.squirrelframework.org/schema/config
            http://www.squirrelframework.org/schema/config/cloud-config.xsd">

    <!-- 支持声明式路由 -->
    <cc:zk-declarative-routing/>

    <!--  创建租户ID路由器 -->
    <bean id="tenantResolver" class="org.squirrelframework.cloud.routing.TenantIdThreadLocalResolver"/>
    <!-- 创建主Profile路由器 -->
    <bean id="profileResolver"  class="org.squirrelframework.cloud.routing.MajorProfileRoutingKeyResolver"/>
    <!-- 创建声明式路由器 -->
    <bean id="rwSplitResolver" class="org.squirrelframework.cloud.routing.DeclarativeRoutingKeyResolver"/>
    <!-- 创建循环派发路由器 -->
    <bean id="dispatchResolver" class="org.squirrelframework.cloud.routing.DispatchableRoutingKeyResolver">
        <property name="path" value="/database/user"/>
        <!-- 启用自动刷新功能 -->
        <property name="autoRefresh" value="true"/>
        <!-- 每隔5分钟自动刷新可路由列表 (read节点下的01，02，03节点) -->
        <property name="refreshInterval" value="5"/>
    </bean>

    <!-- 创建user模块数据源路由器，id指定为my-default-resolver，通过NestedRoutingKeyResolver组装之前创建的路由器 -->
    <bean id="my-default-resolver" class="org.squirrelframework.cloud.routing.NestedRoutingKeyResolver">
        <property name="resolvers">
            <list>
                <!-- 路由器引用顺序与user模块目录结构对应 -->
                <ref bean="tenantResolver"/>
                <ref bean="profileResolver"/>
                <ref bean="rwSplitResolver"/>
                <ref bean="dispatchResolver"/>
            </list>
        </property>
    </bean>

    <cc:zk-client connection-string="127.0.0.1:1234"/>
    <!-- routing-support设为true启用数据库路由，并将routing resolver指定为my-default-resolver -->
    <cc:zk-jdbc-datasource id="dataSource" path="/database/user" routing-support="true" resolver-ref="my-default-resolver"/>

    <context:component-scan base-package="org.squirrelframework.cloud.sample" />
    <tx:annotation-driven transaction-manager="transactionManager" />
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory" />
    </bean>

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="packagesToScan" value="org.squirrelframework.cloud.sample"/>
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" />
        </property>
        <property name="jpaPropertyMap">
            <props>
                <prop key="hibernate.show_sql">false</prop>
                <prop key="hibernate.archive.autodetection" />
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.format_sql">true</prop>
                <!-- 在数据源启用routing-support后，自动创建、更新表功能必须屏蔽 -->
                <!--<prop key="hibernate.hbm2ddl.auto">create</prop>-->
                <!-- 在数据源启用routing-support后，该属性必须设为false -->
                <prop key="hibernate.temp.use_jdbc_metadata_defaults">false</prop>
            </props>
        </property>
    </bean>

</beans>
```
**注**：在启用数据源路由功能后，所有在应用启动时访问数据源动作都必须屏蔽掉，例如hibernate.hbm2ddl.auto，hibernate.temp.use\_jdbc\_metadata\_defaults。因为在系统启动时，无法获取任何数据源路由信息，无法确定该如何路由。


java代码中通过@RoutingKey进行声明式路由：
```java
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @Transactional
    @RoutingKey("write")
    public void insertUser(User user) {
        userDAO.insertUser(user);
    }
    
    // 如果使用了DispatchableRoutingKeyResolver，则recordRoutingKeys需设置为true
    @RoutingKey(value = "read", recordRoutingKeys = true)
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }
}
```

#### 水平分库路由配置
cloud-config通过支持复杂路由规则设置与可路由的数据库sequence生成器，来支持数据库水平拆分。本例将演示通过用户ID将产品数据均匀分布到01-04数据库中。  

对于水平分库数据源配置，Zookeeper中节点配置如下：   
root  
|---/config    
|------/database  
|------|--/product............................... _TenantIdThreadLocalResolver    (tenant1/tenant2)_    
|------------/tenant1............................ _MajorProfileRoutingKeyResolver (dev/prod)_   
|------------|--/dev............................. _DeclarativeRoutingKeyResolver  (01/02/03/04)_   
|------------|--|--/01  
|------------|--|--/02  
|------------|--|--/03    
|------------|--|--/04  
|------------|--/prod............................ _DeclarativeRoutingKeyResolver  (01/02/03/04)_  
|------------|--|--/01  
|------------|--|--/02  
|------------|--|--/03    
|------------|--|--/04         
|------------/tenant2  
|------------|--/dev  
|------------|--/prod  
|---/properties    
|---|--/sequence  

`/root/config/database/product/tenant1/dev`中配置内容如下：  
```json
{
    "driverClassName" : "com.mysql.jdbc.Driver",
    
    "idleMaxAgeInMinutes" : 240,
    "idleConnectionTestPeriodInMinutes" : 60,
    "maxConnectionsPerPartition" : 10,
    "minConnectionsPerPartition" : 1,
    "partitionCount" : 2,
    "acquireIncrement" : 5,
    "statementsCacheSize" : 100,
    "userName" : "root",
    "password" : "root"
}
```

`/root/config/database/product/tenant1/dev/01`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/product-t1-dev-01?useUnicode=true"
}
```
...  
`/root/config/database/product/tenant1/dev/04`中配置内容如下：  
```json
{
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/product-t1-dev-04?useUnicode=true"
}
```
在开发环境上， 我们将01-04产品库对应到同一台数据库服务器。在生产环境中可以对应到单独的数据库服务器。  

`/properties/sequence`中配置如下：
```json
{
    "sequence" : {
        "format.expression" : "T(java.lang.String).format('%s%s%06d', #dbDateStr, #dbName.substring(15), #sequenceValue)",
        "product.id.sharding.rule" : "#id.substring(8, 10)",
        "product.sharding.rule" : "#product?.id?.substring(8, 10) ?: T(java.lang.String).format('%02d', #product.customerId%4+1)"
    }
}
```  
**注**：`/sequence`属性配置定义了sequence生成规则配置与数据库水平分片规则，规则通过Spring EL表达式描述。为了避免在代码中硬编码这些规则，我们将其定义在系统属性中，方便日后管理。  
* format.sequence：生成由16位数字组成的sequence组装规则，其中前8位由sequence生成时的日期组成（如20151217），9-10位由数据库的index组成（如01），后六位由一个当前库当前sequence下自增长的整数组成（如000001），组装出来的sequence就是2015121701000001.
*  product.id.sharding.rule：基于产品ID的数据源路由规则，取产品ID的9-10位作为当前路由键值（对应在format sequence的时候将数据库index放置在9-10位）
* product.sharding.rule：基于产品对象的数据库路由规则。当产品ID不为空时，按产品ID的9-10位键值路由，当产品ID为空时，按产品对应的客户ID与4的模值加1作为路由键值。这条规则可以同时满足创建新产品和保存已有产品时对于数据源路由的需求。

为了使用sequence功能，需在各个数据库中创建对应的sequence表。
```sql
CREATE TABLE IF NOT EXISTS __sequence_table__ (
     name varchar(64) NOT NULL,
     value varchar(20) NOT NULL,
     min_limit varchar(20) NOT NULL
     max_limit varchar(20) NOT NULL
     step varchar(20) NOT NULL
     create_time datetime NOT NULL
     modified_time datetime NOT NULL
     PRIMARY KEY (name)
)；
```
其中，```name```是sequence的名称，```value```是当前sequence的值，```min_limit```是sequence起始值，```max_limit```是sequence最大值（超过最大值后回到min_limit），```step```是每次取sequence的步长。

在Spring中使用时需配置如下： 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cc="http://www.squirrelframework.org/schema/config"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/tx
            http://www.springframework.org/schema/tx/spring-tx.xsd
            http://www.squirrelframework.org/schema/config
            http://www.squirrelframework.org/schema/config/cloud-config.xsd">

    <context:annotation-config/>

	<!--  创建租户ID路由器 -->
    <bean id="tenantResolver" class="org.squirrelframework.cloud.routing.TenantIdThreadLocalResolver"/>
    <!-- 创建主Profile路由器 -->
    <bean id="profileResolver"  class="org.squirrelframework.cloud.routing.MajorProfileRoutingKeyResolver"/>
    <!-- 创建声明式路由器 -->
    <bean id="routingKeyResolver" class="org.squirrelframework.cloud.routing.DeclarativeRoutingKeyResolver">
    	<!-- 启用循环取routing key -->
        <property name="rollingPoll" value="true"/>
    </bean>

	<!-- 组装sequence和routing datasource使用的路由器 -->
    <bean id="my-resolver" class="org.squirrelframework.cloud.routing.NestedRoutingKeyResolver">
        <property name="resolvers">
            <list>
                <ref bean="tenantResolver"/>
                <ref bean="profileResolver"/>
                <ref bean="routingKeyResolver"/>
            </list>
        </property>
    </bean>

	<!-- 启用声明式路由 -->
    <cc:zk-declarative-routing/>
    <cc:zk-client connection-string="127.0.0.1:1234"/>
    <!-- 配置系统属性路径 -->
    <cc:zk-property-placeholder path="/sequence"/>
    <!-- 配置routing datasource -->
    <cc:zk-jdbc-datasource id="dataSource" path="/database/mydb" routing-support="true" resolver-ref="my-resolver"/>
    <!-- 配置routing sequence，并设置sequence格式化表达式的属性键 -->
    <cc:zk-sequence-generator id="sequence" path="/database/mydb" resolver-ref="my-resolver" format-expression="${sequence.format.expression}"/>

    <context:component-scan base-package="org.squirrelframework.cloud.resource.sequence" />
    <tx:annotation-driven transaction-manager="transactionManager" />
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory" />
    </bean>

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="packagesToScan" value="org.squirrelframework.cloud.resource.sequence"/>
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" />
        </property>
        <property name="jpaPropertyMap">
            <props>
                <prop key="hibernate.show_sql">false</prop>
                <prop key="hibernate.archive.autodetection" />
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.format_sql">true</prop>
                <prop key="hibernate.temp.use_jdbc_metadata_defaults">false</prop>
            </props>
        </property>
    </bean>

</beans>
```

在应用代码中使用这些配置内容：
```java
@Service
public class ProductService {

    @Autowired
    @Qualifier("sequence")
    private SequenceGenerator sequenceGenerator;

    @Autowired
    private ProductDao productDao;

    @Transactional
    @RoutingKey("#{ ${sequence.product.sharding.rule} }")
    public String saveProduct(@RoutingVariable("product") Product product) throws Exception {
        if(product.getId() == null) {
        	// 创建新产品
            String productId = sequenceGenerator.next("product");
            product.setId(productId);
            productDao.save(product);
        } else {
        	// 更新已有产品
            productDao.update(product);
        }
        return product.getId();
    }

    @RoutingKey("#{ ${sequence.product.id.sharding.rule} }")
    public Product findProductById(@RoutingVariable("id") String id) {
        return productDao.findProductById(id);
    }

}
```


## 开发计划
* 生产环境配置的权限控制及监管  
* 敏感数据加密
* 完善cloud-config-server展现  
* 支持Routing Sequence
* 支持更多的资源配置  
	* Mongo connection pools
	* Redis connections
	* FTP and SFTP connections
	* RabbitMQ
	* SOLR
	* ElasticSearch
	* Executor services

