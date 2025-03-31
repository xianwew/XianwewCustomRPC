package com.xianwei.customrpc.registry;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.xianwei.customrpc.config.RegistryConfig;
import com.xianwei.customrpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ZooKeeper Registry
 *
 * This class implements the Registry interface using Apache Curator over ZooKeeper.
 * It supports service registration, discovery, caching, and event watching.
 *
 * Docs:
 * - Curator Getting Started: https://curator.apache.org/docs/getting-started
 * - Example Registration: https://github.com/apache/curator/blob/master/curator-examples/src/main/java/discovery/DiscoveryExample.java
 * - Example Watcher: https://github.com/apache/curator/blob/master/curator-examples/src/main/java/cache/CuratorCacheExample.java
 */
@Slf4j
public class ZooKeeperRegistry implements Registry {

    private CuratorFramework client;
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * Locally registered node keys (used to track what we registered)
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * Local cache for a single service (legacy, replace with multi-cache for real apps)
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * Tracks already-watched keys to avoid duplicate listeners
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * Root path for services in ZooKeeper
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

    /**
     * Initialize ZooKeeper connection and service discovery
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
                .build();

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start ZooKeeper client", e);
        }
    }

    /**
     * Register a service instance to ZooKeeper
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * Unregister a service instance from ZooKeeper
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException("Unregistration failed", e);
        }
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * Discover available instances of a service
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        List<ServiceMetaInfo> cached = registryServiceCache.readCache();
        if (cached != null) {
            return cached;
        }

        try {
            Collection<ServiceInstance<ServiceMetaInfo>> instances = serviceDiscovery.queryForInstances(serviceKey);
            List<ServiceMetaInfo> result = instances.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());

            registryServiceCache.writeCache(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Service discovery failed", e);
        }
    }

    /**
     * No need for a heartbeat: ZooKeeper uses ephemeral nodes, so if the client dies, the node disappears automatically.
     */
    @Override
    public void heartBeat() {
        // Not required for ZooKeeper; nodes are ephemeral.
    }

    /**
     * Add a watcher to a specific service node key (used on the client side)
     */
    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;
        boolean newWatch = watchingKeySet.add(watchKey);
        if (newWatch) {
            CuratorCache cache = CuratorCache.build(client, watchKey);
            cache.start();

            cache.listenable().addListener(
                    CuratorCacheListener.builder()
                            .forDeletes(childData -> registryServiceCache.clearCache())
                            .forChanges((oldNode, newNode) -> registryServiceCache.clearCache())
                            .build()
            );
        }
    }

    /**
     * Deregister and release all resources
     */
    @Override
    public void destroy() {
        log.info("Shutting down ZooKeeper registry");

        for (String key : localRegisterNodeKeySet) {
            try {
                client.delete().guaranteed().forPath(key);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete node: " + key);
            }
        }

        if (client != null) {
            client.close();
        }
    }

    /**
     * Helper method to build a ZooKeeper service instance from metadata
     */
    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build service instance", e);
        }
    }
}
