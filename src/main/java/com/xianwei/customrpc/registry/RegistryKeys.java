package com.xianwei.customrpc.registry;

/**
 * Registry Key Constants
 *
 * This interface defines constant keys for identifying different registry implementations.
 * These keys are typically used with the RegistryFactory or configuration files.
 */
public interface RegistryKeys {

    /**
     * Key for the Etcd registry implementation.
     */
    String ETCD = "etcd";

    /**
     * Key for the ZooKeeper registry implementation.
     */
    String ZOOKEEPER = "zookeeper";

}

