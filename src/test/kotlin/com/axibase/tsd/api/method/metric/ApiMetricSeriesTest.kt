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

private const val DEFAULT_LIMIT = 5000;

class ApiMetricSeriesTest {
    private val metric1 = Mocks.metric()
    private val entity1 = Mocks.entity()
    private val entity2 = Mocks.entity()
    private val entity3 = Mocks.entity()
    private val defaultData = listOf(Sample.ofDateInteger("2021-02-04T09:40:24.978Z", 1))


    init {
        SeriesMethod.insertSeriesCheck(
            Series(entity1, metric1, mapOf("tag" to "test")).apply {
                data = defaultData
            },
            Series(entity1, metric1, mapOf("tag" to "test1")).apply {
                data = defaultData
            },
            Series(entity2, metric1).apply {
                data = defaultData
            }
        )

        val series = (1..DEFAULT_LIMIT)
            .map { "t" to "$it" }
            .map {
                Series().apply {
                    metric = metric1
                    entity = entity3
                    tags = mapOf(it)
                    data = defaultData
                }
            }
        SeriesMethod.insertSeriesCheck(series)
    }

    @Test(dataProvider = "limitCases")
    fun testLimit(case: TestCase) {
        val response = queryMetricSeries(metric1, case.params);
        assertThat(response.size, equalTo(case.limit))
    }

    @DataProvider
    private fun limitCases(): Array<Array<Any?>> {
        return TestUtil.convertTo2DimArray(
            listOf(
                TestCase(MetricSeriesParameters().apply { limit = 1 }, 1),
                TestCase(MetricSeriesParameters().apply { entity = entity1 }, 2),
                TestCase(MetricSeriesParameters().apply { entity = entity2 }, 1),
                TestCase(MetricSeriesParameters().apply { tags = mapOf("tag" to "test") }, 1),
                TestCase(null, DEFAULT_LIMIT),
                TestCase(MetricSeriesParameters().apply { limit = 5003 }, 5003)
            )
        )
    }

    data class TestCase(val params: MetricSeriesParameters?, val limit: Int)
}
