FROM ubuntu:bionic

# Create Server OS environment
RUN apt-get update -y &&\
    apt-get upgrade -y &&\
    apt-get install -y openjdk-8-jdk

# App source
COPY /target/universal/f9s_service /src
COPY /app/resources/timetables /src/app/resources/timetables
WORKDIR /src/f9s_service/bin

EXPOSE 8080

CMD ./f9s_service -Dhttp.port=8080 -Dhttp.address=0.0.0.0 -Dplay.http.secret.key=ad31779d4ee49d5ad5162bf1429c32e2e9933f3b
