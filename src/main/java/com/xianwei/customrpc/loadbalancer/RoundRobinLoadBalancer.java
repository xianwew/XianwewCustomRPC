package com.xianwei.customrpc.loadbalancer;

import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin Load Balancer
 *
 * This class implements a load balancing strategy using round-robin selection.
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * The current index for round-robin rotation.
     * Uses AtomicInteger to ensure thread-safety in concurrent environments.
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        // If the list of service instances is empty, return null
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // If there is only one service instance, return it directly (no need for round-robin)
        int size = serviceMetaInfoList.size();
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }

        // Round-robin selection using modulo operation
        int index = currentIndex.getAndIncrement() % size;

        // Return the selected service instance based on the index
        return serviceMetaInfoList.get(index);
    }
}
