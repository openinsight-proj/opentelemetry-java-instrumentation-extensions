package io.daocloud.otel.java.extension.kube;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import io.kubernetes.client.util.Strings;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class KubeClient {
    final static String KUBE_DIR = "/.kube/config";
    // the CoreV1Api loads default api-client from global configuration.
    static CoreV1Api API = new CoreV1Api();

    public KubeClient() {
        ApiClient client = null;
        if (!Strings.isNullOrEmpty(System.getenv("EXTENSION_DEVELOPMENT"))) {
            // file path to KubeConfig
            String kubeConfigPath = System.getenv("HOME") + KUBE_DIR;
            try (FileReader fr = new FileReader(kubeConfigPath)) {
                // loading the out-of-cluster config, a kubeconfig from file-system
                client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(fr)).build();
            } catch (IOException e) {
                System.err.printf("create out-cluster kube-java-client error: %s", e.getMessage());
            }
        } else {
            // in-cluster configuration
            // loading the in-cluster config, including:
            //   1. service-account CA
            //   2. service-account bearer-token
            //   3. service-account namespace
            //   4. master endpoints(ip, port) from pre-set environment variables
            try {
                client = ClientBuilder.cluster().build();
            } catch (IOException e) {
                System.err.printf("create in-cluster kube-java-client error: %s", e.getMessage());
            }

            // set the global default api-client to the in-cluster one from above
            if (Objects.nonNull(client)) {
                Configuration.setDefaultApiClient(client);
            }
        }

        API = new CoreV1Api();
    }

    public V1Namespace getNamespace(String namespaceName) throws ApiException {
        V1NamespaceList list = API.listNamespace(null, null, null, null, null, null, null, null, null, null);
        for (V1Namespace item : list.getItems()) {
            if (Objects.equals(namespaceName, Objects.requireNonNull(item.getMetadata()).getName())) {
                return item;
            }
        }
        return null;
    }
}