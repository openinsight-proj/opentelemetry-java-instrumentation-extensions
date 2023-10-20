/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.daocloud.otel.java.extension.nacos;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * <p>Also see https://github.com/open-telemetry/opentelemetry-java/issues/2022
 *
 * @see NacosAutoConfigurationCustomizerProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class NacosAutoConfigurationCustomizerProvider
        implements AutoConfigurationCustomizerProvider {
    final static String SPRING_CLOUD_DEP = "com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration";

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addPropertiesSupplier(this::setNacosPropertiesCallback);
    }

    private Map<String, String> setNacosPropertiesCallback() {
        Map<String, String> properties = new HashMap<>();

        String clusterId = Objects.isNull(System.getenv("OTEL_K8S_CLUSTER_UID")) ? "" : System.getenv("OTEL_K8S_CLUSTER_UID");
        String podName = Objects.isNull(System.getenv("OTEL_RESOURCE_ATTRIBUTES_POD_NAME")) ? "" : System.getenv("OTEL_RESOURCE_ATTRIBUTES_POD_NAME");
        String k8sNamespace = Objects.isNull(System.getenv("K8S_NAMESPACE")) ? "" : System.getenv("K8S_NAMESPACE");

        //TODO: if com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration claszz not found, do not set properties.
        properties.put("spring.cloud.nacos.discovery.metadata.k8s_cluster_id", clusterId);
        properties.put("spring.cloud.nacos.discovery.metadata.k8s_namespace_name", k8sNamespace);
        properties.put("spring.cloud.nacos.discovery.metadata.k8s_pod_name", podName);

        properties.forEach((k,v)->{
            System.out.println("OTEL Agent Extension[Nacos] setting property, key: " + k +" ,value: "+ v);
            System.setProperty(k,v);
        });

        return new HashMap<>();
    }
}
