#!/bin/bash

echo "======================================"
echo "FirstClub Membership Program"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed."
    echo "Please install Java 17 or higher from https://adoptium.net/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required."
    echo "Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version check: OK"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed."
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    echo "Or use your package manager:"
    echo "  - macOS: brew install maven"
    echo "  - Ubuntu/Debian: sudo apt-get install maven"
    echo "  - CentOS/RHEL: sudo yum install maven"
    exit 1
fi

echo "Maven check: OK"
echo ""

# Build the application
echo "Building the application..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Build successful!"
echo ""

# Run the application
echo "Starting the application..."
echo "The application will be available at http://localhost:8080"
echo "H2 Console will be available at http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run
