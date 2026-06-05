# start with h2
cd path/to/project_TB001
./target/project_tb001 --spring.profiles.active=h2

# start with postgre
cd path/to/project_TB001
docker compose up -d
./target/project_tb001 --spring.profiles.active=postgres

#test api
curl -i http://localhost:8080/

# first time compile
cd path/to/project_TB001
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home
/Users/brucechen/apache-maven-3.9.1/bin/mvn -Pnative native:compile -DskipTests

# re compile
cd path/to/project_TB001
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
/Users/brucechen/apache-maven-3.9.1/bin/mvn -DskipTests package
java -jar target/*-0.0.1-SNAPSH
