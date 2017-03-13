package com.axibase.tsd.logging;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static java.lang.String.format;

public class LoggingFilter implements ClientResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class.getName());
    private static final int MAX_ENTITY_SIZE = 1024 * 8;
    private Boolean isCheckLoggingEnable;

    public LoggingFilter() {
        super();
        initialize();
    }

    private void initialize() {
        try {
            Config config = Config.getInstance();
            isCheckLoggingEnable = config.getCheckLoggingEnable();
        } catch (FileNotFoundException e) {
            LOG.error("Failed to get application config. {}", e);
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
                entityStream.mark(MAX_ENTITY_SIZE + 1);
                final byte[] entity = new byte[MAX_ENTITY_SIZE + 1];
                final int entitySize = entityStream.read(entity);
                b.append(new String(entity, 0, Math.min(entitySize, MAX_ENTITY_SIZE)));
                if (entitySize > MAX_ENTITY_SIZE) {
                    b.append("...more...");
                }
                entityStream.reset();
                String entityBody = b.toString();
                try {
                    entityBody = prettyEntityStream(entityBody);
                } catch (IOException e) {
                    LOG.debug("Failed to get entity by MessageBodyReader. {}", e);
                }
                descBuilder.append(format("%s%n%n", entityBody));
            }
        } catch (IOException e) {
            LOG.debug("Failed to get Entity Descrption, use body instead");
        }
        return descBuilder.toString();
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        if (isCheckLoggingEnable || !isCalledByClass(Checker.class)) {
            String requestDescription = buildRequestDescription(clientRequestContext);
            String responseDescription = buildResponseDescription(clientResponseContext);
            LOG.debug("{}{}", requestDescription, responseDescription);
        }
    }

    private boolean isCalledByClass(Class<?> tClass) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            if (Objects.equals(element.getClassName(), tClass.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String prettyEntityStream(String entity) throws IOException {
        try {
            Object jsonMap = BaseMethod.getJacksonMapper().readValue(entity, Object.class);
            return Util.prettyPrint(jsonMap);
        } catch (IOException e) {
            LOG.debug("Failed to print stream for entity.");
            throw e;
        }
    }
}