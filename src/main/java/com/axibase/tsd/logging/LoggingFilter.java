package com.axibase.tsd.logging;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public class LoggingFilter implements ClientResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class.getName());
    private static final int maxEntitySize = 1024 * 8;

    private static String prettyEntityStream(String entity) throws IOException {

        try {
            Object jsonMap = BaseMethod.getJacksonMapper().readValue(entity, Object.class);
            return Util.prettyPrint(jsonMap);

        } catch (Exception e) {
            return entity;
        }
    }

    public static String buildRequestDescription(ClientRequestContext requestContext) {
        StringBuilder builder = new StringBuilder();
        builder.append(format(
                " > %s  %s%n",
                requestContext.getMethod(), requestContext.getUri())
        );
        if (requestContext.hasEntity()) {
            builder.append(format(
                    "%s%n%n",
                    Util.prettyPrint(requestContext.getEntity()))
            );
        }
        return builder.toString();
    }

    public static String buildResponseDescription(ClientResponseContext responseContext) {
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append(format(" < %d%n", responseContext.getStatus()));
        try {
            InputStream entityStream = responseContext.getEntityStream();
            if (responseContext.hasEntity()) {
                final StringBuilder b = new StringBuilder();
                if (!entityStream.markSupported()) {
                    entityStream = new BufferedInputStream(entityStream);
                }
                entityStream.mark(maxEntitySize + 1);
                final byte[] entity = new byte[maxEntitySize + 1];
                final int entitySize = entityStream.read(entity);
                b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize)));
                if (entitySize > maxEntitySize) {
                    b.append("...more...");
                }
                entityStream.reset();
                descBuilder.append(format(
                        "%s%n%n", prettyEntityStream(b.toString())
                        )
                );
            }
        } catch (IOException e) {
        }
        return descBuilder.toString();
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        LOG.debug(buildRequestDescription(clientRequestContext) + buildResponseDescription(clientResponseContext));
    }
}