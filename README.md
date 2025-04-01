# Custom RPC Docs

[1. Introduction](#1-introduction)  
&emsp;[Main Highlights](#main-highlights)

[2. Usage](#2-usage)  
&emsp;[2.1 Create the Common Package](#21-create-the-common-package)  
&emsp;[2.2 Implement and Expose the Provider](#22-implement-and-expose-the-provider)  
&emsp;[2.3 Consume the Service](#23-consume-the-service)  
&emsp;[2.4 Configure the Remote Registry](#24-configure-the-remote-registry)  
&emsp;[2.5 Putting It All Together](#25-putting-it-all-together)

[3. Key Features](#3-key-features)  
&emsp;[3.1 Global Configuration Loading](#31-global-configuration-loading)  
&emsp;[3.2 Serializer and SPI Mechanism](#32-serializer-and-spi-mechanism)  
&emsp;[3.3 Registry Center](#33-registry-center)  
&emsp;[3.4 Registry Center Optimization](#34-registry-center-optimization)  
&emsp;[3.5 TCP Protocol & Sticky-Packet Issue](#35-tcp-protocol--sticky-packet-issue)  
&emsp;[3.6 Load Balancing](#36-load-balancing)  
&emsp;[3.7 Retry Mechanism](#37-retry-mechanism)  
&emsp;[3.8 Fault Tolerance Strategy](#38-fault-tolerance-strategy)  
&emsp;[3.9 Startup Mechanism](#39-startup-mechanism)

[Conclusion](#conclusion)


## **1. Introduction**

This Custom RPC framework aims to streamline **remote procedure calls** in a distributed environment. It abstracts away complexities of **networking**, **serialization**, **load balancing**, and **fault tolerance** so that teams can focus on **business logic** rather than the details of inter-service communication.

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXdmG9I57eq_6nKOC5RbtIKYhqGisZp1a8eQUGGTf6AnqpfaqlZ0Z1pNdX_ghVLkMXNWN9Fa5SXUQPTj00pnJEZ7TFpHQVzsFQQ1-re58xV5ffB_EsfausI8HxOducW-mT27MyMp?key=neYAAH9DSvM-3sjiH_tpgOlR)


### **Main Highlights**

1. **Clear Module Structure**

   - **Service Consumer**: A consumer module that sends requests to various services and, based on the request parameters, dispatches calls to the correct remote method.

   - **Service Provider**: A provider module that holds service implementations, along with a local registry recording service interfaces mapped to their implementation classes (e.g., `UserService` → `UserServiceImpl`).

   - **Core (CustomRPC)**: The core library orchestrating network communication, serialization/deserialization, request routing, load balancing, and fault tolerance.

   - **Common Package**: Shared data models and interfaces used by both consumer and provider.

2. **Global Configuration** Easily load environment-specific settings (for example, `dev` or `prod`) from `.properties` files. This includes registry addresses, default serializers, network settings, and more.

3. **Mock & Test Capabilities** Allows developers to return **mock data** for interfaces without needing a real remote service. This speeds up integration tests and prototyping.

4. **Pluggable Serialization & SPI** Provides multiple **serialization/deserialization** strategies (JDK, JSON, Hessian, Kryo, Protobuf) through a **Service Provider Interface (SPI)** mechanism. This design supports easy switching or expansion with custom serializers to optimize data transfer.

5. **Registry Integration** Features a **service registry** for providers to register themselves (and for consumers to discover available providers). Supports key-value store solutions such as **Etcd** and **ZooKeeper** to maintain an up-to-date mapping of services.

6. **Load Balancing & Fault Tolerance** Implements several load-balancing strategies (e.g., round-robin, consistent hashing) for selecting the best service provider at runtime. Offers configurable fault-tolerance mechanisms like **FailSafe**, **FailFast**, and **FailOver** to handle failures gracefully and maintain service availability.

7. **Other Key Functionalities**

   - **Offline Provider Detection**: Automatically removes offline provider nodes from the registry so consumers do not attempt calls on invalid endpoints.

   - **Service Info Caching**: Consumers cache registered services locally, reducing registry-center load and allowing calls even if the registry is temporarily unavailable.

   - **Custom Protocol & Transport Optimization**: Uses a custom protocol header to reduce packet overhead, resolving sticky/partial packet issues and minimizing data size over the wire.

   - **SPI-Based Extensibility**: Encourages adding or replacing components (e.g., load balancers, serializers, registry types) without modifying core logic, all driven by configuration files.


## **2. Usage**

### **2.1 Create the Common Package**

First, create a **common** module or package that contains:

1. A **model** or entity class (for example, `User`).

2. A **service interface** (for example, `UserService`).

<!---->

    // In example-common module/package

    import java.io.Serializable;

    public class User implements Serializable {
        private String name;
        // getters & setters
    }

    public interface UserService {

        /**
         * Retrieves user details.
         *
         * @param user the input user object
         * @return the output user object
         */
        User getUser(User user);

        /**
         * A default method for mock testing.
         */
        default short getNumber() {
            return 1;
        }
    }


### **2.2 Implement and Expose the Provider**

In a **provider** module:

**Implement** the `UserService` interface:\
\
`// In the provider module`

    public class UserServiceImpl implements UserService {
        @Override
        public User getUser(User user) {
            System.out.println("Username: " + user.getName());
            return user;
        }
    }

**Start the Provider** with a main class (for example, `ProviderExample`). This sets up the RPC server and registers your service:\
\
`public class ProviderExample {`

        public static void main(String[] args) {
            // List of services to be registered
            List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();

            // Register the UserService implementation
            ServiceRegisterInfo<UserService> serviceRegisterInfo =
                    new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
            serviceRegisterInfoList.add(serviceRegisterInfo);

            // Initialize the provider side
            ProviderBootstrap.init(serviceRegisterInfoList);
        }
    }

In your provider’s `pom.xml`, ensure the **framework** and **common** dependencies are included:

    <dependency>
        <groupId>org.xianwei</groupId>
        <artifactId>CustomRPC</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.xianwei</groupId>
        <artifactId>example-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


### **2.3 Consume the Service**

In a **consumer** module:

1. **Initialize** the consumer side.

2. **Obtain** a proxy for `UserService`.

3. **Invoke** the remote method.

<!---->

    // In the consumer module

    public class ConsumerExample {
        public static void main(String[] args) {
            // Step 1: Initialize RPC context
            ConsumerBootstrap.init();

            // Step 2: Get the remote service proxy
            UserService userService = ServiceProxyFactory.getProxy(UserService.class);

            // Step 3: Create a sample User object
            User user = new User();
            user.setName("xianwei");

            // Step 4: Invoke remote method
            User newUser = userService.getUser(user);

            // Step 5: Print the result
            if (newUser != null) {
                System.out.println(newUser.getName());
            } else {
                System.out.println("user == null");
            }
        }
    }

Similarly, include these dependencies in the consumer’s `pom.xml`:

    <dependency>
        <groupId>org.xianwei</groupId>
        <artifactId>CustomRPC</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.xianwei</groupId>
        <artifactId>example-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


### **2.4 Configure the Remote Registry**

By default, the framework uses **Etcd** with the address `http://localhost:2380`. If you want to override this and use, for example, **ZooKeeper** on `localhost:2181`, you can set the following properties in your `application.properties`:

    rpc.registryConfig.registry=zookeeper
    rpc.registryConfig.address=localhost:2181

If you do **not** change these settings, the framework will attempt to use the default Etcd registry at `http://localhost:2380`.


### **2.5 Putting It All Together**

1. **Run** `ProviderExample`: Publishes `UserServiceImpl` to the registry (Etcd by default, or ZooKeeper if configured).

2. **Run** `ConsumerExample`: Acquires the `UserService` proxy, calls `getUser()`, and prints the response.

3. **Verify Logs**: Check the console outputs on both consumer and provider to confirm that the remote call executed successfully.


## **3. Key Features**

Below are the **9 key features** of this Custom RPC framework, each explained in more depth.


### **3.1 Global Configuration Loading**

A key feature of this RPC framework is its ability to **centrally manage** and **dynamically apply** configuration values for different environments (for example, `dev` vs. `prod`). This ensures the framework can adapt to various deployment scenarios without requiring code changes.

**Centralized Configuration Files**

1. **Multiple File Formats**

   - By default, the framework looks for configuration in `.properties` or `.yml` files (for example, `application.properties`, `application-dev.properties`, `application-prod.properties`).

   - Users can place these files in the classpath so they are discovered automatically at runtime.

2. **Environment-Based Overrides**

   - You can have a **primary** file (for example, `application.properties`) containing baseline settings and **secondary** files (for instance, `application-dev.properties` or `application-prod.properties`) for environment-specific overrides.

   - The framework merges these files, giving environment-specific properties higher priority. For instance, if you run in “dev” mode, `application-dev.properties` overrides any conflicting keys in `application.properties`.

3. **Custom Keys and Prefixes**

   - Typical settings include `rpc.registryConfig.address`, `rpc.serializer`, and `rpc.loadBalancer`, but you can define additional keys as needed.

   - The framework commonly uses the `rpc.` prefix for core properties; however, you can introduce your own keys (for example, `rpc.mock=true`) if you want to toggle a feature like mock service responses.

**Extendable with Third-Party Tools**

1. **Hutool + SnakeYAML**

   - The framework can integrate with[ Hutool](https://hutool.cn/) to parse `.properties` files using `Props` or `.yml` files using `YamlUtil` or SnakeYAML.

   - This allows you to manage more complex configurations, nested structures, or multi-environment setups.

2. **Double-Check Singleton**

   - A typical **ConfigUtils** class initializes and caches configurations using a double-check locking pattern. This ensures a single, consistent set of loaded properties throughout the application’s runtime.

Example structure:\
\
`public class ConfigUtils {`

        private static volatile ConfigUtils instance;
        private Properties properties;

        private ConfigUtils() {
            // Load .properties or .yml, merge overrides, etc.
        }

        public static ConfigUtils getInstance() {
            if (instance == null) {
                synchronized (ConfigUtils.class) {
                    if (instance == null) {
                        instance = new ConfigUtils();
                    }
                }
            }
            return instance;
        }

        // Additional methods to retrieve config values
        public String getString(String key) {
            return properties.getProperty(key);
        }
    }

- If the user does not provide a custom file (e.g., `application.properties`), the framework gracefully defaults to internal, pre-defined values.

**Default and Fallback Settings**

- **Default Behavior**

  - If **no** environment file is found, the framework applies its built-in defaults. For example, the default registry might be Etcd at `http://localhost:2380`, or the default serializer may be the JDK serializer.

- **User Overrides**

  - By providing custom properties (for example, `rpc.registryConfig.registry=zookeeper`), you override defaults at startup. This approach allows rapid iteration without modifying the source code of the core framework.

**Practical Examples**

**Switching to ZooKeeper**\
`# application-dev.properties`

    rpc.registryConfig.registry=zookeeper
    rpc.registryConfig.address=localhost:2181

1.  When running in “dev” mode, the above file overrides the default Etcd address.

**Enabling Mock Services**\
`# application.properties`

    rpc.mock=true

2.  This setting forces the consumer to create mock proxies for testing, bypassing real RPC calls.

**Adjusting Serializer**\
`# application-prod.properties`

    rpc.serializer=kryo

3.  In production mode, the Kryo serializer is used instead of JDK or JSON.

In short, **global configuration loading** in the Custom RPC framework provides a flexible, environment-oriented mechanism for customizing your distributed system’s behavior. The combination of **multiple file formats**, **priority-based overrides**, and **tool-based parsing** (Hutool, SnakeYAML) makes it easy to maintain and evolve settings across development, testing, and production stages.


### **3.2 Serializer and SPI Mechanism**

The framework supports **multiple serialization strategies** for encoding and decoding request and response objects. This flexibility allows developers to choose the best balance between performance, compatibility, and ease of debugging. Additionally, it uses a **Service Provider Interface (SPI)** mechanism that makes it possible to add or override serializers without modifying core code.

**Multiple Serializer Options**

1. **JDK Serializer**

   - **Implementation**: Uses built-in Java serialization (`ObjectOutputStream` and `ObjectInputStream`).

   - **Pros**: Simple to set up and well-integrated into the Java ecosystem.

   - **Cons**: Relatively slow, requires all classes to implement `Serializable`, and offers no easy cross-language support.

2. **JSON Serializer**

   - **Implementation**: Uses Jackson’s `ObjectMapper`.

   - **Pros**: Human-readable text, easy to debug, and widely supported across different platforms.

   - **Cons**: Larger payload sizes compared to binary serializers. Needs special handling for generic types or complex objects (for example, the `RpcRequest` or `RpcResponse` type erasure issue).

3. **Hessian Serializer**

   - **Implementation**: Uses the Hessian binary protocol by Caucho.

   - **Pros**: Compact, relatively fast, and cross-language compatible (useful for polyglot systems).

   - **Cons**: Slightly less mainstream than JSON, so it may require additional dependencies or some tooling overhead.

4. **Kryo Serializer**

   - **Implementation**: Relies on the Kryo library, stored in a `ThreadLocal<Kryo>` to ensure thread safety, as Kryo instances are not thread-safe by default.

   - **Pros**: Very fast and efficient for Java-to-Java serialization, handles complex object graphs.

   - **Cons**: Not cross-language, and it can be tricky to debug since data is stored in a binary format.

5. **Protobuf (Extension)**

   - Not included in the framework but easily integrated through the same SPI approach.

   - **Pros**: Very compact, language-agnostic, and version-tolerant.

   - **Cons**: Requires `.proto` schemas and a compiler step, which can add complexity to the build process.

**How the SPI Loader Works**

1. **Configuration Files**

   - The framework searches in locations such as `META-INF/rpc/system` or `META-INF/rpc/custom` for mapping files containing key-value pairs where each key is a **serializer name** (for instance, `json`, `kryo`, `hessian`, `jdk`) and each value is the **fully qualified class name** of the corresponding serializer implementation.

Example (`serializer` mappings file):\
\
`jdk=com.xianwei.customrpc.serializer.JdkSerializer`

    hessian=com.xianwei.customrpc.serializer.HessianSerializer
    json=com.xianwei.customrpc.serializer.JsonSerializer
    kryo=com.xianwei.customrpc.serializer.KryoSerializer

2. **Dynamic Loading**

   - At startup, the system calls something like `SpiLoader.load(Serializer.class)`, which scans the above directories, reads the config files, and registers each listed class by reflection.

   - When the application needs a specific serializer, it calls `SpiLoader.getInstance(Serializer.class, key)`. If a matching key is found, the SPI loader instantiates and caches that serializer; otherwise, a fallback (such as `JdkSerializer`) may be used.

**SerializerFactory Example**\
`public class SerializerFactory {`

        static {
            SpiLoader.load(Serializer.class);
        }

        private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

        public static Serializer getInstance(String key) {
            Serializer serializer = SpiLoader.getInstance(Serializer.class, key);
            return (serializer != null) ? serializer : DEFAULT_SERIALIZER;
        }
    }

- `SpiLoader.load(Serializer.class)` populates an internal map of keys (for example, `json`, `kryo`) to class objects.

- `getInstance(key)` fetches the corresponding serializer or returns the **default** if none is matched.

**Key Advantages of SPI-Driven Serialization**

1. **Modular & Extensible**

   - Developers can drop in new serializers (for example, a custom `ProtoBufSerializer`) by adding the class and an entry in the SPI mapping file—no need to modify the factory or core code.

2. **Dynamic Configuration**

   - Using environment-specific property files (for instance, setting `rpc.serializer=json` in `application-prod.properties`), you can switch to a different serializer in production without recompiling.

3. **Centralized Fallback**

   - If an unknown key is provided in the config, the factory gracefully falls back to `JdkSerializer` (or whichever default you specify).

**Practical Usage**

- **Selecting a Serializer**:

  - Choose a default in your config (for example, `rpc.serializer=json`).

  - Ensure the SPI config file has an entry for the chosen key (for example, `json=com.xianwei.customrpc.serializer.JsonSerializer`).

  - The framework automatically uses `JsonSerializer` for all request/response objects.

- **Handling Edge Cases**:

  - **Generic Types**: The JSON serializer includes special logic for `RpcRequest` and `RpcResponse`, re-serializing argument arrays and response fields to preserve the correct classes when type erasure occurs.

  - **Thread Safety**: Kryo is wrapped in a thread-local context to avoid concurrency issues.

  - **Security**: JDK serialization can expose vulnerabilities if used with untrusted data. Hessian and Kryo also require careful handling, especially in multi-tenant environments.

In short, the **Serializer and SPI Mechanism** is designed for **flexibility** and **performance**. Whether you need a compact binary representation (Kryo, Hessian, Protobuf) or a human-readable JSON output, you can plug in the correct implementation via a simple configuration change. This modularity makes it straightforward to adapt the framework to evolving requirements without duplicating code.


### **3.3 Registry Center**

In this Custom RPC framework, the **registry center** manages service registration (from providers) and service discovery (by consumers). It allows the framework to dynamically track which services are online, along with their network addresses, versions, and other metadata (e.g., groups or additional configuration).

**Service Registration and Discovery**

1. **Provider Registration**

   - Each provider creates a `ServiceMetaInfo` object containing fields like `serviceName`, `serviceVersion`, `serviceHost`, and `servicePort`.

   - The provider then calls `registry.register(serviceMetaInfo)`.

   - In the `EtcdRegistry` example, each registration command creates or updates a **lease** in Etcd. The lease ensures a node is valid for a configured time span (such as 30 seconds). If the provider does not renew the lease (via a heartbeat), Etcd automatically removes the node.

2. **Consumer Lookup**

   - A consumer obtains all provider nodes for a service by calling `serviceDiscovery(serviceKey)`, where `serviceKey` is often the combination of `serviceName` and `serviceVersion` (for example, `"UserService:1.0"`).

   - The registry returns a list of `ServiceMetaInfo` objects indicating network locations (e.g., `localhost:8080`).

   - The consumer then chooses which provider to call, often in conjunction with a load-balancing strategy (round-robin, random, or consistent hashing).

3. **Watching for Updates**

   - Registries commonly support **watch** or **subscribe** functionality.

   - For instance, `EtcdRegistry.watch(...)` attaches a callback to a **service node key**, so if that key is updated or deleted, the registry can refresh or invalidate local caches.

   - This ensures a consumer’s view of available service nodes remains accurate.

**Support for Multiple Registry Implementations**

1. **Configuration in** `.properties`

In your `application.properties` or `application-dev.properties`, you can define which registry to use:\
\
`# Use Etcd`

    rpc.registryConfig.registry=etcd
    rpc.registryConfig.address=http://localhost:2379

    # Or switch to ZooKeeper
    rpc.registryConfig.registry=zookeeper
    rpc.registryConfig.address=localhost:2181

-

- Additional fields (like `username`, `password`, or `timeout`) can be set in `RegistryConfig`.

- By default, the framework might point to Etcd at `http://localhost:2380`, but you can override it as needed.

2. **SPI-Driven** `RegistryFactory

   `

   - Similar to serializers, the framework’s `RegistryFactory` loads registry implementations via **SPI**. A file named, for example, `com.xianwei.customrpc.registry.Registry` in `META-INF/rpc/system` maps keys (like `etcd` or `zookeeper`) to fully qualified class names.

   - At startup, the framework calls something like `SpiLoader.load(Registry.class)`, reads those mappings, and instantiates the relevant class when it sees `rpc.registryConfig.registry=etcd` (or another key).

   - Each registry class (for instance, `EtcdRegistry`) implements the `Registry` interface, which defines methods like `init()`, `register()`, `serviceDiscovery()`, `heartBeat()`, `watch()`, and `destroy()`.

**Etcd Example**

The **EtcdRegistry** is a concrete example of how this pattern works:

1. **Initialization** (`init`)

   - Creates an Etcd client using the provided address and timeout (for example, `Client.builder().endpoints(registryConfig.getAddress())`).

   - Sets up a `KV` client for key-value operations, and possibly starts a heartbeat scheduler.

2. **Registration** (`register`)

   - Requests a lease from Etcd (like 30 seconds).

   - Stores a JSON-serialized `ServiceMetaInfo` at a key path such as `/rpc/UserService:1.0/localhost:8080`.

   - Continues to track this key in a local set so it can be renewed periodically.

3. **Heartbeat** (`heartBeat`)

   - A scheduled cron job (for example, every 10 seconds) re-registers each local node with a new or updated lease.

   - If the provider process dies, it no longer refreshes the lease, and Etcd automatically removes the key upon expiration.

4. **Service Discovery** (`serviceDiscovery`)

   - Retrieves keys that match `/rpc/UserService:1.0/` using a prefix query.

   - Deserializes each value into a `ServiceMetaInfo`, caches them, and returns them to the caller.

5. **Watch** (`watch`)

   - For each discovered key, sets up a watch in Etcd.

   - On `DELETE` or `PUT`, updates the local cache to reflect changes (removed or modified providers).

6. **Destroy** (`destroy`)

   - Closes local resources and deregisters the known keys (for example, removing them from Etcd).

   - Ensures a clean shutdown so the registry does not keep stale references.

**How to Add a New Registry**

If you need to use another solution—whether **Consul**, **Nacos**, or your own key-value store—simply create a class implementing the `Registry` interface and register it via SPI:

**Create the Class**\
`public class MyCustomRegistry implements Registry {`

        @Override
        public void init(RegistryConfig registryConfig) { ... }
        @Override
        public void register(ServiceMetaInfo serviceMetaInfo) throws Exception { ... }
        @Override
        public void unRegister(ServiceMetaInfo serviceMetaInfo) { ... }
        @Override
        public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) { ... }
        @Override
        public void heartBeat() { ... }
        @Override
        public void watch(String serviceNodeKey) { ... }
        @Override
        public void destroy() { ... }
    }

1. **Add SPI Configuration**

   - In `META-INF/rpc/system`, create or edit `com.xianwei.customrpc.registry.Registry`.

Map a custom key to your new class:\
\
`myregistry=com.xianwei.customrpc.registry.MyCustomRegistry`

**Update** `application.properties
`\
`rpc.registryConfig.registry=myregistry`

    rpc.registryConfig.address=http://localhost:1234

- When the framework starts, it calls `RegistryFactory.getInstance("myregistry")`, which uses reflection to instantiate `MyCustomRegistry`.

- The newly created registry is initialized with the provided config (address, username, etc.).

After these steps, your new registry type will be fully supported by the RPC framework. Providers and consumers can call `register()`, `serviceDiscovery()`, or `watch()` as usual, without any special logic changes in other parts of the code.

**Example Flow with Etcd**

**Provider**:\
\
`// Create ServiceMetaInfo`

    ServiceMetaInfo meta = new ServiceMetaInfo();
    meta.setServiceName("UserService");
    meta.setServiceHost("localhost");
    meta.setServicePort(8080);

    // Register in Etcd
    etcdRegistry.register(meta);

**Consumer**:\
\
`// Discover available providers`

    List<ServiceMetaInfo> providers = etcdRegistry.serviceDiscovery("UserService:1.0");

    // Pick a provider and call the service
    if (!providers.isEmpty()) {
        ServiceMetaInfo chosenProvider = providers.get(0);
        // Make the RPC call to chosenProvider.getServiceHost():chosenProvider.getServicePort()
    }

- **Heartbeat & Removal**:

  - `EtcdRegistry.heartBeat()` runs periodically, refreshing the lease for each local node.

  - If a provider no longer renews its lease or calls `unRegister()`, Etcd eventually deletes that key, triggering watchers to remove it from consumer caches.

**Summary**

The **Registry Center** is essential for:

- **Registering** providers (including updating or removing them).

- **Discovering** available endpoints for each service key.

- **Observing** changes in real time (for instance, offline nodes).

With the **SPI** approach, you can **easily switch** between **Etcd**, **ZooKeeper**, or a custom registry. Configuration is managed via the `registryConfig` object in `RpcConfig` and a `.properties` (or `.yml`) file, ensuring the solution can adapt to diverse infrastructure requirements.


### **3.4 Registry Center Optimization**

While a **basic registry** can register services and return them to consumers, further optimizations ensure the system remains **resilient**, **low-latency**, and **accurate** under real-world conditions. Two key optimization strategies in this framework are **removing invalid nodes** and **local caching** of registry data.

**Removing Invalid Nodes**

1. **Watchers**

   - The registry uses **watchers** to automatically detect when a provider node is removed or expires. For example, the `EtcdRegistry.watch(serviceNodeKey)` method sets up a callback that listens for changes (such as a `DELETE` event) on that key.

   - If a provider node goes offline or is explicitly unregistered, Etcd (or another registry) fires an event that notifies the watcher. The framework can then remove that node from any **local cache** or internal list of active providers.

2. **Heartbeats**

   - Many registries (for instance, Etcd) employ **lease-based** registration. The provider periodically **renews** its lease (`heartBeat()` method). If it fails to do so (for example, the application crashes), the registry **automatically expires** the lease after a set interval (commonly 30 seconds).

   - Once expired, that node is removed from the registry. Consumers who are watching these keys will remove or invalidate them in their local data structures.

3. **Consistent Lookups**

   - Thanks to watchers and lease expiration, the consumer side stays in sync with real-time service availability. Stale entries are **removed** quickly, reducing the risk of sending requests to dead endpoints.

**Local Caching of Service Information**

1. **Performance Boost**

   - Every consumer query to the registry (for example, `serviceDiscovery("UserService:1.0")`) could become a bottleneck if done frequently and across many services. By maintaining a **local cache**, the consumer need not re-query the registry every time it wants to know which providers are online.

   - Instead, it fetches the data once, caches the results, and refreshes only when a **watch event** or **scheduled refresh** indicates a change.

2. **Implementation (RegistryServiceMultiCache)**

   - The code snippet above shows a `RegistryServiceMultiCache` class that stores a map of `serviceKey` to a list of `ServiceMetaInfo`.

When the `serviceDiscovery()` method in `EtcdRegistry` finishes pulling data from Etcd, it calls:\
\
`registryServiceMultiCache.writeCache(serviceKey, result);`

 which adds the list of nodes to the local map:\
\
`Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();`

-

3. **Fault Tolerance**

   - If the registry becomes **temporarily unavailable**, the consumer can continue using its cached data to make calls. This improves fault tolerance:

     - The consumer is no longer completely dependent on the registry’s availability for every request.

     - The local cache eventually updates when the registry recovers or when a watch event triggers.

4. **Cache Invalidation**

   - If a **watch** callback detects a `DELETE` event for a node, the framework calls `registryServiceMultiCache.clearCache(...)` or writes updated data for that service.

   - This keeps the cache fresh and prevents sending requests to dead endpoints.

**Combined Workflow**

1. **First Discovery**

   - Consumer calls `serviceDiscovery(serviceKey)`.

   - The registry queries Etcd (or another registry) for matching keys, deserializes `ServiceMetaInfo` objects, and **caches** them locally using `RegistryServiceMultiCache.writeCache()`.

   - The registry sets **watch** listeners on those keys so it can detect changes.

2. **Normal Operation**

   - For subsequent calls, the framework checks the **local cache** first (`registryServiceMultiCache.readCache(serviceKey)`).

   - If cached data exists, it is returned immediately—**no remote call** to the registry is needed.

   - This **significantly reduces** registry load and lowers latency.

3. **Update or Removal**

   - If a provider fails to renew its lease, the registry (for example, Etcd) **removes** its key.

   - The **watch** callback processes the `DELETE` event, calling `registryServiceMultiCache.clearCache(serviceKey)` or updating the relevant list.

   - On the next request, consumers will no longer see the removed node in the cache.

By combining **watch-based node removal** with **local caching**, the framework ensures **accurate** discovery data while **minimizing** network overhead and **enhancing** resilience against registry downtime.


### **3.5 TCP Protocol & Sticky-Packet Issue**

Modern RPC frameworks need an **efficient** and **robust** way to transport data. While HTTP-based solutions (for example, REST or gRPC over HTTP/2) are common, **this framework opts for raw TCP** to **minimize overhead**, enable **custom framing**, and provide **direct control** over data flow.

**Why TCP Instead of HTTP?**

1. **Performance and Overhead**

   - HTTP carries additional header information (for example, HTTP verbs, headers, status lines) that can be repetitive or unnecessary for a high-throughput RPC scenario.

   - With raw TCP, the framework can define **only the necessary protocol fields**, ensuring minimal overhead on the wire.

2. **Custom Framing**

   - By using raw TCP, developers can implement their **own protocol header**. This header precisely defines how to structure requests and responses, handle versioning, specify serializer types, etc., without being constrained by HTTP’s standard fields.

3. **Direct Socket Control**

   - TCP grants direct control over **when data is read, how partial packets are handled**, and how data is **buffered** on the network. This control is crucial for **resolving sticky-packet** (partial data merges) or **half-packet** (incomplete data) issues.

4. **Potential Downsides**

   - Manual management of connections requires more work compared to using an HTTP library. However, the **gain in flexibility and performance** often justifies this extra effort for large-scale RPC systems.

**Custom Protocol Header**

The framework **defines a fixed-size header**—in this example, **17 bytes**—to delineate messages. This header is captured by `ProtocolConstant.MESSAGE_HEADER_LENGTH`, and includes fields such as:

- **magic** (1 byte)\
  &#x20;A “magic number” (for instance, `0x1`) helps identify whether incoming data matches the expected protocol.

- **version** (1 byte)\
  &#x20;Used for forward/backward compatibility.

- **serializer** (1 byte)\
  &#x20;Indicates whether the message was serialized with JSON, Kryo, Hessian, or another format.

- **type** (1 byte)\
  &#x20;Distinguishes requests from responses or heartbeat messages.

- **status** (1 byte)\
  &#x20;Indicates success, failure, or error codes (often used in responses).

- **requestId** (8 bytes)\
  &#x20;Allows matching responses to their corresponding requests, supporting asynchronous patterns.

- **bodyLength** (4 bytes)\
  &#x20;Tells the decoder how many bytes to read for the payload.

Example constant definitions from `ProtocolConstant`:

    public interface ProtocolConstant {
        int MESSAGE_HEADER_LENGTH = 17;       // total header size in bytes
        byte PROTOCOL_MAGIC = 0x1;           // magic number
        byte PROTOCOL_VERSION = 0x1;         // protocol version
    }

**Tackling Sticky-Packets**

**The Problem**

Under high throughput, **TCP** can combine multiple writes (from the sender) into a single packet or split a single write across multiple packets. If the application simply reads whatever bytes are available without strict boundaries, partial or concatenated data can cause **deserialization** to fail or produce incorrect results.

**The Solution**

1. **Fixed-Length Header**

   - By reading exactly **17 bytes** first, the system can reliably parse the header, identify the **bodyLength**, and then read precisely that many bytes for the payload.

   - This approach ensures the decoder reassembles each message correctly, avoiding partial merges.

2. **Message Decoder**

   - Classes like `ProtocolMessageDecoder` follow these steps:

     1. **Check** the `magic` byte to confirm the message is valid.

     2. **Extract** `version`, `serializer`, `type`, `status`, `requestId`, and `bodyLength` from the header.

     3. **Read** `bodyLength` bytes from the buffer to get the serialized payload.

     4. **Deserialize** the payload (for example, into an `RpcRequest` or `RpcResponse`) using the serializer specified by `serializer`.

Sample excerpt from `ProtocolMessageDecoder`:

    // Step 1: Validate magic
    byte magic = buffer.getByte(0);
    if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
        throw new RuntimeException("Invalid magic number");
    }

    // Step 2: Parse fixed header fields
    header.setMagic(magic);
    header.setVersion(buffer.getByte(1));
    header.setSerializer(buffer.getByte(2));
    ...

    // Step 3: Determine body length
    int bodyLen = buffer.getInt(13);
    byte[] bodyBytes = buffer.getBytes(17, 17 + bodyLen);

    // Step 4: Deserialize based on 'serializer' key and 'type' field
    ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
    switch (messageTypeEnum) {
        case REQUEST:
            RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
            return new ProtocolMessage<>(header, request);
        // ...
    }

**Practical Implementations**

In Java, frameworks like **Vert.x** (`RecordParser`), **Netty** (`ByteToMessageDecoder`), or even manual read loops can handle partial data. The essential idea is:

1. **Buffer** incoming bytes until at least **17 bytes** (the header length) are available.

2. **Read** the header to determine `bodyLength`.

3. **Wait** until the buffer has at least `bodyLength` more bytes.

4. **Extract** the payload, decode, and form the final `ProtocolMessage` object.

**Summary**

**TCP with a custom protocol** gives this RPC framework **fine-grained control** over how messages are structured and processed. By:

- Using a **17-byte fixed header** (magic, version, serializer, type, status, requestId, bodyLength).

- Reading the exact `bodyLength` from the buffer.

- Decoding with the appropriate serializer (JSON, Kryo, Hessian, etc.),

the framework **eliminates** sticky-packet issues and ensures **reliable**, **high-performance** communication—**without** the overhead and constraints of HTTP.


### **3.6 Load Balancing**

In a distributed RPC framework, multiple service instances often run behind a single service interface. **Load balancing** helps distribute traffic across these instances to minimize latency, avoid overloading, and improve overall scalability. This Custom RPC framework supports **multiple strategies** and leverages **SPI** to dynamically load them.

**Common Load-Balancing Strategies**

1. **Round Robin**

   - Cycles through instances in a **circular** pattern.

   - Uses an `AtomicInteger` to track an index, incrementing it each time a selection occurs.

Example:\
\
`public class RoundRobinLoadBalancer implements LoadBalancer {`

        private final AtomicInteger currentIndex = new AtomicInteger(0);

        @Override
        public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
            if (serviceMetaInfoList.isEmpty()) {
                return null;
            }
            int size = serviceMetaInfoList.size();
            if (size == 1) {
                return serviceMetaInfoList.get(0);
            }
            int index = currentIndex.getAndIncrement() % size;
            return serviceMetaInfoList.get(index);
        }
    }

2. **Random**

   - Selects a random instance from the list.

   - Simple to implement and can achieve **even** distribution over many requests.

Example:\
\
`public class RandomLoadBalancer implements LoadBalancer {`

        private final Random random = new Random();

        @Override
        public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
            int size = serviceMetaInfoList.size();
            if (size == 0) {
                return null;
            }
            if (size == 1) {
                return serviceMetaInfoList.get(0);
            }
            return serviceMetaInfoList.get(random.nextInt(size));
        }
    }

3. **Consistent Hash**

   - Ensures **requests with the same key** map to the same provider node.

   - Useful in stateful scenarios (for example, caching, user sessions) where consistent mapping reduces cache misses.

Example:\
\
`public class ConsistentHashLoadBalancer implements LoadBalancer {`

        private static final int VIRTUAL_NODE_NUM = 100;
        private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

        @Override
        public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
            if (serviceMetaInfoList.isEmpty()) {
                return null;
            }
            // Build the consistent hash ring
            for (ServiceMetaInfo info : serviceMetaInfoList) {
                for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                    int hash = getHash(info.getServiceAddress() + "#" + i);
                    virtualNodes.put(hash, info);
                }
            }
            // Hash the request parameters
            int requestHash = getHash(requestParams);
            Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(requestHash);
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        }

        private int getHash(Object key) {
            return key.hashCode(); // Example hash implementation
        }
    }

4. **Weighted Round Robin (Extension)**

   - Assigns a **weight** to each node. Nodes with higher weights are selected more frequently.

   - Typically implemented by inserting each node into a pool multiple times or adjusting the round-robin index logic based on weight.

   - Not shown in the current code snippets, but easy to add through the **SPI** mechanism (see below).

**PI-Based Strategy Selection**

The framework uses an **SPI** (Service Provider Interface) approach to dynamically pick the load-balancing strategy:

**Factory Lookup**\
`LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());`

- The `rpc.loadBalancer` config key (for example, `"roundRobin"`, `"random"`, or `"consistentHash"`) determines which class is loaded.

- If no key is provided or no matching strategy is found, the framework may **default** to a fallback (e.g., **RoundRobinLoadBalancer**).

**SPI Mappings**

In the file `META-INF/rpc/system/com.xianwei.customrpc.loadbalancer.LoadBalancer`, each line maps a **key** to a **fully qualified class**:\
\
`roundRobin=com.xianwei.customrpc.loadbalancer.RoundRobinLoadBalancer`

    random=com.xianwei.customrpc.loadbalancer.RandomLoadBalancer
    consistentHash=com.xianwei.customrpc.loadbalancer.ConsistentHashLoadBalancer

- The `SpiLoader.load(LoadBalancer.class)` call reads these entries into an internal map.

**Usage in the RPC Flow**

**Discovery**:\
&#x20;The consumer retrieves a list of nodes for a service:\
\
`List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());`

**Selection**:\
&#x20;The consumer obtains a load-balancer based on configuration:\
\
`LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());`

    ServiceMetaInfo selectedServiceMetaInfo =
        loadBalancer.select(requestParams, serviceMetaInfoList);

**Invocation**:\
&#x20;The consumer then makes the RPC call (potentially with retries or fault tolerance) to the chosen node.

**Adding a Custom Load-Balancing Strategy**

One of the major benefits of the **SPI** design is that you can add or swap load balancers without modifying core framework code.

**Step 1: Implement the** `LoadBalancer` **Interface**

Create a new class, for example:

    package com.xianwei.customrpc.loadbalancer;

    import com.xianwei.customrpc.model.ServiceMetaInfo;
    import java.util.List;
    import java.util.Map;

    public class MyCustomLoadBalancer implements LoadBalancer {

        @Override
        public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
            if (serviceMetaInfoList.isEmpty()) {
                return null;
            }
            // Example logic: pick the first node or track real-time metrics
            return serviceMetaInfoList.get(0);
        }
    }

If you want advanced logic, like **least connections** or **latency-based** selection, store relevant metrics (for example, in a thread-safe map) and compute the best node here.

**Step 2: Add an SPI Entry**

In your resource directory, create or modify the file:

    META-INF/rpc/system/com.xianwei.customrpc.loadbalancer.LoadBalancer

Append a line mapping a **unique key** to your class:

    myCustom=com.xianwei.customrpc.loadbalancer.MyCustomLoadBalancer

This key (`"myCustom"`) is what you’ll specify in configuration.

**Step 3: Configure in** `application.properties`

    rpc.loadBalancer=myCustom

At runtime, the framework calls:

    LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());

which resolves `"myCustom"` to `MyCustomLoadBalancer`.

**Step 4: Test and Validate**

- Ensure your **ServiceProxy** or other invocation code is indeed using the key from your config.

- Watch logs or metrics to confirm your custom strategy is selecting nodes as intended.

Example: Weighted Round Robin (Sketch)

    public class WeightedRoundRobinLoadBalancer implements LoadBalancer {

        // Suppose each ServiceMetaInfo includes a 'weight' field
        @Override
        public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
            if (serviceMetaInfoList.isEmpty()) {
                return null;
            }

            // For demonstration, replicate nodes by weight:
            List<ServiceMetaInfo> weightedList = new ArrayList<>();
            for (ServiceMetaInfo info : serviceMetaInfoList) {
                int weight = info.getWeight(); // hypothetical field
                for (int i = 0; i < weight; i++) {
                    weightedList.add(info);
                }
            }

            // Pick randomly or in round-robin manner from this weightedList
            int index = ThreadLocalRandom.current().nextInt(weightedList.size());
            return weightedList.get(index);
        }
    }

**Summary**

Load balancing is a **crucial** aspect of scaling distributed systems. The Custom RPC framework offers:

1. **Built-In Strategies**: Round Robin, Random, Consistent Hash, (optionally Weighted).

2. **SPI Mechanism**: Allows easy addition of **custom** strategies (for example, Weighted Round Robin, Least Connections, or Latency-Based).

3. **Configuration-Driven**: Switching strategies only requires changing `rpc.loadBalancer` in your config file, plus an SPI entry for any new implementations.

This design simplifies experimentation and deployment of **different algorithms**. You can pick the best fit for your application’s **performance** and **reliability** requirements—without forking or redeploying the framework’s core code.


### **3.7 Retry Mechanism**

In a distributed system, transient network failures or momentary timeouts can occur. Rather than failing immediately, many RPC calls benefit from a **retry** mechanism. The Custom RPC framework supports **configurable retry strategies** that you can select or implement as needed.

**Automatic Retries**

If a request fails (for example, due to a network issue or a timeout), the framework can **retry**. Depending on your chosen strategy, retries may:

- **Wait a fixed interval** between attempts,

- **Use an exponential backoff**, or

- Not retry at all (fail immediately).

This flexibility allows you to balance **responsiveness** against **robustness** in the face of intermittent failures.

**Configurable Backoff Examples**

1. **No Retry**

   - Fails on the **first** error, with **no** additional attempts.

Implemented by `NoRetryStrategy`:\
\
`@Slf4j`

    public class NoRetryStrategy implements RetryStrategy {
        @Override
        public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
            // Just execute the RPC call once, no retries
            return callable.call();
        }
    }

- Useful for scenarios where it’s preferable to **fail fast** (for example, if repeated calls might have adverse side effects).

2. **Fixed Interval**

   - Waits a **fixed** amount of time between each retry attempt.

Example: `FixedIntervalRetryStrategy` uses Guava’s **Retryer** to wait **3 seconds** between attempts, for up to **3** total attempts:\
\
`@Slf4j`

    public class FixedIntervalRetryStrategy implements RetryStrategy {

        @Override
        public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
            Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                    .retryIfExceptionOfType(Exception.class)
                    .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .withRetryListener(attempt -> log.info("Retry attempt {}", attempt.getAttemptNumber()))
                    .build();

            return retryer.call(callable);
        }
    }

- A good **middle ground** for handling temporary glitches without hammering the remote service too quickly.

3. **Exponential Backoff** (Extension)

   - **Not** implemented in the framework, but easily added using Guava’s `WaitStrategies.exponentialWait(...)`, or by writing a custom strategy.

   - Each retry **doubles** the wait time (for example, 1s, 2s, 4s...). This helps avoid **overwhelming** the server or the network during an outage or latency spike.

**Integrated with the SPI Approach**

Like serializers and load balancers, **retry strategies** are **pluggable** via SPI:

1. **RetryStrategyFactory**

   - Uses `SpiLoader.load(RetryStrategy.class)` to discover all implementations declared in a file named (for example) `META-INF/rpc/system/com.xianwei.customrpc.retry.RetryStrategy`.

   - It includes a fallback (`NoRetryStrategy`) if no key is specified or matched.

2. **Selecting a Strategy**

   - In `application.properties` (or `.yml`), set a key (for example, `rpc.retryStrategy=fixedInterval`) to indicate which strategy to use.

The consumer-side logic (for example, in `ServiceProxy`) calls something like:\
\
`RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());`

    rpcResponse = retryStrategy.doRetry(() -> VertxClient.doRequest(rpcRequest, selectedServiceMetaInfo));

3. **Configuration Keys**

   - The framework’s default keys might include:

     - `no` for `NoRetryStrategy

       `

     - `fixedInterval` for `FixedIntervalRetryStrategy

       `

   - You can easily add a **custom** key (for example, `"expBackoff"`) mapped to a custom class.

**Example of Adding a Custom Retry Strategy**

**Create a Class Implementing** `RetryStrategy
` For instance, you might write an **ExponentialBackoffRetryStrategy**:\
\
`public class ExponentialBackoffRetryStrategy implements RetryStrategy {`

        @Override
        public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
            Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                    .retryIfExceptionOfType(Exception.class)
                    // Start at 1 second and double the wait each attempt
                    .withWaitStrategy(WaitStrategies.exponentialWait(1, 5, TimeUnit.SECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                    .build();

            return retryer.call(callable);
        }
    }

**Add an SPI Configuration Entry** In `META-INF/rpc/system/com.xianwei.customrpc.retry.RetryStrategy`, add a line:\
\
`expBackoff=com.xianwei.customrpc.retry.ExponentialBackoffRetryStrategy`

**Configure and Use** In your config file (`application.properties`):\
\
`rpc.retryStrategy=expBackoff`

1.  The `RetryStrategyFactory` then loads `ExponentialBackoffRetryStrategy` at runtime.

**Usage in the RPC Flow**

1. **Proxy Invocation**:

   - When the consumer calls a remote method, the proxy assembles an `RpcRequest` and obtains a list of providers via the registry.

2. **Load Balancing**:

   - A chosen load-balancer selects one service instance.

3. **Retry Execution**:

The code calls:\
\
`RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());`

    RpcResponse response = retryStrategy.doRetry(() -> VertxClient.doRequest(rpcRequest, selectedServiceMetaInfo));

- If the call fails, the strategy decides whether to re-attempt, how long to wait, or when to stop and throw an exception.

**Summary**

**Retry Mechanisms** significantly improve **resilience** in distributed systems:

- **No**: Fail fast, no attempts beyond the first.

- **Fixed Interval**: Simple, predictable spacing between retries.

- **Exponential Backoff**: Gently escalates wait times, avoiding meltdown under prolonged outages.

- **Custom**: Implement any specialized logic by writing a new class that implements `RetryStrategy` and registering it in SPI.

Because the **SPI** design provides a **pluggable** architecture, you can add or change retry strategies by editing **configuration** (for example, `rpc.retryStrategy`) and updating the SPI file—without modifying the **core code**.


### **3.8 Fault Tolerance Strategy**

In a distributed RPC environment, service calls may fail due to **network outages**, **server crashes**, or **transient exceptions**. A **fault tolerance strategy** specifies how the system responds when errors occur. By combining fault-tolerance with retries and load balancing, you can maintain higher availability and provide smoother user experiences under failure conditions.

**Common Fault-Tolerance Approaches**

1. **FailFast**

   - **Immediately** throws an exception on error, signaling the caller that something went wrong.

   - Suitable when **fast feedback** is crucial and partial or silent failures are unacceptable.

**Example (**`FailFastTolerantStrategy`**):**\
\
`public class FailFastTolerantStrategy implements TolerantStrategy {`

        @Override
        public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
            throw new RuntimeException("Service error occurred", e);
        }
    }

