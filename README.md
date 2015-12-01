# cloud-config
cloud-config is a multi-tenant-aware configuaration center based on Zookeeper and tightly integrated with Spring framework which is inspired by [Centralized Application Configuration with Spring and Apache ZooKeeper](http://www.infoq.com/presentations/spring-apache-zookeeper).

## How to start
1. Run following scripts to build entire project after sync the latest code  
   cd cloud-config-ui && npm install && cd .. && mvn clean install
2. Install Zookeeper and create a {namespace} node under Zookeeper root path
3. Start cloud-config-server application  
   java -Dnamespace={namespace} -Dconfig.center.url={zk.url} -jar cloud-config-server/target/cloud-config-server-1.0.0-SNAPSHOT.jar  
4. Go to http://localhost:8001/ in browser

## How to use
1. import cloud-config-client as your project maven dependency  

## Features  
1. System properties config and lookup    
2. Single or Multi-tenant-aware DataSource config and lookup    
3. Custom resource object config and lookup  

## Rencent Goal
1. Integrate with Docker to provide live demo
2. Enhance UI and documentation
