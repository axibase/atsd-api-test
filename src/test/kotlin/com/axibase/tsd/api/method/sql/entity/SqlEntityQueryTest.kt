package com.axibase.tsd.api.method.sql.entity

import com.axibase.tsd.api.method.entity.EntityMethod
import com.axibase.tsd.api.method.sql.SqlTest
import com.axibase.tsd.api.model.common.InterpolationMode
import com.axibase.tsd.api.model.entity.Entity
import com.axibase.tsd.api.util.DateProcessorManager
import com.axibase.tsd.api.util.Mocks
import org.testng.annotations.BeforeClass
import java.time.ZoneId
import kotlin.test.Test

private val prefix = Mocks.entity()
private val entityOne = prefix + Mocks.entity()
private val entityTwo = prefix + Mocks.entity()
private val entityThree = prefix + Mocks.entity()
private val classCodeOne = "class_$entityOne"
private val classCodeTwo = "class_$entityTwo"

class SqlEntityQueryTest : SqlTest() {

    @BeforeClass
    fun setup() {
        val entityOneBean = Entity(entityOne)
        val entityTwoBean = Entity(entityTwo)
        run {
            val entity = entityOneBean
            entity.label = "entity_query_test_1"
            entity.interpolationMode = InterpolationMode.PREVIOUS
            entity.timeZoneID = "UTC"
            entity.addTag("class_code", classCodeTwo)
            entity.addTag("symbol", "symbol_$entityOne")
            entity.addTag("lot_size", "5")
            entity.addTag("primary", classCodeOne)
            EntityMethod.createOrReplaceEntityCheck(entity)
        }

        run {
            val entity = entityTwoBean
            entity.addTag("class_code", classCodeTwo)
            entity.addTag("symbol", "symbol_$entityTwo")
            entity.addTag("lot_size", "5")
            entity.label = "entity_query_test_2"
            EntityMethod.createOrReplaceEntityCheck(entity)
        }

        Thread.sleep(2000)
        run {
            val entity = entityTwoBean
            entity.addTag("lot_size", "10")
            EntityMethod.updateEntity(entity)
        }

        run {
            val entity = entityOneBean
            entity.addTag("class_code", classCodeOne)
            EntityMethod.updateEntity(entity)
        }


        run {
            val entity = Entity(entityThree)
            entity.label = "entity_query_test_3"
            EntityMethod.createOrReplaceEntityCheck(entity)
        }
    }