2. **FailSafe**

   - **Silently** handles exceptions without propagating them, returning a **default** or **empty** response.

   - Useful if it’s okay to **ignore** certain failures and continue operation.

**Example (**`FailSafeTolerantStrategy`**):**\
\
`@Slf4j`

    public class FailSafeTolerantStrategy implements TolerantStrategy {
        @Override
        public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
            log.info("Silently handling exception", e);
            // Return an empty response, so the caller never sees the error
            return new RpcResponse();
        }
    }

3. **FailOver**

   - Retries the **same request** on a **different** provider node if one fails.

   - Common in load-balanced environments to avoid a faulty node.

**Example (**`FailOverTolerantStrategy`**):**\
\
`@Slf4j`

    public class FailOverTolerantStrategy implements TolerantStrategy {
        @Override
        public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
            // Potentially discover another healthy service node and retry
            return null; // not implemented in the snippet
        }
    }

4. **FailBack**

   - Reattempts **failed requests** after a **recovery period** or in the background.

   - Less common in simpler RPC frameworks, but useful in scenarios where data can be safely re-sent later (for example, logging or analytics).

Example (`FailBackTolerantStrategy`):\
\
`@Slf4j`

    public class FailBackTolerantStrategy implements TolerantStrategy {
        @Override
        public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
            // Could push the request to a queue or scheduler for reprocessing
            return null; // not fully implemented in the snippet
        }
    }

