[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

# Running Tests

## Regular Docker Image

Note: run tests in a freshly installed image.

* Install Docker image:

```bash
docker run -d --name=<container-name> -p 8088:8088 -p 8443:8443 -p 8081:8081 -p 8082:8082/udp axibase/atsd:latest
```

* Set `last.insert.write.period.seconds` to 0 on the **Settings > Server Properties** page.
* Import and enable the [test rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) on the **Alerts > Rules** page.
* Run tests

## Special Docker Image

Note: run tests in a freshly installed image.

* Install the image:

```bash
docker run -d -p 8088:8088 -p 8443:8443 -p 8081:8081 --name="atsd-api-test" -e axiname="$ATSD_LOGIN" -e axipass="$ATSD_PASSWORD" \
-e timezone="Asia/Kathmandu" axibase/atsd:api_test
```

* Run tests

## Package

Before running the tests, configure the ATSD server:

* Set `last.insert.write.period.seconds` to 0 on the **Settings > Server Properties** page.
* Import and enable the [test rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) on the **Alerts > Rules** page.

After running the tests delete all `atsd_*` tables from the database:

* Stop ATSD:

```bash
/opt/atsd/bin/atsd-tsd.sh stop
```

* Verify that ATSD is stopped:

```bash
jps
```

If `Server` process is present in the output, kill it forcefully with ``kill -9 {Server-PID}``.

* Disable all `atsd_*` tables using regex:

```bash
echo "disable_all 'atsd_.*' \n y" | /opt/atsd/hbase/bin/hbase shell
```

* Drop all previously disabled tables:

```bash
echo "drop_all 'atsd_.*' \n y" | /opt/atsd/hbase/bin/hbase shell
```

* Verify that no `atsd_*` tables are present:

```bash
echo "list" | /opt/atsd/hbase/bin/hbase shell
```

* Start ATSD:

```bash
/opt/atsd/bin/atsd-tsd.sh start
```

Note: Since the server configuration was stored in a deleted table, do not forget to re-configure the server.
