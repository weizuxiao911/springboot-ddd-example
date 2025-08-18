
### 应用架构设计

项目工程秉承 DDD（领域驱动设计）思想，使用 Maven Modules 方式构建，采用分层架构设计，将项目工程内部拆分成 4 层结构。


#### 一、parent 说明

虽然项目工程使用 Maven Modules 方式构建，但最外层的 pom.xml 仅作为管理 modules 实现模块聚合，内部各层仍采用统一封装的 parent 依赖包。如此设计的主要思考如下：

1. 为确保项目工程依赖包版本的统一性，需引入统一的 parent 包进行依赖管理。
2. 项目工程最外层的 pom.xml 仅承担 modules 的聚合管理，不依赖 parent，内部模块自行依赖 parent，这样可减少包依赖层级的复杂度。


#### 二、DDD 四层结构

##### 1. 领域层（domain）

- 核心职责：根据业务抽象出领域模型（充血模型）、领域服务、领域事件、值对象等，对业务进行具象化（封装业务规则和核心逻辑）。
- 技术实现：纯 Java 实现，不依赖任何 Spring 组件或技术框架。
- 核心内容
  - 充血模型的领域对象（实体、聚合根，包含业务行为和规则）
  - 领域服务（领域内跨实体的业务逻辑，需多个实体协作完成的操作）
  - 仓储接口（Repository 接口，定义领域对象的持久化契约，不包含实现）
  - 值对象、枚举、领域事件（封装无唯一标识的属性或事件通知逻辑）


##### 2. 应用层（application）

- 核心职责：通过应用服务实现业务流程编排，协调领域层完成用例，对外提供业务能力接口。
- 技术实现：纯 Java 实现（包含接口和核心逻辑），不依赖 Spring 组件，可独立运行和测试。
- 核心内容：
  - 应用服务（接口+实现）：编排领域服务和领域对象，实现完整业务流程（如“用户注册→创建账号→发送通知”）
  - 命令/查询对象（Command/Query）：封装应用服务的输入参数，明确操作意图（避免方法参数过多）
  - DTO（数据传输对象）：定义应用层与接口层之间的数据交互格式，屏蔽领域对象细节


##### 3. 接口层（interfaces）

- 核心职责：定义对外暴露的访问接口契约，实现外部请求与应用层的适配。
- 技术实现：仅定义接口和转换规则，依赖 Spring 注解（如`@GetMapping`、`@FeignClient`），不包含业务逻辑。
- 核心内容
  - Controller 接口：定义 HTTP 接口契约（路径、参数、响应格式）
  - Feign 客户端接口：定义服务间远程调用契约
  - DTO 转换器接口：声明领域对象与 DTO 的转换规则（如`User → UserDTO`）


##### 4. 基础设施层（infrastructure）

- 核心职责：提供技术实现支持，落地上层定义的接口，处理与外部资源的交互。
- 技术实现：依赖 Spring Boot 及其他框架（如 MyBatis、Redis），实现所有技术细节。
- 核心内容：
  - Controller 实现：实现接口层的 Controller 接口，处理 HTTP 请求（参数解析、调用应用服务、返回响应）
  - 仓储实现：实现领域层的 Repository 接口（如用 MyBatis/JPA 操作数据库）
  - 应用服务适配器：对应用层的应用服务进行技术增强（如添加`@Transactional`、`@Service`注解，注册为 Spring Bean）
  - DTO 转换器实现：实现接口层的转换规则（如用 MapStruct 完成对象映射）
  - Spring 配置：Bean 注册、事务管理、缓存配置等技术组件


#### 三、模块依赖关系

1. 严格遵循**单向依赖**原则，避免循环依赖。

```plaintext
infrastructure → application → domain
       ↑                ↑
       └── interfaces ──┘

# 基础设施层依赖接口层和应用层
# 接口层依赖应用层
# 应用层依赖领域层
# 领域层不依赖任何层
```

2. 父模块 pom.xml 中定义构建顺序（按依赖层级排序）

```xml
<modules>
    <module>domain</module>
    <module>application</module>
    <module>interfaces</module>
    <module>infrastructure</module>
</modules>
```


#### 四、核心优势

1. 业务与技术解耦：领域层和应用层纯 Java 实现，核心业务逻辑不依赖框架，可独立测试和迁移。
2. 可替换性：基础设施层的技术实现（如从 JPA 换为 MyBatis）不影响上层业务逻辑。
3. 团队协作：领域专家可专注于 domain 模块设计业务规则，开发人员专注于 infrastructure 模块实现技术细节。
4. 可测试性：领域层和应用层可通过单元测试验证业务逻辑，无需启动 Spring 容器或依赖外部资源。