**Flexible Configuration via SPI**

Similar to **retry** and **load balancing**, fault-tolerance strategies are **pluggable** through **SPI**:

1. **TolerantStrategyFactory**

   - Calls `SpiLoader.load(TolerantStrategy.class)`, reading a file like `META-INF/rpc/system/com.xianwei.customrpc.fault.tolerant.TolerantStrategy` that maps keys (for example, `failFast`, `failSafe`, `failOver`, `failBack`) to implementing classes.

   - Provides a fallback strategy (`FailFastTolerantStrategy`) if no configuration is specified.

2. **Runtime Selection**

In your `application.properties` (or `.yml`):\
\
`rpc.tolerantStrategy=failSafe`

The consumer logic (for example, in `ServiceProxy`) obtains the configured strategy:\
\
`TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());`

    rpcResponse = tolerantStrategy.doTolerant(context, exception);

- The specific approach (fail fast, fail over, etc.) is determined purely by **configuration**, with no need to change core code.

**Adding a Custom Fault-Tolerance Strategy**

If you need a specialized approach, for example, a **hybrid** of failover plus a fallback service invocation, you can write your own:

**Implement the** `TolerantStrategy` **Interface**\
`public class MyCustomTolerantStrategy implements TolerantStrategy {`

        @Override
        public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
            // Example: log, then route request to a fallback service
            // or return a custom error response
            return new RpcResponse(...); // fill in as needed
        }
    }

