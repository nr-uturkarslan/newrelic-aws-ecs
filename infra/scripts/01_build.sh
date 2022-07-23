#!/bin/bash

##################
### Apps Setup ###
##################

### Set variables

# Persistence
declare -A persistence
persistence["name"]="aws-ecs-persistence"
persistence["port"]=8080

####################
### Build & Push ###
####################

# Persistence
persistenceDockerTag="${DOCKERHUB_NAME}/${persistence[name]}:$(date +%s)"
docker build \
  --platform linux/amd64 \
  --build-arg newRelicAppName=${persistence[name]} \
  --build-arg newRelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  --tag $persistenceDockerTag \
  "../../apps/persistence/."
docker push $persistenceDockerTag
