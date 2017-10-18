package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TcpParsingTestLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COMMANDS_FILE = "commands.txt";
    private static final String METRICS_DIR = "metrics";
    private static final String SERIES_DIR = "series";

    private static final Random RANDOM_GENERATOR = new Random(8300833249730420964L);

    private static final int TOTAL_COMMANDS = 10;
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 40;
    private static final int MIN_METRICS = 1;
    private static final int MAX_METRICS = 200;

    private static int randomCounter = 1;
    private static int randomUnicodeCounter = 1;

    /* Refer https://github.com/axibase/atsd/blob/master/api/network/base-abnf.md for details*/
    private static final List<Character> CHAR_SAFE = charsFromRanges(
            new int[][] {
                    {0x21, 0x21},
                    {0x23, 0x29},
                    {0x2b, 0x3c},
                    {0x3e, 0x3e},
                    {0x40, 0x5b},
                    {0x5d, 0x7e},
            }
    );

    private static final List<Character> CHAR_UNICODE = charsFromRanges(
            new int[][] {
                    {0x80, 0xff},
                    {0x100, 0x17f},
                    {0x370, 0x3ff},
                    {0x400, 0x4ff},
                    {0x500, 0x52f},
                    {0x4e00, 0x9fff},
            }
    );

    private static final List<Character> CHAR_NAME = extendCharRanges(
            CHAR_SAFE,
            new int[][] {
                    {'=', '='},
                    {'"', '"'},
            }
    );
    private static final List<Character> CHAR_NAME_UNICODE = extendCharRanges(CHAR_NAME, CHAR_UNICODE);

    private static final List<Character> CHAR_VALUE = extendCharRanges(
            CHAR_NAME,
            new int[][] {
                    {' ', ' '},
                    {'\n', '\n'},
            }
    );
    private static final List<Character> CHAR_VALUE_UNICODE = extendCharRanges(CHAR_VALUE, CHAR_UNICODE);

    private static List<Character> charsFromRanges(int[][] ranges) {
        return extendCharRanges(new ArrayList<>(), ranges);
    }

    private static List<Character> extendCharRanges(List<Character> characterList, int[][] ranges) {
        List<Character> extendedList = new ArrayList<>(characterList);
        for (int[] range : ranges) {
            for (char j = (char) range[0]; j <= range[1]; j++) {
                extendedList.add(j);
            }
        }
        return extendedList;
    }

    private static List<Character> extendCharRanges(List<Character> characterList, List<Character> anotherList) {
        List<Character> extendedList = new ArrayList<>(characterList);
        extendedList.addAll(anotherList);
        return extendedList;
    }

    private static String escape(String s) {
        boolean safeString = true;
        for (char c : s.toCharArray()) {
            if (!CHAR_SAFE.contains(c)) {
                safeString = false;
                break;
            }
        }
        if (safeString) {
            return s;
        } else {
            return '"'+ s.replace("\"", "\"\"") + '"';
        }
    }

    private static String generateString(List<Character> characterList) {
        int stringLength = RANDOM_GENERATOR.nextInt(MAX_LENGTH - MIN_LENGTH + 1) + MIN_LENGTH;
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            int nextChar = RANDOM_GENERATOR.nextInt(characterList.size());
            randomString.append(characterList.get(nextChar));
        }
        return randomString.toString();
    }

    static TcpParsingTestData getRandomData(boolean allowUnicode) {
        String caseName = allowUnicode ? "random-unicode-" : "random-";
        int caseId;
        if (allowUnicode) {
            caseId = randomUnicodeCounter++;
        } else {
            caseId = randomUnicodeCounter;
        }
        TcpParsingTestData data = new TcpParsingTestData(caseName + caseId);
        StringBuilder commandsBuilder = new StringBuilder();

        for (int i = 0; i < TOTAL_COMMANDS; i++) {
            String entityName = generateString(allowUnicode ? CHAR_NAME_UNICODE : CHAR_NAME);
            commandsBuilder.append("series e:");
            commandsBuilder.append(escape(entityName));
            commandsBuilder.append(" ");

            int metricCount = RANDOM_GENERATOR.nextInt(MAX_METRICS - MIN_METRICS + 1) + MIN_METRICS;
            for (int j = 0; j < metricCount; j++) {
                String metricName = generateString(allowUnicode ? CHAR_NAME_UNICODE : CHAR_NAME);
                String metricValue = generateString(allowUnicode ? CHAR_VALUE_UNICODE : CHAR_VALUE);

                commandsBuilder.append("x:");
                commandsBuilder.append(escape(metricName));
                commandsBuilder.append("=");
                commandsBuilder.append(escape(metricValue));
                commandsBuilder.append(" ");

                Series generatedSeries = new Series();
                generatedSeries.setEntity(entityName.toLowerCase());
                generatedSeries.setMetric(metricName.toLowerCase());
                generatedSeries.addSamples(Sample.ofDateText(Mocks.ISO_TIME, metricValue.replaceAll("\n+", "\n")));

                try {
                    String seriesJson = MAPPER.writeValueAsString(new Series[]{generatedSeries});
                    data.getSeriesJsonList().add(seriesJson);
                } catch (JsonProcessingException e) {
                    /* We would not go here */
                    throw new RuntimeException(e);
                }
            }
            commandsBuilder.append(" d:");
            commandsBuilder.append(Mocks.ISO_TIME);
            commandsBuilder.append("\n");
        }

        data.setCommandsText(commandsBuilder.toString());
        return data;
    }

    private static String readFile(Path filePath) throws IOException {
        return FileUtils.readFileToString(getFile(filePath));
    }

    private static File getFile(String name) {
        URL resourceUrl = TcpParsingTest.class.getResource(name);
        if (resourceUrl != null) {
            try {
                return new File(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                /* Shouldn't happen */
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static File getFile(Path path) {
        return getFile(path.toString());
    }

    static TcpParsingTestData[] loadFromResources() throws IOException {
        Path root = Paths.get("test_set");
        File rootFile = getFile(root);
        File[] list = rootFile.listFiles();
        List<TcpParsingTestData> resultList = new ArrayList<>();
        if (list != null) {
            for (File testDir : list) {
                Path testDirPath = root.resolve(testDir.getName());
                TcpParsingTestData test = new TcpParsingTestData(testDir.getName());
                loadFromResources(test, testDirPath);
                resultList.add(test);
            }
        }
        TcpParsingTestData[] result = new TcpParsingTestData[resultList.size()];
        resultList.toArray(result);
        return result;
    }

    private static void loadFromResources(TcpParsingTestData data, Path testDirPath) throws IOException {
        data.setCommandsText(readFile(testDirPath.resolve(COMMANDS_FILE)));
        loadJsonFiles(testDirPath.resolve(METRICS_DIR), data.getMetricsJsonList());
        loadJsonFiles(testDirPath.resolve(SERIES_DIR), data.getSeriesJsonList());
    }

    private static void loadJsonFiles(Path subPath, List<String> itemList) throws IOException {
        File itemsDir = getFile(subPath);
        if (itemsDir == null) {
            return;
        }
        File[] itemsFiles = itemsDir.listFiles();
        if (itemsFiles != null) {
            for (File itemFile : itemsFiles) {
                String json = FileUtils.readFileToString(itemFile);
                itemList.add(json);
            }
        }
    }
}
