Custom RPC Docs


## **1. Introduction**

This Custom RPC framework aims to streamline **remote procedure calls** in a distributed environment. It abstracts away complexities of **networking**, **serialization**, **load balancing**, and **fault tolerance** so that teams can focus on **business logic** rather than the details of inter-service communication.

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfwdZjZDHshLm0ww--S1t1Z_XY8kjZfJBr60f-S1-fi16Mm7MHZDKYT56B93AEi7JTjzIRyx8UsxPBegvwyCCF9mE1UpthY50Aycl_jN91RsK5yqUsBG5unWp4kuyawF4IScOuF?key=neYAAH9DSvM-3sjiH_tpgOlR)


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

1. A **model** or entity class (e.g., `User`).

2. A **service interface** (e.g., `UserService`).

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

**Start the Provider** with a main class (e.g., `ProviderExample`). This sets up the RPC server and registers your service:\
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


### **2.4 Putting It All Together**

1. **Run** `ProviderExample`: Publishes `UserServiceImpl` to the registry.

2. **Run** `ConsumerExample`: Acquires the `UserService` proxy, calls `getUser()`, and displays the returned data.

3. **Verify Logs**: Observe console outputs in both consumer and provider to confirm successful remote invocation.


## **3. Key Features**

Below are the **9 key features** of this Custom RPC framework, each explained in more depth.


### **3.1 Global Configuration Loading**

- **Centralized Configuration**:

  - Uses `.properties` or `.yml` files (for instance, `application.properties`, `application-dev.properties`, `application-prod.properties`).

  - Allows environment-based overrides for registry addresses, serializer types, load balancer strategies, and more.

- **Extendable with Tools (e.g., Hutool)**:

  - A `ConfigUtils` class can parse configuration files once (often with a double-check singleton).

  - If no custom configuration file is found, the framework uses default settings (e.g., a default serializer or default registry type).

  - Users can define additional keys (for example, `rpc.mock=true` or `rpc.serializer=json`) and the framework picks these up automatically during initialization.


### **3.2 Serializer and SPI Mechanism**

- **Multiple Serializer Options**:

  - **JDK**: Built-in Java serialization.

  - **JSON**: Typically uses Jackson.

  - **Hessian**: Binary, cross-language protocol from the Hessian library.

  - **Kryo**: High performance, efficient for Java objects.

  - **Protobuf**: Google’s Protocol Buffers, very compact and version-tolerant.

- **SPI (Service Provider Interface)**:

  - A custom SPI loader searches directories (e.g., `META-INF/rpc/system` or `META-INF/rpc/custom`) for config files mapping “serializer keys” (like `json`) to implementing class names (e.g., `com.company.JsonSerializer`).

  - The framework then uses reflection to load the appropriate serializer at runtime.

  - This design allows new or custom serializers to be added without modifying core code.


### **3.3 Registry Center**

- **Service Registration and Discovery**:

  - The provider advertises `UserService` (and others) to a registry (Etcd, ZooKeeper, Redis, etc.), indicating its address and port.

  - The consumer queries the registry for available instances of that service and obtains network details to establish connections.

- **Support for Multiple Registry Implementations**:

  - You can configure which registry to use in your `.properties` file (e.g., `rpc.registry=etcd` or `rpc.registry=zookeeper`).

  - A central `RegistryFactory` (also driven by SPI) locates the correct registry implementation for you.


### **3.4 Registry Center Optimization**

- **Removing Invalid Nodes**:

  - Watchers or heartbeat mechanisms automatically remove unresponsive providers from the registry.

  - This keeps consumer-side lookups consistent and avoids sending requests to dead endpoints.

- **Local Caching of Service Information**:

  - The consumer can cache registry data (e.g., a list of providers) to reduce registry load and to continue operating if the registry becomes unavailable temporarily.

  - This caching mechanism improves performance and resilience.


### **3.5 TCP Protocol & Sticky-Packet Issue**

- **Custom Protocol Header**:

  - The framework can define a fixed-size header (e.g., 17 bytes) including fields like `messageId`, `magic`, `version`, `serializerType`, `messageType`, `messageState`, and `bodySize`.

  - This header enables precise handling of request boundaries.

- **Tackling Sticky-Packets**:

  - A decoder reads the header first, extracts the `bodySize`, then reads that exact number of bytes to form one message.

  - This prevents partial merges of messages on high-throughput TCP channels.

- **Practical Implementations**:

  - Tools like Vert.x `RecordParser` or Netty’s `ByteToMessageDecoder` help reassemble incoming bytes into complete RPC messages.


### **3.6 Load Balancing**

- **Common Load-Balancing Strategies**:

  - **Round Robin**: Calls providers in a cyclical pattern.

  - **Random**: Chooses any available provider at random.

  - **Consistent Hash**: Requests with the same key always map to the same provider to minimize cache misses.

  - **Weighted Round Robin**: Some providers can receive more traffic based on assigned weights.

- **SPI-Based Strategy Selection**:

  - A `LoadBalancerFactory` can dynamically pick a strategy based on config (e.g., `rpc.loadbalancer=consistenthash`).

  - This allows you to test and swap strategies easily.


### **3.7 Retry Mechanism**

- **Automatic Retries**:

  - If a request fails (network issue, timeout, etc.), the framework can retry with the same or another provider, depending on your configuration.

- **Configurable Backoff**:

  - **No retry**: Immediately fail on the first error.

  - **Fixed interval**: Wait a set time, then retry.

  - **Exponential backoff**: Each subsequent retry waits longer (e.g., 1s, 2s, 4s).

  - Using libraries like **Guava-Retrying** to implement these strategies in code.

- **Integrated with the SPI Approach**:

  - A `RetryStrategyFactory` can select the correct retry strategy (`fixedInterval`, `exponentialBackoff`, etc.) based on the key provided in `application.properties`.


### **3.8 Fault Tolerance Strategy**

- **Common Fault-Tolerance Approaches**:

  - **FailFast**: Immediately throw an exception on error.

  - **FailSafe**: Silently handle the exception, possibly returning a null or default object.

  - **FailOver**: Attempt the same request on another provider instance.

  - **FailBack**: Reattempt failed requests after some recovery period (less common in simpler RPC frameworks).

- **Flexible Configuration**:

  - Similar to load balancers and retry strategies, a `FaultToleranceFactory` can pick a strategy based on config (e.g., `rpc.faultTolerance=failover`).

  - Combined with load balancing and retries, this can greatly improve resilience.


### **3.9 Startup Mechanism**

- **ProviderBootstrap**:

  1. Loads and parses global configs.

  2. Binds server sockets or starts a TCP listener.

  3. Registers available service interfaces (e.g., `UserService`) and their implementations (e.g., `UserServiceImpl`) in a local map.

  4. Publishes this provider info to the chosen registry backend.

  5. Waits for and handles incoming requests (decoding, invocation via reflection, and encoding of responses).

- **ConsumerBootstrap**:

  1. Loads global configs (including which registry to use).

  2. Discovers or refreshes available providers for the required services.

  3. Creates dynamic proxies (e.g., `ServiceProxyFactory.getProxy(UserService.class)`).

  4. On invocation, the proxy sends a properly encoded request to a chosen provider, receiving a serialized response and returning it to the consumer code.
