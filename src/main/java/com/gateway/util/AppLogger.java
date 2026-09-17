package com.gateway.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger(AppLogger.class.getName());

    private AppLogger() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void logException(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
}
