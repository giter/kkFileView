docker run --rm -v /root/.m2:/root/.m2 -v $(pwd):/app -w /app maven:3-openjdk-8 mvn clean

