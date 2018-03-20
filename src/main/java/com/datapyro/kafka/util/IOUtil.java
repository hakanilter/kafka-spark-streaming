package com.datapyro.kafka.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtil {

    public static byte[] readResourceAsBytes(String resourceName) throws IOException {
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[10240];
        int nRead;
        try (InputStream is = IOUtil.class.getResourceAsStream(resourceName)) {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        }
        return buffer.toByteArray();
    }

    public static String readResourceAsString(String resourceName) throws IOException {
        byte[] data = readResourceAsBytes(resourceName);
        return new String(data, "utf-8");
    }

}
