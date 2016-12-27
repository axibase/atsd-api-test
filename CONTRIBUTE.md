###Contribute guide

To contribute to this project you must obey the following rules:

#### git
1. Name your remote branch with specified pattern `surname-issue_id` for example 'pushkin-1234'.
2. Pull request flow
    1. Before submit a `pull request`:
        1. `squash` all commits into a single commit
        2. `rebase` your branch on latest master
        3. run all tests on clear latest ATSD installation
    2. After your have received a `change request` submit a new single commit corresponding to requested changes 
    with a commit message `code review #number` where `number` is corresponding to a code review commit order.
    3. After your commit has been approved rebase your remote branch on actual master with a force push. 


#### code style
Use [standard](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf) java code style.
1. Each test must contain javadoc before test method declaration with a related issue number or a comment 
that this test was added directly bypassing corporate issue tracker.
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

#### implementation specific

1. Use registers for unique name generation to guaranty that your tests are not overlapping with others.
2. Use special safe check methods for arrange step.
