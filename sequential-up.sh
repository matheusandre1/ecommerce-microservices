#!/bin/bash

# Services Configuration
INFRA_SERVICES="zookeeper kafka kafka-init postgres postgres-auth mongodb redis prometheus grafana jaeger"
APP_SERVICES="authentication-service product-service order-service notification-service"

show_help() {
    echo "Usage: ./sequential-up.sh [up] [--build]"
    echo ""
    echo "Description:"
    echo "  Starts the environment sequentially in detached mode (-d) to save RAM/CPU."
    echo ""
    echo "Examples:"
    echo "  ./sequential-up.sh --build    # Rebuild and start everything"
    echo "  ./sequential-up.sh up         # Start existing containers"
    echo ""
}

# Show help if no arguments
if [ $# -eq 0 ] || [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
    show_help
    exit 0
fi

# Clean up arguments: remove 'up' or '-d' if the user typed them
# since we will enforce 'up -d' anyway.
CLEAN_ARGS=""
for arg in "$@"; do
    if [[ "$arg" != "up" && "$arg" != "-d" ]]; then
        CLEAN_ARGS="$CLEAN_ARGS $arg"
    fi
done

echo "🚀 [1/2] Starting Infrastructure..."
docker-compose up -d $INFRA_SERVICES

echo "⏳ Waiting 15s for stability..."
sleep 15

echo "🚀 [2/2] Starting Apps Sequentially..."
for service in $APP_SERVICES; do
    echo "👉 Processing [$service]..."
    # Always enforce 'up -d' and append remaining user flags (like --build)
    docker-compose up -d $CLEAN_ARGS $service
    
    if [ $? -ne 0 ]; then
        echo "❌ Error: Failed to start $service. Aborting."
        exit 1
    fi
    echo "✅ $service is up."
    echo "----------------------------------------"
done

echo "✨ Environment is ready!"