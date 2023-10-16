/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.daocloud.otel.java.extension.nacos;

import com.google.auto.service.AutoService;
import io.daocloud.otel.java.extension.kube.KubeClient;
import io.kubernetes.client.openapi.models.V1Namespace;
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
        autoConfiguration
                .addTracerProviderCustomizer(this::configureSdkTracerProvider)
                .addPropertiesSupplier(this::getNacosProperties);
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
            SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {

        return tracerProvider
                .setSpanLimits(SpanLimits.builder().setMaxNumberOfAttributes(1024).build());
    }

    private Map<String, String> getNacosProperties() {
        Map<String, String> properties = new HashMap<>();

        KubeClient kubeController = new KubeClient();
        V1Namespace ns;
        try {
            ns = kubeController.getNamespace("kube-system");
        } catch (Exception e) {
            System.err.printf("java-client get namespace error: %s", e.getMessage());
            return properties;
        }

        String clusterId = Objects.isNull(ns.getMetadata().getUid()) ? "" : ns.getMetadata().getUid();
        String podName = Objects.isNull(System.getenv("OTEL_RESOURCE_ATTRIBUTES_POD_NAME")) ? "" : System.getenv("OTEL_RESOURCE_ATTRIBUTES_POD_NAME");
        String k8sNamespace = Objects.isNull(System.getenv("K8S_NAMESPACE")) ? "" : System.getenv("K8S_NAMESPACE");

        try {
            Class.forName(SPRING_CLOUD_DEP);
            properties.put("spring.cloud.nacos.discovery.metadata.k8s_cluster_id", clusterId);
            properties.put("spring.cloud.nacos.discovery.metadata.k8s_namespace_name", k8sNamespace);
            properties.put("spring.cloud.nacos.discovery.metadata.k8s_pod_name", podName);
        } catch (Exception ignored) {
        }

        return properties;
    }
}
