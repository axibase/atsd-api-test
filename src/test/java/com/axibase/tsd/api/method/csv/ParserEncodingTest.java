package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class ParserEncodingTest extends CSVUploadMethod {
    private static final String RESOURCE_DIR = "parser_encoding";
    private static final String ENTITY_PREFIX = "e-csv-test-encoding-parser";
    public static final String PARSER_NAME = "test-encoding-parser";

    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String WINDOWS_1251 = "Windows-1251";

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException {
        File configPath = resolvePath(RESOURCE_DIR + File.separator + PARSER_NAME+".xml");
        boolean success = importParser(configPath);
        assertTrue(success);
    }

    /* #2916 */
    @Test
    public void testCsvCorrectTextEncodingISO8859_1(Method method) throws Exception {
        String controlSequence = "¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
        String entityName = ENTITY_PREFIX+"-1";
        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        checkCsvCorrectTextEncoding(controlSequence, entityName, csvPath, ISO_8859_1);
    }

    /* #2916 */
    @Test
    public void testCsvCorrectTextEncodingWindows1251(Method method) throws Exception {
        String controlSequence = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        String entityName = ENTITY_PREFIX+"-2";
        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        checkCsvCorrectTextEncoding(controlSequence, entityName, csvPath, WINDOWS_1251);
    }

    private void checkCsvCorrectTextEncoding(String controlSequence, String entityName, File csvPath, String textEncoding) throws InterruptedException, IOException {
        Registry.Entity.registerPrefix(entityName);

        Response response = binaryCsvUpload(csvPath, PARSER_NAME, textEncoding, null);

        assertEquals(response.getStatus(), OK.getStatusCode());

        Thread.sleep(1000L);

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate(Util.MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(Util.MAX_QUERYABLE_DATE);
        List<Message> storedMessageList = MessageMethod.executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});

        assertEquals("Unexpected message body", controlSequence, storedMessageList.get(0).getMessage());
    }

}
