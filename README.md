[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

Run tests in a freshly installed Docker container with image for tests:
```bash
docker run -d --name="atsd-api-test" -e axiname="$ATSD_LOGIN" -e axipass="$ATSD_PASSWORD" \
-e timezone="Asia/Kathmandu" axibase/atsd:api_test
```
