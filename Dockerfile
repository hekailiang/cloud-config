FROM hekailiang/jdk7
MAINTAINER Henry He <hekailiang@gmail.com>

# install git/maven/nodejs
RUN apt-get -y update && apt-get -y install git maven nodejs

# clone the cloud-config project
RUN git clone https://github.com/hekailiang/cloud-config.git

# switch to cloud-config directory
WORKDIR cloud-config

# build entire project
RUN cd cloud-config-ui && npm install && cd .. && mvn clean install

# expose default port
EXPOSE 8001

CMD java -Dnamespace=${namespace} -Dconfig.center.url=${zk.url} -jar cloud-config-server/target/cloud-config-server-1.0.0-SNAPSHOT.jar
