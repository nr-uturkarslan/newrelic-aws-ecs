#!/bin/bash

# Get commandline arguments
while (( "$#" )); do
  case "$1" in
    --app-name)
      appName="$2"
      shift
      ;;
    *)
      shift
      ;;
  esac
done

if [[ $appName == "" ]]; then
  echo -e "Application name (--app-name) is not given!\n"
fi

# Get application ID
appId=$(curl -X GET 'https://api.eu.newrelic.com/v2/applications.json' \
    -H "Api-Key:${NEWRELIC_API_KEY}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    | jq -r '.applications[] | select(.name==''"'${appName}'"'') | .id')

# Get timestamp
timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)

# Create deployment marker
curl -X POST "https://api.eu.newrelic.com/v2/applications/$appId/deployments.json" \
  -i \
  -H "Api-Key:${NEWRELIC_API_KEY}" \
  -H "Content-Type: application/json" \
  -d \
  '{
    "deployment": {
      "revision": "1.0.0",
      "changelog": "Initial deployment",
      "description": "Deploy app.",
      "user": "user1@newrelic.com",
      "timestamp": "'"${timestamp}"'"
    }
  }'
