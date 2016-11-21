package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.NotCheckedException;

import static org.testng.Assert.fail;


public class EntityTest extends EntityMethod {
    public static void assertEntityExisting(String message, Entity entity) {
        try {
            Checker.check(new EntityCheck(entity));
        } catch (NotCheckedException e) {
            fail(message);
        }
    }
}
