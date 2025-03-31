package com.xianwei.customrpc.bootstrap;

import com.xianwei.customrpc.RpcApplication;
import com.xianwei.customrpc.config.RegistryConfig;
import com.xianwei.customrpc.config.RpcConfig;
import com.xianwei.customrpc.model.ServiceMetaInfo;
import com.xianwei.customrpc.model.ServiceRegisterInfo;
import com.xianwei.customrpc.registry.LocalRegistry;
import com.xianwei.customrpc.registry.Registry;
import com.xianwei.customrpc.registry.RegistryFactory;
import com.xianwei.customrpc.server.VertxServer;

import java.util.List;

/**
 * Service Provider Bootstrap Class (Initialization)
 */
public class ProviderBootstrap {

    /**
     * Initialization method
     *
     * @param serviceRegisterInfoList List of services to register
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // Initialize the RPC framework (configuration and registry center)
        RpcApplication.init();

        // Load global RPC configuration
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // Register each service
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();

            // Register the service locally (in local registry)
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // Register the service with the registry center (e.g., Zookeeper, Etcd)
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

            // Prepare metadata for the service
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            try {
                registry.register(serviceMetaInfo); // Remote registration
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " service registration failed", e);
            }
        }

        // Start the TCP server (using Vert.x) to handle incoming RPC requests
        VertxServer vertxTcpServer = new VertxServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
