# ref: https://github.com/open-telemetry/opentelemetry-operator/tree/main/autoinstrumentation/java
FROM busybox

COPY build/libs/opentelemetry-java-instrumentation-extensions-*.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar