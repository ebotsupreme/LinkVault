package com.linkvault.util;


import org.slf4j.Logger;

public class LogUtils
{
    public static void info(Logger log, String message, Object... args) {
        if (log.isInfoEnabled()) {
            log.info(message, args);
        }
    }
    public static void debug(Logger log, String message, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(message, args);
        }
    }

    public static void error(Logger log, String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error(message, throwable);
        }
    }

    public static void warn(Logger log, String message, Object... args) {
        if (log.isWarnEnabled()) {
            log.warn(message, args);
        }
    }
}
