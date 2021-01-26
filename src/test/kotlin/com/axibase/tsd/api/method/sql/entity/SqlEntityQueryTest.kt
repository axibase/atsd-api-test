package com.axibase.tsd.api.method.sql.entity

import com.axibase.tsd.api.method.entity.EntityMethod
import com.axibase.tsd.api.method.sql.SqlTest
import com.axibase.tsd.api.model.common.InterpolationMode
import com.axibase.tsd.api.model.entity.Entity
import com.axibase.tsd.api.util.Mocks
import org.testng.annotations.BeforeClass
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
        run {
            val entity = Entity(entityOne)
            entity.label = "entity_query_test_1"
            entity.interpolationMode = InterpolationMode.PREVIOUS
            entity.timeZoneID = "UTC"
            entity.addTag("class_code", classCodeOne)
            entity.addTag("symbol", "symbol_$entityOne")
            entity.addTag("lot_size", "5")
            EntityMethod.createOrReplaceEntityCheck(entity)
        }

        run {
            val entity = Entity(entityTwo)
            entity.addTag("class_code", classCodeTwo)
            entity.addTag("symbol", "symbol_$entityTwo")
            entity.addTag("lot_size", "10")
            entity.label = "entity_query_test_2"
            EntityMethod.createOrReplaceEntityCheck(entity)
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
                "class_code=class_${entityOne};lot_size=5;symbol=symbol_${entityOne}"
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
    fun `test name and tag conditions`() {
        val query = "select name from atsd_entity where " +
                "name like '%sql-entity-query-test%' " +
                "and tags.class_code like 'class%' " +
                "and tags.symbol = 'symbol_$entityTwo' " +
                "and tags.lot_size > 7"
        val expectedResult = arrayOf(arrayOf(entityTwo))
        assertSqlQueryRows("Unexpected result", expectedResult, query)
    }

}