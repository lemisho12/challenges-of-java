#!/bin/bash

# ========================================
#   Personal Diary Manager
# ========================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Personal Diary Manager${NC}"
echo -e "${BLUE}========================================${NC}"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH.${NC}"
    echo "Please install Java 11 or higher:"
    echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "  Fedora/RHEL: sudo dnf install java-17-openjdk"
    echo "  Mac: brew install openjdk@17"
    echo "  Or download from: https://adoptium.net/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d. -f1)

if [[ "$JAVA_MAJOR" -lt 11 ]]; then
    echo -e "${RED}Error: Java version $JAVA_MAJOR is too old.${NC}"
    echo "Please install Java 11 or higher."
    exit 1
fi

echo -e "${GREEN}Java version: $JAVA_VERSION${NC}"

# Set JavaFX path
JAVAFX_PATH="lib/javafx-sdk-17.0.6/lib"
if [[ ! -d "$JAVAFX_PATH" ]]; then
    echo -e "${RED}Error: JavaFX SDK not found at $JAVAFX_PATH${NC}"
    echo "Please download JavaFX SDK 17 from:"
    echo "  https://gluonhq.com/products/javafx/"
    echo "and extract it to the 'lib' folder."
    exit 1
fi

# Create data directory if it doesn't exist
if [[ ! -d "data" ]]; then
    mkdir -p data/entries
    echo -e "${GREEN}Created data directories.${NC}"
fi

# Check for build directory
if [[ ! -d "build/classes/java/main" ]]; then
    echo -e "${YELLOW}Warning: Build directory not found. Attempting to compile...${NC}"
    
    if command -v gradle &> /dev/null; then
        gradle build
    elif command -v mvn &> /dev/null; then
        mvn compile
    else
        echo -e "${RED}Error: Neither Gradle nor Maven found. Please build the project first.${NC}"
        exit 1
    fi
fi

# Run the application
echo -e "${GREEN}Starting Personal Diary Manager...${NC}"
echo

java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml,javafx.web \
     -cp "build/classes/java/main:build/resources/main:lib/*" \
     com.diary.manager.Main

EXIT_CODE=$?

if [[ $EXIT_CODE -ne 0 ]]; then
    echo
    echo -e "${RED}Application exited with error code $EXIT_CODE${NC}"
    read -p "Press Enter to continue..."
fi

exit $EXIT_CODE