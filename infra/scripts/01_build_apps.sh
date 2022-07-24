#!/bin/bash

##################
### Apps Setup ###
##################

### Set variables

# Persistence
declare -A persistence
persistence["name"]="aws-ecs-persistence"
persistence["port"]=8080

# Persistence
declare -A proxy
proxy["name"]="aws-ecs-proxy"
proxy["port"]=8080

####################
### Build & Push ###
####################

# Persistence
persistenceDockerTag="${DOCKERHUB_NAME}/${persistence[name]}:$(date +%s)"
echo "Docker tag: $persistenceDockerTag"

docker build \
  --platform linux/amd64 \
  --build-arg newRelicAppName=${persistence[name]} \
  --build-arg newRelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  --tag $persistenceDockerTag \
  "../../apps/persistence/."
docker push $persistenceDockerTag

# Proxy
proxyDockerTag="${DOCKERHUB_NAME}/${proxy[name]}:$(date +%s)"
echo "Docker tag: $proxyDockerTag"

docker build \
  --platform linux/amd64 \
  --build-arg newRelicAppName=${proxy[name]} \
  --build-arg newRelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  --tag $proxyDockerTag \
  "../../apps/proxy/."
docker push $proxyDockerTag
