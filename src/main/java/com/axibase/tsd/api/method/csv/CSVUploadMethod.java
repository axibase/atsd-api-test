package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.Util;
import lombok.SneakyThrows;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.glassfish.jersey.media.multipart.file.DefaultMediaTypePredictor.CommonMediaTypes.getMediaTypeFromFile;

public class CSVUploadMethod extends BaseMethod {
    public static File resolvePath(String path) throws URISyntaxException, FileNotFoundException {
        URL url = CSVUploadMethod.class.getResource(path);
        if (url == null) {
            throw new FileNotFoundException("File " + path + " not found");
        }
        return new File(url.toURI());
    }

    @SneakyThrows(IOException.class)
    public static boolean importParser(File configPath) {
        try (MultiPart multiPart = new MultiPart()) {
        Response response = executeRootRequest(webTarget -> {
            FileDataBodyPart fileDataBodyPart
                    = new FileDataBodyPart("file", configPath, getMediaTypeFromFile(configPath));
            multiPart.bodyPart(fileDataBodyPart);
            return webTarget.path("/csv/configs/import")
                    .request()
                    .post(Entity.entity(multiPart, Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE)));

        });

        response.bufferEntity();
        return Response.Status.Family.SUCCESSFUL == Util.responseFamily(response);
    }
    }

    @SneakyThrows(IOException.class)
    public static Response multipartCsvUpload(File file, String parserName) {
        try (MultiPart multiPart = new MultiPart()) {
            MediaType mediaType = getMediaTypeFromFile(file);
            if (mediaType == null || mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                mediaType = new MediaType("text", "csv");
            }
            FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("filedata", file, mediaType);
            multiPart.bodyPart(fileDataBodyPart);

            Response response = executeApiRequest(webTarget -> webTarget
                    .path("csv")
                    .queryParam("config", parserName)
                    .queryParam("wait", true)
                    .request()
                    .header("MIME-Version", "1.0")
                    .post(Entity.entity(multiPart, Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE))));

            response.bufferEntity();
            return response;
        }
    }

    public static Response binaryCsvUpload(File file, String parserName) {
        return binaryCsvUpload(file, parserName, null, null);
    }

    public static Response binaryCsvUpload(File file, String parserName, String entity) {
        return binaryCsvUpload(file, parserName, null, entity);
    }

    public static Response binaryCsvUpload(File file, String parserName, String encoding, String entity) {
        Response response = executeApiRequest(webTarget -> webTarget.path("csv")
                .queryParam("config", parserName)
                .queryParam("wait", true)
                .queryParam("filename", file.getName())
                .queryParam("encoding", encoding)
                .queryParam("default-entity", entity)
                .request().post(Entity.entity(file, new MediaType("text", "csv"))));

        response.bufferEntity();
        return response;
    }

}