1. **Register in SPI**

Edit/create the file:\
\
`META-INF/rpc/system/com.xianwei.customrpc.fault.tolerant.TolerantStrategy`

Append a line mapping a key (for example, `myCustom`) to your class:\
\
`myCustom=com.xianwei.customrpc.fault.tolerant.MyCustomTolerantStrategy`

2. **Configure**

In your config:\
\
`rpc.tolerantStrategy=myCustom`

After these steps, the framework automatically loads `MyCustomTolerantStrategy` whenever it sees `rpc.tolerantStrategy=myCustom`. This is **entirely** decoupled from the rest of the RPC logic, so you can quickly adapt to evolving business or resilience requirements.

**Combining with Load Balancing and Retry**

The **fault tolerance** mechanism typically works **after** load balancing and **after** the retry strategy. A typical call sequence on the consumer might look like:

1. **Lookup**: Find service nodes from the registry.

2. **Load Balancer**: Pick one node.

3. **Retry**: Attempt the call up to `N` times, possibly switching nodes if the strategy supports it.

4. **Fault Tolerance**: If final attempts still fail, apply your tolerant strategy (fail fast, safe, over, or back).

This interplay ensures a **multi-layered defense** against different failure modes.

**Summary**

Fault tolerance strategies address **how** the RPC framework responds when **all** retries fail or a **serious exception** occurs. By selecting the correct approach:

