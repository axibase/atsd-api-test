#Contribute guide

## Pre-requirements
1. An instance of ATSD.
2. A default settings of project are stored in `src/test/resources/client  .properties` file. By default you can redefine the following properties:
   ```properties
   # ATSD  instance properties
    login=username
    password=password
    protocol=http
    serverName=hbs.axibase.com
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

## Development

### Version control (git)
1. Name your remote branch with specified pattern `surname-issue_id` for example 'pushkin-1234'.
2. Pull request flow
    1. Before submit a `pull request`:
        1. `squash` all commits into a single commit
        2. `rebase` your branch on latest master
        3. run all tests on clear latest ATSD installation
    2. After your have received a `change request` submit a new single commit corresponding to requested changes 
    with a commit message `code review #number` where `number` is corresponding to a code review commit order.
    3. After your commit has been approved rebase your remote branch on actual master with a force push.
    4. If tests fail because of some unfixed ATSD bugs mark pull request with label `pending`, add message with issue numbers related to bugs and wait until all unfixed bugs will be fixed.

### Code style
Use [standard](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf) java code style.

  Each test must contain javadoc before test method declaration with a related issue number or a comment that this test was added directly bypassing corporate issue tracker.

```java
    /**
     * #1234
     */
    @Test
    public void testSomething() {
        //arrange
        
        //action
        
        //assert
    }
```

### Implementation specific
1. Use registers for unique name generation to guaranty that your tests are not overlapping with others.
2. Use special safe check methods for arrange step.
