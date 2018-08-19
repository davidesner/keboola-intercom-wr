FROM maven:3.5.0-jdk-8
MAINTAINER David Esner <esnerda@gmail.com>


COPY . /code/
ENV MAVEN_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx256m"
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx256m"
WORKDIR /code/
RUN mvn compile

ENTRYPOINT mvn -q exec:java -Dexec.args=/data