- **FailFast**: Propagate the error immediately.

- **FailSafe**: Hide the error and return a default object.

- **FailOver**: Retry on another node automatically.

- **FailBack**: Defer the request for later reprocessing.

- **Custom**: Implement any specialized logic needed.

Because the framework uses **SPI**, you can switch or customize these behaviors at **deployment time**, enabling **rapid experimentation** and **tunable** resilience for diverse production environments.


### **3.9 Startup Mechanism**

A well-defined **startup process** is vital for any RPC framework. It ensures **configuration** is loaded, **services** are registered, and **network listeners** are properly initialized before handling requests. This framework provides two main classes—**ProviderBootstrap** and **ConsumerBootstrap**—to streamline the initialization process for providers and consumers, respectively.

**ProviderBootstrap**

When building a **provider** (server) application:

1. **Global Initialization**

   - Calls `RpcApplication.init()` to load the **global RPC configuration** (`RpcConfig`).

   - The configuration includes details like registry type/address, serializer settings, server host/port, etc.

2. **Service Registration**

   - Accepts a list of `ServiceRegisterInfo<?>` entries, each mapping a **service name** (for example, `UserService.class.getName()`) to its **implementation** (`UserServiceImpl.class`).

   - For each service:

     1. **LocalRegistry**: Binds the service name to the implementation class in a **local in-memory map**.

     2. **Registry**: Constructs a `ServiceMetaInfo` (name, host, port) and calls `registry.register(serviceMetaInfo)`, advertising this service instance to the external registry (Etcd, ZooKeeper, etc.).

