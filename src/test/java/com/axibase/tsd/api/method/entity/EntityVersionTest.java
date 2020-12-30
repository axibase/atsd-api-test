package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.Mocks;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EntityVersionTest extends EntityMethod {

    @Test
    public void test() throws Exception {
        String entityName = Mocks.entity();
        Entity entity = new Entity(entityName);
        entity.setEnabled(true);
        createOrReplaceEntity(entity);
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("a", "b");
        entity.setTags(tags);
        updateEntity(entity);
        tags.put("c", "d");
        updateEntity(entity);
        entity.setInterpolationMode(InterpolationMode.LINEAR);
        updateEntity(entity);

        List<Long> versions = getEntityVersions(entityName, null, null);
        assertEquals(versions.size(), 4);

        long timeOne = versions.get(0);
        long timeTwo = versions.get(1);
        long timeThree = versions.get(2);
        long timeFour = versions.get(3);

        List<Long> expected = Arrays.asList(timeOne, timeTwo);
        versions = getEntityVersions(entityName, null, timeTwo);
        assertEquals(versions, expected);

        expected = Arrays.asList(timeThree, timeFour);
        versions = getEntityVersions(entityName, timeThree, null);
        assertEquals(versions, expected);

        expected = Arrays.asList(timeTwo, timeThree);
        versions = getEntityVersions(entityName, timeTwo, timeThree);
        assertEquals(versions, expected);

        Entity actual = getEntity(entityName);
        assertTrue(EqualsBuilder.reflectionEquals(actual, entity, "createdDate", "versionDate"));
        assertEquals(actual.getCreatedDate().toInstant().toEpochMilli(), timeOne);
        assertEquals(actual.getVersionDate().toInstant().toEpochMilli(), timeFour);

        actual = getEntity(entityName, timeFour);
        assertTrue(EqualsBuilder.reflectionEquals(actual, entity, "createdDate", "versionDate"));
        assertEquals(actual.getCreatedDate().toInstant().toEpochMilli(), timeOne);
        assertEquals(actual.getVersionDate().toInstant().toEpochMilli(), timeFour);

        entity.setInterpolationMode((InterpolationMode) null);
        actual = getEntity(entityName, timeThree);
        assertTrue(EqualsBuilder.reflectionEquals(actual, entity, "createdDate", "versionDate"));
        assertEquals(actual.getCreatedDate().toInstant().toEpochMilli(), timeOne);
        assertEquals(actual.getVersionDate().toInstant().toEpochMilli(), timeThree);

        entity.getTags().remove("c");
        actual = getEntity(entityName, timeTwo);
        assertTrue(EqualsBuilder.reflectionEquals(actual, entity, "createdDate", "versionDate"));
        assertEquals(actual.getCreatedDate().toInstant().toEpochMilli(), timeOne);
        assertEquals(actual.getVersionDate().toInstant().toEpochMilli(), timeTwo);

        entity.getTags().clear();
        actual = getEntity(entityName, timeOne);
        assertTrue(EqualsBuilder.reflectionEquals(actual, entity, "createdDate", "versionDate"));
        assertEquals(actual.getCreatedDate().toInstant().toEpochMilli(), timeOne);
        assertEquals(actual.getVersionDate().toInstant().toEpochMilli(), timeOne);
    }
}