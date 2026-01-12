### start minikube
minikube start

### Build
`docker build -t demo .`

### Start only docker container (not in k8s)
`docker run -p 8080:8080 demo`

### K8S deploy
`minikube image load demo:latest`  
`kubectl apply -f demo.yaml`  
`minikube service saas-service`  

### Delete all k8s components if required
`kubectl delete all --all`

### Read configmap
`kubectl get configmap leader -o yaml`

### debug inside cluster
`kubectl port-forward <pod-name> 5005:5005`