#!/bin/bash

# The URL you want to call
url="http://localhost:6060/test"

# Function to make a single request
make_request() {
  # Capture the response body and status code
  response=$(curl -s -w "%{http_code}" "$url")
  http_code="${response: -3}"
  body="${response:0:${#response}-3}"
  
  # Check the response status code
  if [ "$http_code" -eq 200 ]; then
    echo "Request successful. Response body: $body"
  else
    echo "Request failed with status code $http_code. Response body: $body"
  fi
}

# Export the function to be used by xargs
export -f make_request
export url

# Generate 1000 numbers and pass them to xargs to run 20 concurrent requests
seq 10000 | xargs -n1 -P20 bash -c 'make_request'