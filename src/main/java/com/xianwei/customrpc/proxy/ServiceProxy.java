package com.xianwei.customrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.xianwei.customrpc.RpcApplication;
import com.xianwei.customrpc.config.RpcConfig;
import com.xianwei.customrpc.constant.RpcConstant;
import com.xianwei.customrpc.fault.retry.RetryStrategy;
import com.xianwei.customrpc.fault.retry.RetryStrategyFactory;
import com.xianwei.customrpc.fault.tolerant.TolerantStrategy;
import com.xianwei.customrpc.fault.tolerant.TolerantStrategyFactory;
import com.xianwei.customrpc.loadbalancer.LoadBalancer;
import com.xianwei.customrpc.loadbalancer.LoadBalancerFactory;
import com.xianwei.customrpc.model.RpcRequest;
import com.xianwei.customrpc.model.RpcResponse;
import com.xianwei.customrpc.model.ServiceMetaInfo;
import com.xianwei.customrpc.registry.Registry;
import com.xianwei.customrpc.registry.RegistryFactory;
import com.xianwei.customrpc.server.VertxClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Proxy (JDK Dynamic Proxy)
 *
 * This class handles method calls on service interface proxies.
 * It constructs RPC requests, discovers service providers, applies load balancing,
 * and makes network requests to invoke the remote service.
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * Intercepts method calls on a proxy instance and handles them via remote procedure call (RPC).
     *
     * @param proxy  the proxy instance
     * @param method the method being called
     * @param args   the arguments passed to the method
     * @return the result returned from the remote service
     * @throws Throwable if the RPC call fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Build an RpcRequest based on the method being invoked
        String serviceName = method.getDeclaringClass().getName(); // Fully qualified interface name
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // Retrieve service provider list from the registry
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);

        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());

        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("No available service address");
        }

        // Apply load balancing to select a target service instance
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

        // Execute the RPC request using retry and fault tolerance strategies
        RpcResponse rpcResponse;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // If retry fails, apply the fault-tolerance strategy
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }

        return rpcResponse.getData(); // Return result from the RPC response
    }
}
