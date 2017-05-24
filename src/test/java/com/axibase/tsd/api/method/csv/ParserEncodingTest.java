package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageQuerySize;
import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class ParserEncodingTest extends CSVUploadMethod {
    public static final String PARSER_NAME = "test-encoding-parser";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String WINDOWS_1251 = "Windows-1251";
    private static final String RESOURCE_DIR = "parser_encoding";
    private static final String ENTITY_PREFIX = "e-csv-test-encoding-parser";

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException {
        File configPath = resolvePath(RESOURCE_DIR + File.separator + PARSER_NAME + ".xml");
        boolean success = importParser(configPath);
        assertTrue(success);
    }

    /* #2916 */
    @Test
    public void testCsvCorrectTextEncodingISO8859_1(Method method) throws Exception {
        String controlSequence = "¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
        String entityName = ENTITY_PREFIX + "-1";
        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        checkCsvCorrectTextEncoding(controlSequence, entityName, csvPath, ISO_8859_1);
    }

    /* #2916 */
    @Test
    public void testCsvCorrectTextEncodingWindows1251(Method method) throws Exception {
        String controlSequence = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        String entityName = ENTITY_PREFIX + "-2";
        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");
        checkCsvCorrectTextEncoding(controlSequence, entityName, csvPath, WINDOWS_1251);
    }

    private void checkCsvCorrectTextEncoding(String controlSequence, String entityName, File csvPath, String textEncoding) throws Exception {
        Registry.Entity.register(entityName);
        Response response = binaryCsvUpload(csvPath, PARSER_NAME, textEncoding, null);
        assertEquals(response.getStatus(), OK.getStatusCode());
        MessageQuery query = new MessageQuery();
        query.setEntity(entityName);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        assertMessageQuerySize(query, 1);
        List<Message> messageList = MessageMethod.queryMessage(query);
        assertEquals("Unexpected message body", messageList.get(0).getMessage(), controlSequence);
    }
}
