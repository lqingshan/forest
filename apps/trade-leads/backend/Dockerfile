FROM crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/jdk:25.0.2-oraclelinux9 AS build

ARG MAVEN_VERSION=3.9.11
ENV MAVEN_HOME=/opt/apache-maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /workspace

RUN set -eux; \
    curl -fsSL "https://mirrors.huaweicloud.com/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tar.gz; \
    mkdir -p /opt; \
    tar -xzf /tmp/maven.tar.gz -C /opt; \
    mv "/opt/apache-maven-${MAVEN_VERSION}" "${MAVEN_HOME}"; \
    rm -f /tmp/maven.tar.gz

COPY base-backend/pom.xml base-backend/pom.xml
COPY base-backend/pom.trade-leads.xml base-backend/pom.trade-leads.xml
COPY base-backend/starter-common/pom.xml base-backend/starter-common/pom.xml
COPY base-backend/starter-auth/pom.xml base-backend/starter-auth/pom.xml
COPY base-backend/starter-wechat/pom.xml base-backend/starter-wechat/pom.xml
COPY base-backend/starter-carrier-auth/pom.xml base-backend/starter-carrier-auth/pom.xml
COPY base-backend/starter-redis/pom.xml base-backend/starter-redis/pom.xml
COPY base-backend/starter-sms/pom.xml base-backend/starter-sms/pom.xml
COPY base-backend/starter-object-storage/pom.xml base-backend/starter-object-storage/pom.xml
COPY business/common/backend/pom.xml business/common/backend/pom.xml
COPY business/domains/notification/backend/pom.xml business/domains/notification/backend/pom.xml
COPY business/domains/verification/backend/pom.xml business/domains/verification/backend/pom.xml
COPY business/domains/user/backend/pom.xml business/domains/user/backend/pom.xml
COPY business/domains/file/backend/pom.xml business/domains/file/backend/pom.xml
COPY business/domains/point/backend/pom.xml business/domains/point/backend/pom.xml
COPY business/domains/lead/backend/pom.xml business/domains/lead/backend/pom.xml
COPY business/domains/recharge/backend/pom.xml business/domains/recharge/backend/pom.xml
COPY business/domains/payment/backend/pom.xml business/domains/payment/backend/pom.xml
COPY business/aggregations/user-point/backend/pom.xml business/aggregations/user-point/backend/pom.xml
COPY business/aggregations/user-lead/backend/pom.xml business/aggregations/user-lead/backend/pom.xml
COPY apps/trade-leads/backend/pom.xml apps/trade-leads/backend/pom.xml

COPY base-backend/starter-common/src base-backend/starter-common/src
COPY base-backend/starter-auth/src base-backend/starter-auth/src
COPY base-backend/starter-wechat/src base-backend/starter-wechat/src
COPY base-backend/starter-carrier-auth/src base-backend/starter-carrier-auth/src
COPY base-backend/starter-redis/src base-backend/starter-redis/src
COPY base-backend/starter-sms/src base-backend/starter-sms/src
COPY base-backend/starter-object-storage/src base-backend/starter-object-storage/src
COPY business/common/backend/src business/common/backend/src
COPY business/domains/notification/backend/src business/domains/notification/backend/src
COPY business/domains/verification/backend/src business/domains/verification/backend/src
COPY business/domains/user/backend/src business/domains/user/backend/src
COPY business/domains/file/backend/src business/domains/file/backend/src
COPY business/domains/point/backend/src business/domains/point/backend/src
COPY business/domains/lead/backend/src business/domains/lead/backend/src
COPY business/domains/recharge/backend/src business/domains/recharge/backend/src
COPY business/domains/payment/backend/src business/domains/payment/backend/src
COPY business/aggregations/user-point/backend/src business/aggregations/user-point/backend/src
COPY business/aggregations/user-lead/backend/src business/aggregations/user-lead/backend/src
COPY apps/trade-leads/backend/src apps/trade-leads/backend/src

WORKDIR /workspace/base-backend

RUN --mount=type=cache,id=forest-maven-repository,target=/root/.m2/repository,sharing=locked \
    mvn -f pom.trade-leads.xml -Dmaven.repo.local=/root/.m2/repository -pl com.forest.apps:forest-app-trade-leads -am clean package -DskipTests \
    && cp /workspace/apps/trade-leads/backend/target/forest-app-trade-leads-*.jar /workspace/trade-leads-backend.jar

FROM crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/jdk:25.0.2-oraclelinux9

WORKDIR /app

COPY --from=build /workspace/trade-leads-backend.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
