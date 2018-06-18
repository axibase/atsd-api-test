#!/usr/bin/env bash

export ATSD_LOGIN=axibase
export ATSD_PASSWORD=axibase
atsd_http_port=50088
atsd_tcp_port=50081

default_timezone="Asia/Kathmandu"
name=atsd-api-tests
atsd_host=localhost
check_url=http://${atsd_host}:${atsd_http_port}/version

# Check url http code with passed credentials
function url_http_status {
    url=$1
    login=$2
    password=$3
    echo $(curl -u ${login}:${password} \
    -w "%{http_code}" \
    -s \
    -o /dev/null \
    ${url})
}


function start_container {
    atsd_login=$1
    atsd_password=$2
    atsd_http_port=$3
    docker run -d --name=${name} \
           -p ${atsd_http_port}:8088 \
           -p ${atsd_tcp_port}:8081 \
           -e axiname=${atsd_login} \
           -e axipass=${atsd_password} \
           -e timezone=${default_timezone} \
           -e JAVA_OPTS="-Xmx4G"\
           axibase/atsd:api_test
    # Wait while container is not starting up
    while [[ $(url_http_status ${check_url} ${ATSD_LOGIN} ${ATSD_PASSWORD}) != 200 ]]; do
        echo "Waiting to start ${name} container ...";
        sleep 3;
    done
    echo "Container is started"
}


#docker rm -f ${name}
#start_container ${ATSD_LOGIN} ${ATSD_PASSWORD} ${atsd_http_port}

curl --user ${ATSD_LOGIN}:${ATSD_PASSWORD} ${check_url}
mvn clean test \
    -Dmaven.test.failure.ignore=false \
    -DserverName=${atsd_host} \
    -DhttpPort=${atsd_http_port} \
    -DtcpPort=${atsd_tcp_port} \
    -Dlogin=${ATSD_LOGIN} \
    -Dpassword=${ATSD_PASSWORD} \
    -DloggerLevel=info
