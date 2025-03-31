package com.xianwei.customrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.xianwei.customrpc.config.RegistryConfig;
import com.xianwei.customrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Etcd Registry
 *
 * This class implements the Registry interface using Etcd as the backend.
 * It handles service registration, discovery, heartbeat renewal, and watching service changes.
 */
public class EtcdRegistry implements Registry {

    private Client client;
    private KV kvClient;

    /**
     * Locally registered node keys (used for lease renewal).
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * Deprecated: single-service cache (legacy, not recommended).
     */
    @Deprecated
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * Multi-service cache (recommended, supports multiple service keys).
     */
    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();

    /**
     * Set of keys currently being watched for changes.
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * Root path prefix used for all RPC keys in etcd.
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * Initialize etcd client using registry config.
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat(); // Start scheduled heartbeats
    }

    /**
     * Register a service node in etcd with a lease (30 seconds).
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        Lease leaseClient = client.getLeaseClient();
        long leaseId = leaseClient.grant(30).get().getID();

        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        localRegisterNodeKeySet.add(registerKey); // track for heartbeats
    }

    /**
     * Unregister a service node from etcd and local cache.
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * Discover service providers for a given serviceKey.
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // Try cache first
        List<ServiceMetaInfo> cached = registryServiceMultiCache.readCache(serviceKey);
        if (cached != null) {
            return cached;
        }

        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption
            ).get().getKvs();

            List<ServiceMetaInfo> result = keyValues.stream()
                    .map(kv -> {
                        String key = kv.getKey().toString(StandardCharsets.UTF_8);
                        watch(key); // Start watching for changes
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            registryServiceMultiCache.writeCache(serviceKey, result);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch service list", e);
        }
    }

    /**
     * Heartbeat scheduler to renew service leases every 10 seconds.
     */
    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        if (CollUtil.isEmpty(kvs)) continue; // already expired

                        String value = kvs.get(0).getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo); // re-register (acts as renewal)

                    } catch (Exception e) {
                        throw new RuntimeException(key + " renewal failed", e);
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start(); // Start cron scheduler
    }

    /**
     * Watch a specific service node key for changes (DELETE/PUT).
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();

        // Avoid duplicate watches
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        case DELETE:
                            // Clear from cache when node is removed
                            // NOTE: Ideally should use serviceKey, not serviceNodeKey
                            registryServiceMultiCache.clearCache(serviceNodeKey);
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    /**
     * Gracefully shut down and deregister all local service nodes.
     */
    @Override
    public void destroy() {
        System.out.println("Shutting down local node(s)");
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to deregister node: " + key);
            }
        }

        if (kvClient != null) kvClient.close();
        if (client != null) client.close();
    }
}
