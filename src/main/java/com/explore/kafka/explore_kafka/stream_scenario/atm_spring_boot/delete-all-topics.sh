#!/usr/bin/env bash
set -euo pipefail

CONTAINER="kafka-1"
BOOTSTRAP="localhost:9092"

# List topics, skip empty lines and internal topics starting with __, then delete each
docker exec "$CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP" --list \
  | grep -vE '^$|^__' \
  | while IFS= read -r topic; do
      if [ -n "$topic" ]; then
        echo "Deleting topic: $topic"
        docker exec "$CONTAINER" kafka-topics --bootstrap-server "$BOOTSTRAP" --delete --topic "$topic" \
          || echo "Warning: failed to delete topic $topic"
      fi
    done