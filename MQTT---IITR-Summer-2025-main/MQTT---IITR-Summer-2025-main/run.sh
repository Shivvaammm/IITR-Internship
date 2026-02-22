#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "${GREEN}Building the project with Maven...${NC}"

# Build the project using Maven
mvn clean package

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo "${GREEN}Build successful! Running the application...${NC}"
    
    JAR_FILE="target/testMqtt-1.0-SNAPSHOT-jar-with-dependencies.jar"
    
    if [ -f "$JAR_FILE" ]; then
        # Run the JAR file
        java -jar "$JAR_FILE"
    else
        echo "${RED}JAR file not found: $JAR_FILE${NC}"
        exit 1
    fi
else
    echo "${RED}Build failed!${NC}"
    exit 1
fi
