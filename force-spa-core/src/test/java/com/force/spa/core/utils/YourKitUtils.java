/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to help with performance measurement using YourKit.
 * <p/>
 * YourKit is accessed reflectively so we don't need to include any YourKit jar dependencies in production code.
 */
public final class YourKitUtils {

    private static final Logger LOG = LoggerFactory.getLogger(YourKitUtils.class);

    private static Object controller;

    private static Method forceGC;
    private static Method clearExceptions;
    private static Method clearAllocationData;
    private static Method clearCPUData;

    static {
        try {
            Class<?> controllerClass = Class.forName("com.yourkit.api.Controller");
            controller = controllerClass.newInstance();

            forceGC = controllerClass.getMethod("forceGC");
            clearExceptions = controllerClass.getMethod("clearExceptions");
            clearAllocationData = controllerClass.getMethod("clearAllocationData");
            clearCPUData = controllerClass.getMethod("clearCPUData");

            controllerClass.getMethod("enableExceptionTelemetry").invoke(controller);
            controllerClass.getMethod("enableStackTelemetry").invoke(controller);
            controllerClass.getMethod("startCPUTracing", String.class).invoke(controller, (String) null);

        } catch (ClassNotFoundException e) {
            LOG.info("YourKit not found in classpath");
        } catch (Exception e) {
            LOG.info("YourKit agent is not active");
        }
    }

    private YourKitUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Returns an indication of whether YourKit is present.
     */
    public static boolean isYourKitPresent() {
        return controller != null;
    }

    /**
     * Clears performance data.
     * <p/>
     * Typically this is called after caches have been primer so that you can collect statistics free from one-time
     * costs.
     */
    public static void clearYourKitData() {
        if (isYourKitPresent()) {
            try {
                forceGC.invoke(controller);
                clearExceptions.invoke(controller);
                clearAllocationData.invoke(controller);
                clearCPUData.invoke(controller);
            } catch (Exception e) {
                LOG.error("Failed to reset YourKit statistics", e);
            }
        }
    }
}