#### 五、注意事项

1. 接口层的 FeignClient 接口需在基础设施层通过`@EnableFeignClients`扫描注册。
2. 仓储接口的实现类需用`@Repository`注解，确保被 Spring 容器扫描管理。
3. 事务注解`@Transactional`仅在基础设施层的“应用服务适配器”上使用，控制业务流程的事务边界。
4. 领域层禁止引入任何框架注解（如 JPA 的`@Entity`应放在基础设施层的数据库实体类上）。
5. 应用层的核心逻辑需独立于 Spring 实现，基础设施层仅通过“适配器”模式添加技术注解（如`@Service`）。


#### 六、领域模型定义及应用  

领域模型是DDD的核心，通过**值对象、实体、聚合根、领域服务、领域事件**等概念封装业务规则，确保业务逻辑的内聚性和一致性。以下是各模型的定义、应用场景及实现示例：  

##### 1. 值对象（Value Object）  

- **定义**：无唯一标识（ID）的对象，通过属性值描述一个不可变的概念（如“地址”、“金额”），属性值相等即视为相同对象。  
- **核心特征**：不可变性（创建后属性不可修改）、属性相等性（重写`equals()`和`hashCode()`）、无生命周期管理。  
- **应用场景**：封装多个关联属性（如用户的收货地址、订单的金额与币种）。  

**示例**：  

```java
// 领域层：值对象（地址）
public class Address {
    private final String province; // 不可变，用final修饰
    private final String city;
    private final String detail;

    // 构造器初始化所有属性，无setter方法
    public Address(String province, String city, String detail) {
        // 校验属性合法性（体现业务规则）
        if (StringUtils.isEmpty(province)) {
            throw new IllegalArgumentException("省份不能为空");
        }
        this.province = province;
        this.city = city;
        this.detail = detail;
    }

    // 重写equals和hashCode，基于属性值判断相等性
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(province, address.province) &&
                Objects.equals(city, address.city) &&
                Objects.equals(detail, address.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(province, city, detail);
    }
}
```  

##### 2. 实体（Entity）  

- **定义**：有唯一标识（ID）的对象，通过ID区分不同实例，属性可修改，具有独立的生命周期（如“用户”、“商品”）。  
- **核心特征**：唯一标识（ID）、可变性（属性可修改）、生命周期管理（创建、更新、删除）、包含业务行为。  
- **应用场景**：需要跟踪变化的核心业务对象（如用户的基本信息、商品的库存）。  

**示例**：  

```java
// 领域层：实体（用户的收货地址实体，需独立更新）
public class UserAddress {
    private final Long id; // 唯一标识
    private Long userId; // 关联用户ID
    private Address address; // 关联值对象（地址）
    private boolean isDefault; // 是否默认地址

    // 构造器：创建时必须指定ID和核心属性
    public UserAddress(Long id, Long userId, Address address) {
        this.id = id;
        this.userId = userId;
        this.address = address;
    }

    // 业务行为：设置为默认地址（封装业务规则）
    public void setAsDefault() {
        this.isDefault = true;
    }

    // 业务行为：更新地址信息（属性可修改）
    public void updateAddress(Address newAddress) {
        this.address = newAddress; // 替换值对象（值对象不可变，直接替换）
    }

    // 仅暴露getter，避免外部直接修改属性
    public Long getId() { return id; }
    public boolean isDefault() { return isDefault; }
}
```  


##### 3. 聚合根（Aggregate Root）  

- **定义**：聚合（一组关联对象）的根节点，是聚合对外的唯一入口，负责维护聚合内对象的一致性，包含聚合内的实体和值对象。  
- **核心特征**：唯一标识（ID）、管理聚合内对象的生命周期、封装聚合内的业务规则、通过仓储（Repository）持久化。  
- **应用场景**：代表一个完整的业务概念（如“订单”聚合包含订单头、订单项、收货地址等）。  

**示例**：  

```java
// 领域层：聚合根（订单）
public class Order {
    private final Long id; // 聚合根唯一标识
    private Long userId;
    private List<OrderItem> items; // 聚合内实体（订单项）
    private Address shippingAddress; // 聚合内值对象（收货地址）
    private OrderStatus status; // 订单状态（枚举）
    private LocalDateTime createTime;

    // 构造器：创建订单时初始化聚合根及内部对象
    public Order(Long id, Long userId, List<OrderItem> items, Address shippingAddress) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.CREATED;
        this.createTime = LocalDateTime.now();
    }

    // 业务行为：添加订单项（确保聚合内一致性）
    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有未支付的订单可添加商品"); // 业务规则
        }
        this.items.add(item);
    }

    // 业务行为：订单支付（状态流转规则）
    public void pay() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有创建状态的订单可支付");
        }
        this.status = OrderStatus.PAID;
    }

    // 仅暴露必要的getter，聚合内对象通过业务行为操作
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}

// 聚合内实体（订单项）
class OrderItem {
    private Long productId;
    private int quantity;
    private BigDecimal price;

    // 构造器和业务行为...
}
```  


