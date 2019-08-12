package com.axibase.tsd.logging;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.message.internal.HeaderUtils;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class LoggingFilter implements ClientResponseFilter {
    private static final int MAX_ENTITY_SIZE = 1024 * 8;
    private static final ThreadLocal<RequestAndResponse> lastRequestAndResponse = new ThreadLocal<>();
    private final boolean isCheckLoggingEnable = Config.getInstance().isCheckLoggingEnable();

    @SuppressWarnings("unchecked")
    private static void appendHeaders(MultivaluedMap headers, StringBuilder buffer) {
        final Map<String, String> headersSingleValue = HeaderUtils.asStringHeadersSingleValue(headers);
        for (Map.Entry<String, String> header : headersSingleValue.entrySet()) {
            final String value = header.getValue();
            buffer.append(header.getKey()).append(": ");
            if (value != null) {
                buffer.append(value);
            }
            buffer.append("\r\n");
        }
    }

    private static void appendRequestDescription(ClientRequestContext requestContext, StringBuilder builder) {
        builder.append(" > ").append(requestContext.getMethod())
                .append("  ").append(requestContext.getUri()).append('\n');
        appendHeaders(requestContext.getHeaders(), builder);
        if (requestContext.hasEntity()) {
            builder.append(Util.prettyPrint(requestContext.getEntity()))
                    .append("\n\n");
        }
    }

    private static void appendResponseDescription(ClientResponseContext responseContext, StringBuilder builder, boolean full) {
        builder.append(" < ").append(responseContext.getStatus()).append('\n');
        appendHeaders(responseContext.getHeaders(), builder);
        try {
            if (full) {
                if (responseContext instanceof ClientResponse) {
                    final ClientResponse response = (ClientResponse) responseContext;
                    builder.append(response.readEntity(String.class));
                }
            } else {
                InputStream entityStream = responseContext.getEntityStream();
                if (responseContext.hasEntity()) {
                    if (!entityStream.markSupported()) {
                        entityStream = new BufferedInputStream(entityStream);
                    }
                    entityStream.mark(MAX_ENTITY_SIZE + 1);
                    final byte[] entity = new byte[MAX_ENTITY_SIZE + 1];
                    final int entitySize = readNBytes(entityStream, entity, entity.length);
                    entityStream.reset();
                    appendPrettyPrintedJson(entity, entitySize, builder);
                    if (entitySize > MAX_ENTITY_SIZE) {
                        builder.append("...more...");
                    }
                    builder.append("\n\n");
                }
            }
        } catch (IOException e) {
            log.debug("Failed to get Entity Description");
        }
    }

    private static void appendPrettyPrintedJson(byte[] entity, int entitySize, StringBuilder builder) {
        if (entitySize <= MAX_ENTITY_SIZE) {
            try {
                String entityBody = prettyEntityStream(entity, entitySize);
                builder.append(entityBody);
                return;
            } catch (IOException e) {
                log.debug("Failed to get entity by MessageBodyReader.", e);
            }
        }
        builder.append(new String(entity, 0, Math.min(entitySize, MAX_ENTITY_SIZE)));
    }

    private static int readNBytes(InputStream inputStream, byte[] buffer, int limit) throws IOException {
        int offset = 0;
        int readCount;
        while (offset != limit && (readCount = inputStream.read(buffer, offset, limit - offset)) != -1) {
            offset += readCount;
        }
        return offset;
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        final RequestAndResponse requestAndResponse = new RequestAndResponse(clientRequestContext, clientResponseContext);
        lastRequestAndResponse.set(requestAndResponse);
        if (log.isDebugEnabled() && (isCheckLoggingEnable || !isCalledByClass(Checker.class))) {
            log.debug(composeRequestAndResponse(requestAndResponse, false));
        }
    }

    private static String composeRequestAndResponse(RequestAndResponse requestAndResponse, boolean fullResponse) {
        StringBuilder buffer = new StringBuilder();
        appendRequestDescription(requestAndResponse.clientRequestContext, buffer);
        appendResponseDescription(requestAndResponse.clientResponseContext, buffer, fullResponse);
        return buffer.toString();
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

    private static String prettyEntityStream(byte[] entity, int length) throws IOException {
        try {
            Object jsonMap = BaseMethod.getJacksonMapper().readValue(entity, 0, length, Object.class);
            return Util.prettyPrint(jsonMap);
        } catch (IOException e) {
            log.debug("Failed to print stream for entity.");
            throw e;
        }
    }

    public static void clear() {
        lastRequestAndResponse.remove();
    }

    public static String getRequestAndResponse() {
        final RequestAndResponse requestAndResponse = lastRequestAndResponse.get();
        return requestAndResponse == null ? null : composeRequestAndResponse(requestAndResponse, true);
    }

    @Data
    private static final class RequestAndResponse {
        private final ClientRequestContext clientRequestContext;
        private final ClientResponseContext clientResponseContext;
    }
}