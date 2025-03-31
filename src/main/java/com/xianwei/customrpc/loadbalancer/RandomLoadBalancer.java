package com.xianwei.customrpc.loadbalancer;

import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Random Load Balancer
 *
 * Selects a service node randomly from the list of available nodes.
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        int size = serviceMetaInfoList.size();
        if (size == 0) {
            return null; // No available service
        }
        // If only one service is available, return it directly
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }
        // Select one service at random
        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