3. **TCP Server Startup**

   - Creates and starts a **Vert.x**-based `VertxServer` (or another TCP/HTTP server) on the configured port (for example, `8080`).

   - This server listens for **incoming RPC calls**, decodes requests (e.g., using a custom protocol), finds the right local service via reflection, invokes the method, and **encodes** the response back to the consumer.

4. **Lifecycle and Shutdown**

   - A **shutdown hook** in `RpcApplication` ensures the registry is **gracefully destroyed** (removing service entries) if the JVM exits.

   - This prevents stale service info from lingering in the registry.

**Example** (`ProviderBootstrap.init()` partial flow):

    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        RpcApplication.init();  // Load and parse global config
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // For each service to register
        for (ServiceRegisterInfo<?> info : serviceRegisterInfoList) {
            String serviceName = info.getServiceName();
            // Local in-memory bind
            LocalRegistry.register(serviceName, info.getImplClass());

            // Remote registry registration
            ServiceMetaInfo meta = new ServiceMetaInfo();
            meta.setServiceName(serviceName);
            meta.setServiceHost(rpcConfig.getServerHost());
            meta.setServicePort(rpcConfig.getServerPort());
            registry.register(meta);
        }

        // Start the TCP server
        VertxServer vertxTcpServer = new VertxServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }

