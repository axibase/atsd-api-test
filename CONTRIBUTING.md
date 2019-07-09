# Contributing guide

## Pre-requirements
1. An instance of ATSD `api_test` version. You can install it using [docker image](https://hub.docker.com/r/axibase/atsd).
   ```bash
   docker pull axibase/atsd:api_test
   docker run -d -p 8088:8088 -p 8081:8081 --name atsd_api_test_container -e axiname="axibase" -e axipass="password" -e timezone="time_zone_id" axibase/atsd:api_test
   ```
2. A default settings of project are stored in `src/test/resources/client  .properties` file. By default you can redefine the following properties:
   ```properties
   # ATSD  instance properties
    login=username
    password=password
    protocol=http
    serverName=atsd.host
    httpPort=8088
    tcpPort=8081
    apiPath=/api/v1
    # App settings
    loggerLevel=info
    ```

3. For development purposes you should create `dev.client.properties` file and define settings that you need. Also you can redefine specific properties by maven `-D{properties.name}=properties.value`  construction. 

4. Then you can run test:
   ```bash
   mvn clean test
   ```

#### IDE note.

If you use IntelliJ IDEA for development you should install Lombok plugin for proper code inspection and completion.

## Development

### Version control (git)
1. Name your remote branch with specified pattern `last_name-issue_id` for example 'pushkin-1234'.
2. Pull request flow

    a. Before submit a `pull request`:
    
        1. `squash` all commits into a single commit
        2. `rebase` your branch on latest master
        3. run all tests on clear latest ATSD installation
    b. If your have received a `change request` submit a new single commit corresponding to requested changes 
    with a commit message `code review #number` where `number` is corresponding to a code review commit order.
    
    c. If tests fail because of some unfixed ATSD bugs mark pull request with label `pending`, add message with issue numbers related to bugs and wait until all unfixed bugs will be fixed.

### Code style
Use [Axibase](./STYLEGUIDE.md) Java code style.

  Each test must contain an Issue annotation with related issue number or a comment that this test was added directly
  bypassing corporate issue tracker.
  Always write test description for new tests - as complete as possible.
  It is very helpful in case when someone tries to understand why
  the test fails and how to fix it properly.

```java
    @Issue("1234")
    @Test(description = "My explanatory test description")
    public void testSomething() {
        //arrange
        
        //action
        
        //assert
    }
```

### Implementation specific
1. Use registers for unique name generation to guarantee that your tests are not overlapping with others.
2. Use special safe check methods for arrange step.