##### 4. 领域服务（Domain Service）  

- **定义**：处理跨实体/聚合的业务逻辑，当业务规则无法归属到单个实体或聚合根时，由领域服务协调完成。  
- **核心特征**：无状态（不保存数据）、依赖领域对象（实体/聚合根）、封装跨对象的业务规则。  
- **应用场景**：需要多个实体/聚合协作的业务逻辑（如“用户注册时检查手机号唯一性”、“订单支付时扣减库存”）。  

**示例**：  

```java
// 领域层：领域服务（订单支付领域服务，跨订单和库存聚合）
public class OrderPaymentService {
    // 依赖其他聚合的仓储接口（通过依赖注入）
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public OrderPaymentService(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // 业务逻辑：支付订单并扣减库存（跨聚合协作）
    public void payOrder(Long orderId) {
        // 1. 获取订单聚合根
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        
        // 2. 校验订单状态（调用聚合根的业务行为）
        order.pay(); // 订单状态流转为“已支付”
        
        // 3. 扣减库存（跨聚合操作）
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId());
            inventory.deduct(item.getQuantity()); // 调用库存实体的扣减行为
            inventoryRepository.save(inventory);
        }
        
        // 4. 保存订单状态
        orderRepository.save(order);
    }
}
```  


##### 5. 领域事件（Domain Event）  

- **定义**：当领域中发生重要业务事件时，由领域对象发布的事件，用于通知其他模块或系统（如“订单创建成功”、“支付完成”）。  
- **核心特征**：记录事件发生的时间和相关数据、由领域对象主动发布、可被事件监听器处理。  
- **应用场景**：实现跨模块/系统的解耦通信（如订单创建后发送通知、支付完成后触发物流）。  

**示例**：  

```java
// 领域层：领域事件（订单创建事件）
public class OrderCreatedEvent {
    private final Long orderId;
    private final Long userId;
    private final LocalDateTime createTime;

    public OrderCreatedEvent(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId = userId;
        this.createTime = LocalDateTime.now();
    }

    // getter...
}

// 聚合根中发布事件
public class Order {
    // 其他属性和方法...

    // 构造器：创建订单时发布事件
    public Order(Long id, Long userId, List<OrderItem> items, Address shippingAddress) {
        // 初始化逻辑...
        // 发布事件（通过事件发布器，由基础设施层实现）
        DomainEventPublisher.publish(new OrderCreatedEvent(id, userId));
    }
}

// 基础设施层：事件发布器实现（依赖Spring事件机制）
@Component
public class DomainEventPublisher {
    private static ApplicationEventPublisher springPublisher;

    // 注入Spring的事件发布器
    public DomainEventPublisher(ApplicationEventPublisher publisher) {
        DomainEventPublisher.springPublisher = publisher;
    }

    // 发布领域事件
    public static void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}

// 事件监听器（基础设施层或应用层）
@Component
public class OrderCreatedListener {
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 处理事件：如发送短信通知、创建物流单等
        System.out.println("订单" + event.getOrderId() + "创建成功，用户ID：" + event.getUserId());
    }
}
```  


##### 6. 领域模型的协作关系  

- **聚合根**是领域模型的核心，通过ID关联其他实体和值对象，对外提供业务行为；  
- **实体**通过ID独立存在，可属于某个聚合，也可单独作为聚合根；  
- **值对象**依附于实体或聚合根，通过属性描述特征，无独立生命周期；  
- **领域服务**协调多个聚合或实体完成跨边界的业务逻辑；  
- **领域事件**实现领域模型间的解耦通信，触发后续业务流程。  

##### 7. 其他说明

- **聚合根**对外提供业务行为，这个“外”是指聚合边界之外，比如同个领域层下的其他聚合或应用层。
- 应用层可访问**聚合根实体**，但禁止直接访问**非聚合根的普通实体**，普通实体需**通过所属聚合根访问**。
- 优先使用**聚合根实体**承载业务规则，仅当协调多个聚合或实体完成跨边界的业务逻辑才引入**领域服务**。领域服务的设计需警惕 “什么都往里塞”（避免 “领域服务膨胀”），遵循：
  - 能放在聚合根里的业务规则，绝不放在领域服务；
  - 领域服务只做 “协调”，不做 “具体业务规则实现”（具体规则仍由聚合根的方法实现）。

