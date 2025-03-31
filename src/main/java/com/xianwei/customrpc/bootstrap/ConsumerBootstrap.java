package com.xianwei.customrpc.bootstrap;

import com.xianwei.customrpc.RpcApplication;

public class ConsumerBootstrap {

    /**
     * Initialization method
     */
    public static void init() {
        // Initialize the RPC framework (including configuration and registry center)
        RpcApplication.init();
    }

}

