[![Build Status](https://travis-ci.org/axibase/atsd-api-test.svg?branch=master)](https://travis-ci.org/axibase/atsd-api-test)

The API tests require the target ATSD to be properly configured:

1. Set `last.insert.write.period.seconds` to 0 on the **Settings > Server Properties** page.

2. Import and enable the [test rule](https://raw.githubusercontent.com/axibase/dockers/atsd_api_test/rules.xml) on the **Alerts > Rules** page.