    @Test
    fun `test select all`() {
        val expected = EntityMethod.getEntity(entityOne)
        val query = "select * from atsd_entity where name = '${entityOne}'"
        val expectedResult = arrayOf(arrayOf(
                entityOne,
                "entity_query_test_1",
                "${expected.createdDate.toInstant().toEpochMilli()}",
                "${expected.versionDate.toInstant().toEpochMilli()}",
                "UTC",
                "PREVIOUS",
                null,
                "true",
                "class_code=class_${entityOne};lot_size=5;primary=class_${entityOne};symbol=symbol_${entityOne}"
        ))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag present condition`() {
        val query = "select name from atsd_entity where name like '${prefix}%' and tags.class_code != ''"
        val expectedResult = arrayOf(arrayOf(entityOne), arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag condition equal`() {
        val query = "select name from atsd_entity where  tags.class_code = '$classCodeTwo'"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag condition in`() {
        val query = "select name from atsd_entity where  tags.class_code in ('$classCodeTwo', '$classCodeOne')"
        val expectedResult = arrayOf(arrayOf(entityOne), arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag condition in single element`() {
        val query = "select name from atsd_entity where  tags.class_code in ('$classCodeOne')"
        val expectedResult = arrayOf(arrayOf(entityOne))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag condition not in`() {
        val query = "select name from atsd_entity where name in ('$entityOne', '$entityTwo', '$entityThree') and tags.class_code not in ('$classCodeOne')"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tag condition not like`() {
        val query = "select name from atsd_entity where name in ('$entityOne', '$entityTwo', '$entityThree') and tags.class_code not like '$classCodeOne'"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test name and tag conditions`() {
        val query = "select name from atsd_entity where " +
                "name like '%sql-entity-query-test%' " +
                "and tags.class_code like 'class%' " +
                "and tags.symbol = 'symbol_$entityTwo' " +
                "and tags.lot_size > 7"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test name and tag conditions string comparison`() {
        val query = "select name from atsd_entity where " +
                "name like '%sql-entity-query-test%' " +
                "and tags.class_code like 'class%' " +
                "and tags.symbol = 'symbol_$entityTwo' " +
                "and tags.lot_size > '07'"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test tags comparison`() {
        val query = "select name from atsd_entity where tags.class_code = '$classCodeOne' and tags.primary = tags.class_code"
        val expectedResult = arrayOf(arrayOf(entityOne))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test subquery`() {
        val query = "select class_code from (select tags.class_code as class_code, tags.symbol from atsd_entity where  name like '${prefix}%' and tags.class_code != '')"
        val expectedResult = arrayOf(arrayOf(classCodeOne), arrayOf(classCodeTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test aggregation subquery`() {
        val query = "select base_symbol, sum(lot_size), count(*) from " +
                "(select tags.lot_size as lot_size, SUBSTR(tags.symbol, 1,6) AS base_symbol from atsd_entity where name like '${prefix}%' and tags.class_code != '') " +
                "group by base_symbol"
        val expectedResult = arrayOf(arrayOf("symbol", "15", "2"))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test entity_tag function with version parameter`() {
        val versions = EntityMethod.getEntityVersions(entityTwo, null, null)
        val query = "select entity_tag(entity, 'lot_size', ${versions[0]}), entity_tag(entity, 'lot_size', ${versions[1]}), entity_tag(entity, 'lot_size')  from atsd_entity where name = '$entityTwo'"
        val expectedResult = arrayOf(arrayOf("5", "10", "10"))
        assertSqlQueryRows("Unexpected result", expectedResult, query)

        val versionAsIsoStringOne = DateProcessorManager.ISO.print(versions[0], ZoneId.of("UTC"));
        val versionAsIsoStringTwo = DateProcessorManager.ISO.print(versions[1], ZoneId.of("UTC"));
        val sql = "select entity_tag(entity, 'lot_size', '$versionAsIsoStringOne'), entity_tag(entity, 'lot_size', '$versionAsIsoStringTwo'), entity_tag(entity, 'lot_size')  from atsd_entity where name = '$entityTwo'"
        assertSqlQueryRows("Unexpected result", expectedResult, sql)
    }

    @Test
    fun `test WITH VERSION option`() {
        val versions = EntityMethod.getEntityVersions(entityTwo, null, null)
        val version = DateProcessorManager.ISO.print(versions[0], ZoneId.of("UTC"));
        val query = "select tags.lot_size, entity_tag(entity, 'lot_size'), entity_tag(entity, 'lot_size', ${versions[1]})   from atsd_entity where name = '$entityTwo' WITH VERSION = '$version'"
        val expectedResult = arrayOf(arrayOf("5", "5", "10"))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test WITH VERSION option end time`() {
        val versions = EntityMethod.getEntityVersions(entityTwo, null, null)
        val seconds = (System.currentTimeMillis() - versions[0]) / 1000
        val query = "select tags.lot_size, entity_tag(entity, 'lot_size'), entity_tag(entity, 'lot_size', ${versions[1]})   from atsd_entity where name = '$entityTwo' WITH VERSION = now - $seconds * SECOND"
        val expectedResult = arrayOf(arrayOf("5", "5", "10"))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

    @Test
    fun `test WITH VERSION option where clause`() {
        val versions = EntityMethod.getEntityVersions(entityTwo, null, null)
        val version = DateProcessorManager.ISO.print(versions[0], ZoneId.of("UTC"))
        val query = "select name, tags.lot_size from atsd_entity where tags.class_code in ('$classCodeOne', '$classCodeTwo') and entity_tag(entity, 'lot_size') = '5' WITH VERSION = '$version' order by name"
        val expectedResult = arrayOf(arrayOf(entityOne, "5"), arrayOf(entityTwo, "5"))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

}