# OpenSearch monitoring for OpenSearch

## Demo Environment

For the demo environment, we want to have two OpenSearch clusters:

- The main cluster, which is the cluster to be monitored
- The monitoring cluster, which receives logs and processes it

We use:

- minikube for providing a K8s cluster
- OpenSearch K8s operator
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

3. Setup K8s operator for OpenSearch

```bash
helm repo add opensearch-operator https://opensearch-project.github.io/opensearch-k8s-operator/
helm install opensearch-operator opensearch-operator/opensearch-operator
```

4. Install secrets for main OpenSearch cluster

```bash
kubectl apply -f main-cluster/admin-credentials-secret.yaml
kubectl apply -f main-cluster/dashboards-credentials-secret.yaml
kubectl apply -f main-cluster/security-config-secret.yaml
```

*The secrets are used in `operator-config.yaml`. Thus, they need to be installed first.*

5. Install additional config files for the main OpenSearch cluster

```bash
kubectl apply -f main-cluster/log4j2-config.yaml
```

*The config file is used in `operator-config.yaml`. Thus, they need to be installed first.*

6. Install main OpenSearch cluster

```bash
kubectl apply -f main-cluster/operator-config.yaml
```

*This starts the cluster.*

7. Review the progress of the startup sequence

*Starting up the cluster takes a while. You can review the created nodes/pods by executing the following command:*

```bash
kubectl get pods
```
*The startup is not finished until you see the three pods `main-cluster-masters-0`, `main-cluster-masters-1`, `main-cluster-masters-2`.

*You can review the logs of pods by taking a pod name from `kubectl get pods` and executing:*

```bash
kubectl logs <pod name>
```


8. Make Dashboards available locally

```bash
kubectl port-forward svc/main-cluster-dashboards 5601 
```

The following does not work:

8.1. Create host names for your local system

```bash
echo "$(minikube ip) dashboards.main.local" | sudo tee -a /etc/hosts
echo "$(minikube ip) dashboards.monitoring.local" | sudo tee -a /etc/hosts
```

8.2. Enable the ingress plugin in Minikube

```bash
minikube addons enable ingress
```

8.3. Activate the ingress

```bash
kubectl apply -f main-cluster/dashboards-ingress.yaml
```

```bash
kubectl port-forward svc/main-cluster-dashboards 5601
```

9. Create namespace for monitoring OpenSearch cluster

```bash
kubectl create namespace monitoring
```

*We run the monitoring cluster in a separate namespace. This helps to keep
the different configurations apart.*


10. Install secrets for monitoring OpenSearch cluster

```bash
kubectl apply -f monitoring-cluster/admin-credentials-secret.yaml
kubectl apply -f monitoring-cluster/dashboards-credentials-secret.yaml
kubectl apply -f monitoring-cluster/security-config-secret.yaml
```
11. Install monitoring OpenSearch cluster

```bash
kubectl apply -f monitoring-cluster/operator-config.yaml
```

12. Make Dashboards available locally
```bash
kubectl port-forward svc/monitoring-cluster-dashboards 5601 --namespace=monitoring
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
kubectl delete opensearchcluster main-cluster
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