In order to pass all of the tests, set "last.insert.write.period.seconds" to 0 in Server Properties: 

https://astd.host:8443/admin/serverproperties

Also, [the rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) should be imported (https://astd.host:8443/rules/import) and enabled.

[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)