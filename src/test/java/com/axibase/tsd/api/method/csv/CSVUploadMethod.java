package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.method.BaseMethod;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static javax.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.media.multipart.file.DefaultMediaTypePredictor.CommonMediaTypes.getMediaTypeFromFile;

public class CSVUploadMethod extends BaseMethod {
    public static File resolvePath(String path) throws URISyntaxException {
        URL url = CSVUploadTest.class.getResource(path);
        return (url == null) ? null : new File(url.toURI());
    }

    public static boolean importParser(File configPath) {
        Invocation.Builder builder = httpRootResource.path("/csv/configs/import").request();
        MultiPart multiPart = new MultiPart();
        FileDataBodyPart fileDataBodyPart
                = new FileDataBodyPart("file", configPath, getMediaTypeFromFile(configPath));
        multiPart.bodyPart(fileDataBodyPart);
        Response response = builder.post(Entity.entity(multiPart, multiPart.getMediaType()));
        return response.getStatus() == OK.getStatusCode();
    }

    public static Response multipartCsvUpload(File file, String parserName) {
        MultiPart multiPart = new MultiPart();
        MediaType mediaType = getMediaTypeFromFile(file);
        if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
            mediaType = new MediaType("text", "csv");
        }
        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("filedata", file, mediaType);
        multiPart.bodyPart(fileDataBodyPart);

        Response response = httpApiResource.path("csv")
                .queryParam("config", parserName)
                .queryParam("wait", true)
                .request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        response.close();
        return response;
    }

    public static Response binaryCsvUpload(File file, String parserName) {
        return binaryCsvUpload(file, parserName, null);
    }
    public static Response binaryCsvUpload(File file, String parserName, String encoding) {
        Response response = httpApiResource.path("csv")
                .queryParam("config", parserName)
                .queryParam("wait", true)
                .queryParam("filename", file.getName())
                .queryParam("encoding", encoding)
                .request().post(Entity.entity(file, new MediaType("text", "csv")));
        response.close();
        return response;
    }

}
