FROM adoptopenjdk/openjdk8:x86_64-ubuntu-jdk8u202-b08
MAINTAINER "Filip Bielejec" <nodrama.io>

COPY target/spread-server.jar .

# VOLUME /configs
# VOLUME /logs
# VOLUME /spread-vis
# VOLUME /spread-storage

# DEBUG
# RUN ls -l

# COPY /docker-builds/spread-server/check-write.sh .
# RUN chmod +x check-write.sh

# RUN ./check-write.sh /spread-storage
# RUN ./check-write.sh /spread-vis
# RUN ./check-write.sh /logs
# END: DEBUG

ENTRYPOINT ["java", "-jar", "spread-server.jar"]
CMD ["--spring.config.location=file:/configs/spread.properties"]
