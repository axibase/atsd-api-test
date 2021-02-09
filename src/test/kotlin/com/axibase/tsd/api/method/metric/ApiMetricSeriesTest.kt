package com.axibase.tsd.api.method.metric

import com.axibase.tsd.api.method.metric.MetricMethod.queryMetricSeries
import com.axibase.tsd.api.method.series.SeriesMethod
import com.axibase.tsd.api.model.series.Sample
import com.axibase.tsd.api.model.series.Series
import com.axibase.tsd.api.util.Mocks
import com.axibase.tsd.api.util.TestUtil
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test


class ApiMetricSeriesTest {
    private val metric = Mocks.metric()
    private val entity1 = Mocks.entity()
    private val entity2 = Mocks.entity()
    private val defaultData = listOf(Sample.ofDateInteger("2021-02-04T09:40:24.978Z", 1))


    init {
        SeriesMethod.insertSeriesCheck(
            Series(entity1, metric, mapOf("tag" to "test")).apply {
                data = defaultData
            },
            Series(entity1, metric, mapOf("tag" to "test1")).apply {
                data = defaultData
            },
            Series(entity2, metric).apply {
                data = defaultData
            }
        )
    }

    @Test(dataProvider = "limitCases")
    fun testLimit(case: TestCase) {
        val response = queryMetricSeries(metric, case.params);
        assertThat(response.size, equalTo(case.limit))
    }

    @DataProvider
    private fun limitCases(): Array<Array<Any?>> {
        return TestUtil.convertTo2DimArray(
            listOf(
                TestCase(MetricSeriesParameters().apply { limit = 1 }, 1),
                TestCase(MetricSeriesParameters().apply { entity = entity1 }, 2),
                TestCase(MetricSeriesParameters().apply { tags = mapOf("tag" to "test") }, 1)
            )
        )
    }

    data class TestCase(val params: MetricSeriesParameters?, val limit: Int)
}
