FROM gradle:latest

# Install Groovy and Maven
RUN apt-get update && apt-get install -y \
    groovy \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy project files
COPY . /app

# Ensure gradlew is executable
RUN chmod +x gradlew