**ConsumerBootstrap**

When building a **consumer** (client) application:

1. **Global Initialization**

   - Calls `RpcApplication.init()` (or `ConsumerBootstrap.init()`) to load the **global RPC configuration**.

   - No server socket is needed because the consumer typically **initiates** connections rather than listening for requests.

2. **Service Discovery**

   - When the consumer attempts to call a remote service (e.g., `UserService`), it queries the registry for a **list of providers**.

   - The **load balancer** (if configured) selects the appropriate node.

3. **Dynamic Proxies**

   - The consumer uses `ServiceProxyFactory` (or a similar mechanism) to create **proxy objects** for each interface.

   - When your code calls `userService.getUser(user)`, the proxy:

     1. **Encodes** the request using a chosen serializer and protocol.

     2. **Sends** it over the network to the selected provider.

     3. **Waits** for the response, **decodes** it, and returns the result as if it were a local call.

4. **Configuration-Driven**

   - The consumer’s `.properties` (or `.yml`) might specify registry details (`rpc.registry=etcd`), serializer type (`rpc.serializer=json`), load balancing (`rpc.loadBalancer=random`), retry strategies, etc.

   - Changing these values can alter the consumer’s behavior without modifying source code.

**Example** (`ConsumerBootstrap.init()` partial flow):

    public static void init() {
        // 1. Initialize the RPC framework (loads config, registry, etc.)
        RpcApplication.init();

        // 2. Any additional consumer-specific steps could go here
        //    e.g., caching service discovery, pre-warming connections, etc.
    }

