package com.axibase.tsd.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.StringUtils
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.internal.matchers.TypeSafeMatcher

object CSVMatcher {
    @JvmStatic
    fun eqCsv(expectedCsv: String?, format: CSVFormat? = CSVFormat.DEFAULT): Matcher<in String> {
        return object : TypeSafeMatcher<String?>() {
            private lateinit var errors: List<String>;
            private var isInit = false

            override fun matchesSafely(actualCsv: String?): Boolean {
                if (isInit) {
                    throw IllegalStateException("Same CSV matcher is used twice or more")
                } else {
                    errors = hasMismatchErrors(actualCsv)
                }
                isInit = true
                return errors.isEmpty()

            }

            override fun describeTo(description: Description) {
                description.appendText("\"${errors.joinToString(StringUtils.LF)}\"")
            }

            private fun hasMismatchErrors(actualCsv: String?): List<String> {
                val errors = mutableListOf<String>()
                val actualRecords = readRecords(actualCsv, format)
                val expectedRecords = readRecords(expectedCsv, format)
                if (actualRecords.size != expectedRecords.size) {
                    errors.add("row count (${expectedRecords.size}) but actual is ${actualRecords.size}")
                    return errors.toList()
                }
                for (i in actualRecords.indices) {
                    val actualRecord = actualRecords[i]
                    if (i >= expectedRecords.size) break
                    val expectedRecord = expectedRecords[i]
                    if (actualRecord.size() != expectedRecord.size()) {
                        errors.add("column count (${expectedRecords.size}) on row $i")
                        return errors
                    }
                    for (j in 0 until actualRecord.size()) {
                        val actualCell = actualRecord[j]
                        if (j >= expectedRecord.size()) break
                        val expectedCell = expectedRecord[j]
                        if (actualCell != expectedCell) {
                            errors.add("value '$expectedCell' is expected but '${actualCell}' found: row $i, column $j")
                        }
                    }
                }
                return errors.toList()
            }

            private fun readRecords(csv: String?, format: CSVFormat?): List<CSVRecord> {
                return csv?.reader().use {
                    CSVParser(it, format).records
                }
            }
        }
    }
}
