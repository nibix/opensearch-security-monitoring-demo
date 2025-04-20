# OpenSearch monitoring for OpenSearch

## Demo Environment

For the demo environment, we want to have two OpenSearch clusters:

- The main cluster, which is the cluster to be monitored
- The monitoring cluster, which receives logs and processes it

We use:

- minikube for providing a K8s cluster
- OpenSearch helm charts
- fluentbit for reading logfiles and for making logs available to DataPrepper (TODO)
- maybe: DataPrepper


## Steps

1. Configure minikube resources

```bash
minikube config set cpus 4
minikube config set memory 16384
```

*By default, minikube only provides 2GB of RAM, which is too little for the large
setup we are going to create. The `set memory 16384` command gives it 16GB.*

*Possibly, you also have to adjust resource limits in the Docker Desktop settings.*

2. Start minikube

```bash
minikube start
```

3. Setup helm charts for OpenSearch

```bash
helm repo add opensearch https://opensearch-project.github.io/helm-charts/
```

4. Generate TLS certs

*(already done with SG tlstool, see certs directory)*

5. Install TLS certs as secrets

```bash
kubectl create secret generic opensearch-certificates --from-file=main-cluster/certs/ 
```

6. Install secrets for main OpenSearch cluster

```bash
kubectl apply -f main-cluster/security-config-secret.yaml
```

*The secrets are used in `opensearch-helm-values.yaml`. Thus, they need to be installed first.*

7. Install main OpenSearch cluster

```bash
helm install --values=main-cluster/opensearch-helm-values.yaml main-cluster opensearch/opensearch
```

*This starts the cluster.*

8. Review the progress of the startup sequence

*Starting up the cluster takes a while. You can review the created nodes/pods by executing the following command:*

```bash
kubectl get pods
```

*You can review the logs of pods by taking a pod name from `kubectl get pods` and executing:*

```bash
kubectl logs <pod name>
```
9. Install OpenSearch Dashboards for main cluster

```bash
helm install --values=main-cluster/opensearch-dashboards-helm-values.yaml main-cluster-dashboards opensearch/opensearch-dashboards
```

*If you have installed it before, use the following instead:*

```bash
helm upgrade --install --values=main-cluster/opensearch-dashboards-helm-values.yaml main-cluster-dashboards opensearch/opensearch-dashboards
```

10. Make Dashboards available locally

```bash
kubectl port-forward svc/main-cluster-dashboards-opensearch-dashboards 5601 
```

9. Create namespace for monitoring OpenSearch cluster

```bash
kubectl create namespace monitoring
```

*We run the monitoring cluster in a separate namespace. This helps to keep
the different configurations apart.*

10. Install TLS certs as secrets

```bash
kubectl create secret generic opensearch-certificates --from-file=monitoring-cluster/certs/ --namespace=monitoring 
```

10. Install secrets for monitoring OpenSearch cluster

```bash
kubectl apply -f monitoring-cluster/security-config-secret.yaml
```
11. Install monitoring OpenSearch cluster

```bash
helm install --values=monitoring-cluster/opensearch-helm-values.yaml --namespace=monitoring monitoring-cluster opensearch/opensearch
```

12. Install OpenSearch Dashboards for monitoring cluster

```bash
helm install --values=monitoring-cluster/opensearch-dashboards-helm-values.yaml --namespace=monitoring monitoring-cluster-dashboards opensearch/opensearch-dashboards 
```

*If you have installed it before, use the following instead:*

```bash
helm upgrade --install --values=main-cluster/opensearch-dashboards-helm-values.yaml main-cluster-dashboards opensearch/opensearch-dashboards
```

12. Make Dashboards available locally
```bash
kubectl port-forward svc/monitoring-cluster-dashboards-opensearch-dashboards 5602:5601  --namespace=monitoring
```

13. Setup Fluent Bit

```bash
helm repo add fluent https://fluent.github.io/helm-charts
```

```bash
helm upgrade --install fluent-bit fluent/fluent-bit -f main-cluster/fluent-bit-values.yaml
```

This uses the customized fluent-bit-values.yaml configuration.


## Useful commands

### Logs of a pod

```
kubectl logs main-cluster-masters-1
```

### Shell on a pod

```bash
kubectl exec -it main-cluster-masters-1 -- bash
```

### Pods in the monitoring namespace

```bash
kubectl get pods --namespace monitoring
```

### Shell on the minikube K8s node

```
kubectl debug node/minikube -it --image=busybox --share-processes -- chroot /host
```

### Delete main cluster

```
helm delete main-cluster
```

### Stop fluentbit

```bash
helm uninstall fluent-bit
```

### Render fluentbit config to spot issues

```bash
helm template fluent-bit fluent/fluent-bit -f main-cluster/fluent-bit-values.yaml
```

## Resources

- Deploying OpenSearch using the K8s operator: https://opensearch.org/docs/latest/tools/k8s-operator/
- OpenSearch K8s operator user guide: https://github.com/opensearch-project/opensearch-k8s-operator/blob/main/docs/userguide/main.md
- OpenSearch K8s operator values.yml reference: https://github.com/opensearch-project/opensearch-k8s-operator/blob/main/docs/designs/crd.md
- Deploying Fluent Bit using K8s: https://docs.fluentbit.io/manual/installation/kubernetes#installation
- Fluentbit multiline parsing: https://www.cncf.io/blog/2025/04/08/managing-multi-line-logs-with-fluent-bit-and-python/
