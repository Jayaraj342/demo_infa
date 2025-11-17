# Build
`docker build -t demo .`

# Start
`docker run -p 8080:8080 demo`

# K8S deploy
`minikube image load demo:latest`
`kubectl apply -f demo.yaml`
`minikube service saas-service`