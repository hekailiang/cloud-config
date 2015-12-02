FROM hekailiang/jdk7
MAINTAINER Henry He <hekailiang@gmail.com>

# install git/maven/nodejs
RUN apt-get -y update && apt-get install --no-install-recommends -y -q curl git maven

# install nodejs
RUN curl http://nodejs.org/dist/v5.0.0/node-v5.0.0-linux-x64.tar.gz > nodejs.tar.gz
RUN tar xvzf nodejs.tar.gz && mv /node-v5.0.0-linux-x64 /nodejs && rm -rf nodejs.tar.gz
ENV PATH $PATH:/nodejs/bin

# clone the cloud-config project
RUN git clone https://github.com/hekailiang/cloud-config.git

# switch to cloud-config directory
WORKDIR cloud-config

# build entire project
RUN cd cloud-config-ui && npm install && cd .. && mvn clean install

# expose default port
EXPOSE 8001

CMD java -Dnamespace=testing -Dconfig.center.url=$ZK_URL -jar cloud-config-server/target/cloud-config-server-*.jar
