../gradlew assemble
cd build/distributions || (echo "ERROR cd build/distributions" && exit 1)
tar xf gemini-terminal-1.0-SNAPSHOT.tar
cd gemini-terminal-1.0-SNAPSHOT || (echo "ERROR cd gemini-terminal-1.0-SNAPSHOT" && exit 1)
