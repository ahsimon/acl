#!/bin/bash

# Check if the URL, number of requests, and number of parallel processes are passed as parameters
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo "Usage: $0 <url> <number_of_requests> <parallel_processes>"
  exit 1
fi

# The URL you want to call
url="$1"

# The number of requests to generate
num_requests="$2"

# The number of parallel processes to run
parallel_processes="$3"

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

# Generate numbers and pass them to xargs to run the specified number of parallel requests
seq "$num_requests" | xargs -n1 -P"$parallel_processes" bash -c 'make_request'
