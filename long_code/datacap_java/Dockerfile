FROM eclipse-temurin:11-jdk-focal

LABEL maintainer="community@devlive.org" \
      description="DataCap Server Image"

# Add datacap
RUN mkdir -p /opt/app
ADD --chown=root:root dist/datacap-server-*.tar.gz /opt/app/
RUN cd /opt/app && \
    mv datacap-server-* datacap

WORKDIR /opt/app/datacap

EXPOSE 9096

ENTRYPOINT ["sh", "./bin/debug.sh"]