package com.xianwei.customrpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.xianwei.customrpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI Loader
 *
 * A custom SPI (Service Provider Interface) implementation that supports key-value mapping.
 * This allows loading multiple implementations of an interface and selecting them dynamically by key.
 */
@Slf4j
public class SpiLoader {

    /**
     * Stores loaded class mappings: Interface name → (key → implementing class)
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * Singleton object cache: class path → instance
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * System SPI directory (used for built-in implementations)
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * Custom SPI directory (used for user-defined extensions)
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * Scan order priority: custom overrides system
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * List of interfaces that support SPI loading
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(
            com.xianwei.customrpc.serializer.Serializer.class,
            com.xianwei.customrpc.registry.Registry.class,
            com.xianwei.customrpc.fault.retry.RetryStrategy.class,
            com.xianwei.customrpc.fault.tolerant.TolerantStrategy.class,
            com.xianwei.customrpc.loadbalancer.LoadBalancer.class
    );

    /**
     * Load all classes listed in LOAD_CLASS_LIST
     */
    public static void loadAll() {
        log.info("Loading all SPI implementations...");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            System.out.println("Loading SPI for: " + aClass);
            load(aClass);
        }
    }

    /**
     * Get a singleton instance for the given SPI interface and key
     *
     * @param tClass the SPI interface class
     * @param key    the key to select the implementation
     * @param <T>    the type of the SPI interface
     * @return the loaded singleton instance
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);

        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader has not loaded SPI for %s", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("No implementation found for %s with key=%s", tClassName, key));
        }

        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();

        // Lazy-load singleton instance
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate class: " + implClassName, e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }

    /**
     * Load SPI implementations for a single interface type
     *
     * @param loadClass the SPI interface class
     * @return mapping of key → implementation class
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("Loading SPI for type: {}", loadClass.getName());

        Map<String, Class<?>> keyClassMap = new HashMap<>();

        // Scan both system and custom SPI directories
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());

            for (URL resource : resources) {
                try (
                        InputStreamReader reader = new InputStreamReader(resource.openStream());
                        BufferedReader bufferedReader = new BufferedReader(reader)
                ) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");

                        if (strArray.length > 1) {
                            String key = strArray[0].trim();
                            String className = strArray[1].trim();
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to load SPI resource: {}", resource, e);
                }
            }
        }

        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    /**
     * Test method to demonstrate loading and retrieving an SPI instance
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        loadAll();
        System.out.println("Loaded SPI map: " + loaderMap);
        Serializer serializer = getInstance(Serializer.class, "e"); // key must exist in SPI file
        System.out.println("Loaded Serializer instance: " + serializer);
    }
}
