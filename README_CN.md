## cloud-config简介
cloud-config是一个基于Zookeeper的集中化管理的应用配置中心，并与Spring框架紧密集成，支持多租户配置。cloud-config解决了分布式应用或者集群环境下，以硬编码/配置文件/环境变量管理应用配置所造成的应用开发/运维的繁琐工作。

## cloud-config主要功能介绍  
* [全局属性配置](#全局属性配置)
* [数据库资源配置](#数据库资源配置)
* [数据库路由器配置] (#数据库路由器配置)
	* [多租户数据库路由配置] (#多租户数据库路由配置)
	* [读写分离数据库路由配置] (#读写分离数据库路由配置)
	* [水平分库路由配置] (#水平分库路由配置)

### 全局属性配置
cloud-config扩展了Spring的*PropertySourcesPlaceholderConfigurer*, 用户在开发过程中可以很方便的引用Zookeeper中配置的键值。具体在使用时，cloud-config要求用户将所有全局属性置于Zookeeper的{根目录}/properties文件夹下。  
例如：  
root  
|-- /properties    
|---|--/query  
|---|--|--/dev  
|---|--|--/prod    
|---|--/message  
|---|--|--/dev  
|---|--|--/prod    

### 数据库资源配置

### 数据库路由器配置