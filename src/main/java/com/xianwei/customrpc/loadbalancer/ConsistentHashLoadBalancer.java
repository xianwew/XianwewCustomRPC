package com.xianwei.customrpc.loadbalancer;

import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Consistent Hash Load Balancer
 *
 * Selects a service node based on consistent hashing to ensure minimal changes
 * in routing when nodes are added/removed.
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * Consistent Hash Ring holding virtual nodes
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * Number of virtual nodes per actual service node
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // Build the consistent hash ring with virtual nodes
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        // Generate a hash for the current request
        int hash = getHash(requestParams);

        // Find the closest node in the ring >= request hash
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if (entry == null) {
            // If no such node exists, wrap around to the first node in the ring
            entry = virtualNodes.firstEntry();
        }

        return entry.getValue();
    }

    /**
     * Hash function — can be replaced with a more stable or custom one (e.g., MurmurHash, MD5)
     *
     * @param key Key to hash
     * @return Hash value
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
