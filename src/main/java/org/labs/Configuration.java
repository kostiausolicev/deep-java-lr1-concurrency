package org.labs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record Configuration(
        int seats,
        int waiters,
        int sides,
        int threshold
) {
    public static Configuration load() {
        Properties properties = new Properties();
        try (InputStream input = Configuration.class.getResourceAsStream("/application.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return new Configuration(
                Integer.parseInt(properties.getProperty("seats")),
                Integer.parseInt(properties.getProperty("waiters")),
                Integer.parseInt(properties.getProperty("sides")),
                Integer.parseInt(properties.getProperty("threshold"))
        );
    }
}
