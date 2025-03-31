package com.xianwei.customrpc.constant;

/**
 * RPC-related Constants
 */
public interface RpcConstant {

    /**
     * Default prefix for loading configuration files
     * (e.g., properties or YAML files using "rpc" as the root key)
     */
    String DEFAULT_CONFIG_PREFIX = "rpc";

    /**
     * Default version for services
     * (used when no specific version is provided)
     */
    String DEFAULT_SERVICE_VERSION = "1.0";
}
