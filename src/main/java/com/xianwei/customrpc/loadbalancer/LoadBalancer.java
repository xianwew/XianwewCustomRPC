package com.xianwei.customrpc.loadbalancer;


import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * Load Balancer Interface (Used by Consumer Side)
 *
 * Defines how a service instance is selected from a list of available nodes.
 */
public interface LoadBalancer {

    /**
     * Select a service instance to handle the request.
     *
     * @param requestParams       Parameters of the RPC request (can be used for consistent hashing, etc.)
     * @param serviceMetaInfoList List of available service nodes
     * @return The selected service node's metadata
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}

