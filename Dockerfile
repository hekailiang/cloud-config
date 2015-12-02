FROM hekailiang/jdk7
MAINTAINER Henry He <hekailiang@gmail.com>

# install git/maven/nodejs
RUN apt-get -y update && apt-get install --no-install-recommends -y -q curl python build-essential git ca-certificates maven

# install nodejs
RUN mkdir /nodejs && curl http://nodejs.org/dist/v5.0.0/node-v5.0.0-linux-x64.tar.gz | tar xvzf - -C /nodejs --strip-components=1

ENV PATH $PATH:/nodejs/bin

# clone the cloud-config project
RUN git clone https://github.com/hekailiang/cloud-config.git

# switch to cloud-config directory
WORKDIR cloud-config

# build entire project
RUN cd cloud-config-ui && npm install && cd .. && mvn clean install

# expose default port
EXPOSE 8001

CMD java -Dnamespace=testing -Dconfig.center.url=$ZK_URL -jar cloud-config-server/target/cloud-config-server-1.0.0-SNAPSHOT.jar
