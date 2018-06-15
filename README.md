[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

Run tests in a freshly installed Docker container with image for tests:

```bash
docker run -d -p 8088:8088 -p 8443:8443 -p 8081:8081 --name="atsd-api-test" -e axiname="$ATSD_LOGIN" -e axipass="$ATSD_PASSWORD" \
-e timezone="Asia/Kathmandu" axibase/atsd:api_test
```