**Overall Lifecycle**

1. **Provider**:

   - **Setup** global config → **Register** local and remote services → **Start** TCP server → **Handle** requests.

2. **Consumer**:

   - **Setup** global config → **Obtain** registry references → **Create** dynamic proxies → **Make** RPC calls → **Decode** responses.

Both **ProviderBootstrap** and **ConsumerBootstrap** rely on the **RpcApplication** class for **global initialization**, which in turn loads or merges `.properties` files using `ConfigUtils` and sets up a **Registry** implementation (Etcd, Zookeeper, etc.). This consistent pattern ensures minimal boilerplate code while maintaining **flexibility** (SPI-driven design) for advanced scenarios.

**Summary**

The **startup mechanism** ties together all components—configuration, registry, local service registration, network server/client—into a straightforward workflow. By centralizing these steps in **ProviderBootstrap** and **ConsumerBootstrap**:

- **Providers** can easily expose new services by adding them to the registration list, automatically advertising their endpoints and hosting a TCP server to process incoming calls.

- **Consumers** can seamlessly load relevant config (like registry addresses, retry/fault-tolerance strategies), fetch service endpoints, and create dynamic proxies to call them.

In essence, this startup sequence **encapsulates** the complexities of distributed service discovery and networking, letting developers focus on **business logic** rather than infrastructure details.


## **Conclusion**

This Custom RPC framework provides a **comprehensive, extensible, and performance-focused** solution for building distributed services. It tackles every layer of the RPC pipeline—from **global configuration** and **registry integration** to **serialization**, **load balancing**, **retry**, **fault tolerance**, and **networking**—while offering a modular **SPI-based** design that encourages customization. By separating the **provider** and **consumer** modules, using a **core** library for transport and abstraction, and sharing models/interfaces in a **common** package, teams can focus on **business logic** rather than plumbing.

Key points to remember:

1. **Global Configuration**

   - Load environment-specific `.properties` or `.yml` files (for example, `application-dev.properties` or `application-prod.properties`).

   - Override defaults easily and add new configuration keys without changing core code.

2. **SPI Mechanism**

   - Plug in custom **serializers**, **registries**, **load-balancing** strategies, **retry** policies, and **fault tolerance** approaches.

   - Add a class and an entry in an SPI configuration file (for example, `META-INF/rpc/system/com.xianwei.customrpc.loadbalancer.LoadBalancer`) to extend or replace built-ins.

3. **Registry Center**

   - Dynamically registers providers and updates consumer caches as nodes come online or go offline.

   - Watchers and heartbeats ensure **real-time** synchronization and **fault tolerance**.

4. **TCP Transport with Custom Protocol**

   - A **17-byte** fixed header delineates requests, preventing sticky/partial packet issues.

   - Avoids the overhead of HTTP and enables direct socket management, leading to faster communication in large-scale environments.

5. **Load Balancing & Fault Tolerance**

   - Multiple ready-to-use load-balancing strategies (Round Robin, Random, Consistent Hash) and fault-tolerance options (FailFast, FailSafe, FailOver, FailBack).

   - Easily enhanced or replaced through SPI, letting you adapt to unique traffic patterns and reliability needs.

6. **Retries & Mock Testing**

   - Configurable retry strategies (for example, **fixed interval**, **no retry**, or **exponential backoff**) to improve resilience during transient failures.

   - Mock interfaces allow rapid **integration testing** without requiring all remote services to be online.

7. **Provider & Consumer Startup**

   - **ProviderBootstrap**: Registers local services, publishes them to the registry, and starts a TCP server.

   - **ConsumerBootstrap**: Loads the config, initializes the registry references, and creates dynamic proxies that seamlessly invoke remote services.

Taken together, these components form a **robust** and **flexible** RPC framework suitable for a wide range of distributed applications. By relying on **configuration** (rather than tight coupling) and **SPI** (rather than monolithic architecture), the framework promotes **maintainability**, **scalability**, and **evolution** over time.
