[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

ATSD configuration<a name="rule"></a>
==================
The API tests require the target ATSD to be properly configured:

1. Set `last.insert.write.period.seconds` to 0 on the **Settings > Server Properties** page.

2. Import and enable the [test rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) on the **Alerts > Rules** page.

API tests re-run
----------------

### Docker
In order to re-run API tests, re-initialize the Docker container:

* Remove the container.

```bash
docker rm -vf <container-name>
```

* Update Update the ATSD image to the latest revision.

```bash
docker pull axibase/atsd:latest
```
* Install ATSD.

```bash
docker run -d --name=<container-name> -p 8088:8088 -p 8443:8443 -p 8081:8081 \
-p 8082:8082/udp axibase/atsd:latest
```

* Set [correct server properties](#rule) and re-import the rule.
### Packages 
* Login into ATSD server.

```bash
ssh -p <ssh_port> <user>@<server_address>
```

* Stop ATSD.

```bash
/opt/atsd/bin/atsd-tsd.sh stop
```

* Verify that ATSD is stopped.

```bash
jps
```

If `Server` process is present in the output, kill it forcefully with 

```bash
kill -9 {Server-pid}
```

* Change directory to /opt/atsd/hbase/bin.

```bash
cd /opt/atsd/hbase/bin
```

* Open HBase shell.

```bash
/opt/atsd/hbase/bin/hbase shell
```

* Execute the specified commands to disable and delete atsd tables using wildcard.

```bash
disable_all 'atsd_*'
```
```bash
drop_all 'atsd_*'
```

* Verify that no atsd tables are present

```bash
list
```

* Close HBase shell.

```bash
exit
```

* Start ATSD

```bash
/opt/atsd/bin/atsd-tsd.sh start
```

* Set [correct server properties](#rule) (they were stored in one of the deleted tables) and re-import the rule.
