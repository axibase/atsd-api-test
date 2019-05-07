[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

# Running Tests

> Note: run tests in a freshly installed image.

## Prepare ATSD Container for Tests Running

Before creating the container , set the appropriate environment variables.

```
export ATSD_LOGIN=axibase
export ATSD_PASSWORD=axibase
```

Run a container from image preconfigured for tests.

```bash
docker run -d -p 8088:8088 -p 8443:8443 -p 8081:8081 --name="atsd-api-test" -e axiname="$ATSD_LOGIN" -e axipass="$ATSD_PASSWORD" \
-e timezone="Asia/Kathmandu" axibase/atsd:api_test
```

Alternatively, standard ATSD container can be configured to run tests.

```bash
docker run -d --name=<container-name> -p 8088:8088 -p 8443:8443 -p 8081:8081 -p 8082:8082/udp axibase/atsd:latest
```

* Set `last.insert.write.period.seconds` to 0 on the **Settings > Server Properties** page.
* Import and enable the [test rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) on the **Alerts > Rules** page.

## Run tests

```
export ATSD_HOST=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' atsd-api-test)
mvn clean test -Dmaven.test.failure.ignore=false -DserverName=$ATSD_HOST -Dlogin=$ATSD_LOGIN -Dpassword=$ATSD_PASSWORD -DloggerLevel=info
```

