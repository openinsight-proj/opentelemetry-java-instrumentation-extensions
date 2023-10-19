# ref: https://github.com/open-telemetry/opentelemetry-operator/tree/main/autoinstrumentation/java
FROM busybox

COPY opentelemetry-javaagent.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar