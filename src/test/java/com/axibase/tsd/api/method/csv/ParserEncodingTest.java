package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
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
        Registry.Entity.registerPrefix(entityName);
        Response response = binaryCsvUpload(csvPath, PARSER_NAME, textEncoding, null);
        assertEquals(response.getStatus(), OK.getStatusCode());
        Message message = new Message();
        message.setEntity(entityName);
        message.setMessage("Unexpected message body");
        assertMessageExisting(message);
    }
}
