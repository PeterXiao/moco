package com.github.dreamhead.moco.dumper;

import com.github.dreamhead.moco.HttpMessage;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.util.internal.StringUtil;

import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

public final class HttpDumpers {
    public static String asContent(final HttpMessage message) {
        long length = getContentLength(message, -1);
        if (length > 0) {
            return StringUtil.NEWLINE + StringUtil.NEWLINE + contentForDump(message);
        }

        return "";
    }

    private static String contentForDump(final HttpMessage message) {
        String type = message.getHeader(HttpHeaders.CONTENT_TYPE);
        if (isText(type)) {
            return message.getContent().toString();
        }

        return "<content is binary>";
    }

    private static boolean isText(final String type) {
        try {
            MediaType mediaType = MediaType.parse(type);
            return mediaType.is(MediaType.ANY_TEXT_TYPE)
                    || mediaType.subtype().endsWith("javascript")
                    || mediaType.subtype().endsWith("json")
                    || mediaType.subtype().endsWith("xml")
                    || mediaType.is(MediaType.FORM_DATA)
                    || mediaType.subtype().endsWith("form-data");
        } catch (Exception e) {
            return false;
        }
    }

    private static long getContentLength(final HttpMessage response, final long defaultValue) {
        String lengthText = response.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (lengthText != null) {
            try {
                return Long.parseLong(lengthText);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private final static Joiner.MapJoiner headerJoiner = Joiner.on(StringUtil.NEWLINE).withKeyValueSeparator(": ");

    public static String asHeaders(final HttpMessage message) {
        return headerJoiner.join(from(message.getHeaders().entrySet())
                .transformAndConcat(toMapEntries()));
    }

    private static Function<Map.Entry<String, String[]>, Iterable<Map.Entry<String, String>>> toMapEntries() {
        return new Function<Map.Entry<String, String[]>, Iterable<Map.Entry<String, String>>>() {
            @Override
            public Iterable<Map.Entry<String, String>> apply(final Map.Entry<String, String[]> input) {
                String key = input.getKey();
                ImmutableList.Builder<Map.Entry<String, String>> builder = ImmutableList.builder();
                for (String value : input.getValue()) {
                    builder.add(Maps.immutableEntry(key, value));
                }
                return builder.build();
            }
        };
    }

    private HttpDumpers() {
    }
}
