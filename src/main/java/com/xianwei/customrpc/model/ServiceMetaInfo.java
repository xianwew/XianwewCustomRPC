package com.xianwei.customrpc.model;

import cn.hutool.core.util.StrUtil;
import com.xianwei.customrpc.constant.RpcConstant;
import lombok.Data;

/**
 * Service Metadata (Registration Information)
 *
 * This class holds metadata for a service instance registered in the RPC framework.
 */
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
public class ServiceMetaInfo {

    /**
     * Name of the service
     */
    private String serviceName;

    /**
     * Version of the service
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * Host (domain or IP) where the service is running
     */
    private String serviceHost;

    /**
     * Port number on which the service is running
     */
    private Integer servicePort;

    /**
     * Service group (not yet implemented)
     */
    private String serviceGroup = "default";

    /**
     * Get the service key (used for service identification)
     *
     * @return a string in the format serviceName:serviceVersion
     *         (Can be extended later to include serviceGroup)
     */
    public String getServiceKey() {
        // Can later be extended to include serviceGroup
        // return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * Get the unique key for the service node (used for registration)
     *
     * @return a string in the format serviceKey/serviceHost:servicePort
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * Get the full service address (e.g., for making HTTP calls)
     *
     * @return a string like http://host:port or host:port if already prefixed
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
