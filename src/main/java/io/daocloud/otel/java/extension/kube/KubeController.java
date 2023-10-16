package io.daocloud.otel.java.extension.kube;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class KubeController {
    final static String KUBE_DIR = "/.kube/config";

    public KubeController() {
        // file path to your KubeConfig
        String kubeConfigPath = System.getenv("HOME") + KUBE_DIR;
        try (FileReader fr = new FileReader(kubeConfigPath)) {
            // loading the out-of-cluster config, a kubeconfig from file-system
            ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(fr)).build();
            // set the global default api-client to the in-cluster one from above
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            System.err.printf("create java-client error: %s", e.getMessage());
        }
    }

    public V1Namespace getNamespace(String namespaceName) throws ApiException {
        V1NamespaceList list = new CoreV1Api().listNamespace(null, null, null, null, null, null, null, null, null, null);
        for (V1Namespace item : list.getItems()) {
            if (Objects.equals(namespaceName, item.getMetadata().getName())) {
                return item;
            }
        }
        return null;
    }
}