### 领域层（Domain）


#### 一、总体概述  

领域层是系统的**业务核心**，直接映射企业的业务概念、规则和流程，独立于技术实现（如框架、数据库、UI等）。它是领域驱动设计（DDD）的核心层次，决定系统的业务能力和稳定性。  

领域层的核心价值：  

- 封装**不可变的业务真理**（如“订单未支付时可取消”“库存不足时无法下单”）；  
- 通过领域模型表达业务概念，通过领域服务协调复杂逻辑；  
- 隔离技术细节，确保业务逻辑的纯粹性和可维护性。  

核心组成：  

- **实体（Entity）**：具有唯一标识的可变对象（如`Order`），承载业务行为；  
- **值对象（Value Object）**：无唯一标识的不可变对象（如`Money`），以属性值定义身份；  
- **聚合根（Aggregate Root）**：聚合的入口点，维护聚合内对象的一致性（如`Order`包含`OrderItem`）；  
- **领域服务（Domain Service）**：封装跨实体/聚合的复杂业务逻辑；  
- **领域事件（Domain Event）**：捕捉领域内的状态变化（如`OrderPaidEvent`）；  
- **仓储接口（Repository Interface）**：定义领域对象的持久化契约（由基础设施层实现）。  


#### 二、基本原则  

1. **业务驱动，脱离技术细节**  
   领域层设计完全基于业务需求，而非技术框架（如Spring）或存储方式（如MySQL）。例如，`Order`的`pay()`方法应体现“支付后状态变更”的业务规则，而非数据库字段更新逻辑。  

2. **高内聚，低耦合**  
   - 同一业务概念的属性和行为封装在同一对象中（如`Order`包含订单编号、金额及`cancel()`等方法）；  
   - 不同领域对象通过明确关联（如聚合根与子实体）或领域服务交互，避免直接依赖。  

3. **实体与值对象分离**  
   - 实体：有唯一标识（`id`），状态可变化（如订单从“待支付”到“已完成”）；  
   - 值对象：无唯一标识，以属性值定义身份（如`Money`的金额+币种），**不可变**（修改时需创建新对象）。  

4. **聚合边界清晰**  
   聚合是一组紧密关联的领域对象（如`Order`+`OrderItem`），通过聚合根保证内部一致性（如“订单总金额=所有订单项金额之和”）。  

5. **领域逻辑内聚**  
   避免“贫血模型”（仅含getter/setter的数据容器），将业务行为封装在领域对象中（如`Order.pay()`而非外部工具类）。  


#### 三、示例代码  

以电商系统的“订单领域”为例，展示领域层核心组件的设计与实现。  

##### 1. 基础组件（值对象、枚举、异常）  

###### （1）值对象（Value Object）  
```java
// 订单ID（值对象，封装唯一标识）
public class OrderId {
    private final String value;

    // 私有构造，通过静态方法创建（保证不可变）
    private OrderId(String value) {
        validate(value);
        this.value = value;
    }

    // 生成新订单ID（业务规则：前缀+时间戳+随机数）
    public static OrderId generate() {
        String prefix = "ORDER_";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf(new Random().nextInt(1000));
        return new OrderId(prefix + timestamp + "_" + random);
    }

    // 从字符串恢复ID（如从数据库查询时）
    public static OrderId from(String value) {
        return new OrderId(value);
    }

    // 校验ID格式（业务规则）
    private void validate(String value) {
        if (value == null || !value.startsWith("ORDER_")) {
            throw new InvalidDomainException("订单ID格式无效：" + value);
        }
    }

    // 仅暴露getter，无setter（保证不可变）
    public String getValue() {
        return value;
    }

    // 重写equals和hashCode（值对象以属性值判断相等）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return value.equals(orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

// 金额（值对象，包含金额和币种）
public class Money {
    private final BigDecimal amount; // 金额
    private final String currency;   // 币种（如CNY、USD）

    private Money(BigDecimal amount, String currency) {
        validate(amount, currency);
        this.amount = amount;
        this.currency = currency;
    }

    // 工厂方法：创建金额对象
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    // 业务规则校验：金额非负，币种合法
    private void validate(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDomainException("金额不能为负数：" + amount);
        }
        if (currency == null || !Arrays.asList("CNY", "USD").contains(currency)) {
            throw new InvalidDomainException("不支持的币种：" + currency);
        }
    }

    // 金额加法（返回新对象，原对象不变）
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvalidDomainException("币种不匹配：" + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // getter（无setter）
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
```

###### （2）枚举（状态定义）  

```java
// 订单状态（体现业务流转规则）
public enum OrderStatus {
    PENDING_PAYMENT("待支付"),  // 初始状态
    PAID("已支付"),             // 支付后
    SHIPPED("已发货"),          // 商家发货后
    COMPLETED("已完成"),        // 买家确认收货
    CANCELLED("已取消");        // 支付前可取消

    private final String desc;
    OrderStatus(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
```

###### （3）领域异常（业务规则违反时抛出）  
```java
// 领域层通用异常（业务规则被违反）
public class InvalidDomainException extends RuntimeException {
    public InvalidDomainException(String message) {
        super(message);
    }
}

// 订单状态转换异常（如“已支付订单不能取消”）
public class InvalidOrderStatusException extends InvalidDomainException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
```

##### 2. 实体与聚合根（Entity & Aggregate Root）  

###### （1）订单项（子实体，属于Order聚合）  
```java
// 订单项（子实体，无独立生命周期，依赖Order存在）
public class OrderItem {
    // 子实体ID（仅在聚合内唯一，无需全局唯一）
    private final Long id;
    private final ProductId productId; // 商品ID（值对象）
    private final Money unitPrice;     // 单价
    private final int quantity;        // 数量

    public OrderItem(Long id, ProductId productId, Money unitPrice, int quantity) {
        validate(quantity);
        this.id = id;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // 校验数量合法性
    private void validate(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainException("订单项数量必须大于0：" + quantity);
        }
    }

    // 计算订单项总价（单价×数量）
    public Money calculateTotal() {
        BigDecimal totalAmount = unitPrice.getAmount().multiply(BigDecimal.valueOf(quantity));
        return Money.of(totalAmount, unitPrice.getCurrency());
    }

    // getter
    public Long getId() { return id; }
    public ProductId getProductId() { return productId; }
    public Money getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
```

###### （2）订单（聚合根，维护聚合一致性）  

```java
// 订单（聚合根，包含多个OrderItem子实体）
public class Order {
    private final OrderId id;                 // 聚合根唯一标识
    private final UserId userId;              // 所属用户ID
    private final List<OrderItem> items;      // 订单项（子实体）
    private Money totalAmount;                // 订单总金额（聚合内不变量）
    private OrderStatus status;               // 订单状态
    private final LocalDateTime createTime;   // 创建时间
    private LocalDateTime payTime;            // 支付时间
    private final String shippingAddress;     // 收货地址

    // 构造函数：创建订单（初始化状态和总金额）
    public Order(OrderId id, UserId userId, List<OrderItem> items, String shippingAddress) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items); // 防御性拷贝
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING_PAYMENT; // 初始状态：待支付
        this.createTime = LocalDateTime.now();
        this.totalAmount = calculateTotalAmount(); // 计算总金额（聚合不变量）
    }

    // 业务行为：支付订单（状态流转+记录支付时间）
    public void pay() {
        // 校验状态（业务规则：只有待支付订单可支付）
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(
                "当前状态不可支付：" + this.status.getDesc()
            );
        }
        // 状态变更
        this.status = OrderStatus.PAID;
        this.payTime = LocalDateTime.now();
        // 发布领域事件（支付成功后通知其他模块）
        DomainEventPublisher.publish(new OrderPaidEvent(this.id, this.userId, this.totalAmount));
    }

    // 业务行为：取消订单（仅限待支付状态）
    public void cancel() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(
                "当前状态不可取消：" + this.status.getDesc()
            );
        }
        this.status = OrderStatus.CANCELLED;
        // 发布取消事件
        DomainEventPublisher.publish(new OrderCancelledEvent(this.id));
    }

    // 计算订单总金额（聚合不变量：总金额=所有订单项金额之和）
    private Money calculateTotalAmount() {
        if (items.isEmpty()) {
            throw new InvalidDomainException("订单不能包含空订单项");
        }
        // 以第一个订单项的币种为基准
        Money total = Money.of(BigDecimal.ZERO, items.get(0).getUnitPrice().getCurrency());
        for (OrderItem item : items) {
            total = total.add(item.calculateTotal());
        }
        return total;
    }

    // getter（无setter，状态通过行为变更）
    public OrderId getId() { return id; }
    public UserId getUserId() { return userId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); } // 禁止外部修改
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
}
```

##### 3. 领域服务（Domain Service）  

封装跨实体/聚合的业务逻辑（当逻辑无法归属到单一实体时）。  

```java
// 订单领域服务（处理跨聚合的业务逻辑）
public class OrderDomainService {
    // 依赖其他聚合的仓储接口（通过接口依赖，解耦具体实现）
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    // 构造函数注入依赖
    public OrderDomainService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * 创建订单（跨聚合逻辑：检查库存→扣减库存→创建订单）
     */
    public Order createOrder(List<ProductId> productIds, UserId userId, String shippingAddress) {
        // 1. 校验商品是否存在且库存充足（跨聚合：查询商品和库存）
        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductId productId : productIds) {
            // 检查商品是否存在
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new InvalidDomainException("商品不存在：" + productId.getValue()));
            // 检查库存
            Inventory inventory = inventoryRepository.findByProductId(productId);
            if (inventory == null || inventory.getQuantity() < 1) {
                throw new InsufficientInventoryException("商品库存不足：" + productId.getValue());
            }
            // 扣减库存（跨聚合操作）
            inventoryRepository.decreaseStock(productId, 1);
            // 创建订单项
            orderItems.add(new OrderItem(
                null, // ID由仓储生成
                productId,
                product.getPrice(), // 商品单价
                1 // 数量（简化示例，固定为1）
            ));
        }

        // 2. 创建订单（聚合内逻辑）
        OrderId orderId = OrderId.generate();
        return new Order(orderId, userId, orderItems, shippingAddress);
    }
}
```


##### 4. 领域事件与仓储接口  

###### （1）领域事件（Domain Event）  

```java
// 订单支付成功事件
public class OrderPaidEvent implements DomainEvent {
    private final OrderId orderId;
    private final UserId userId;
    private final Money amount;
    private final LocalDateTime occurredAt;

    public OrderPaidEvent(OrderId orderId, UserId userId, Money amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.occurredAt = LocalDateTime.now();
    }

    // getter
    public OrderId getOrderId() { return orderId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}

// 事件发布器（简单实现）
public class DomainEventPublisher {
    private static final List<DomainEventSubscriber> subscribers = new ArrayList<>();

    public static void publish(DomainEvent event) {
        for (DomainEventSubscriber subscriber : subscribers) {
            subscriber.onEvent(event);
        }
    }
}
```

###### （2）仓储接口（Repository Interface）  

```java
// 订单仓储接口（定义持久化契约）
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    void delete(OrderId id);
}

// 库存仓储接口（供领域服务依赖）
public interface InventoryRepository {
    Inventory findByProductId(ProductId productId);
    void decreaseStock(ProductId productId, int quantity);
}
```


#### 四、单元测试  

领域层单元测试聚焦**业务规则的正确性**，验证领域对象的行为和领域服务的逻辑。  

##### 1. 测试要求  

- **聚焦业务逻辑**：测试`Order.pay()`、`Money.of()`等核心行为，而非getter/setter；  
- **完全隔离**：不依赖数据库、框架，通过Mock仓储接口（如`InventoryRepository`）；  
- **覆盖全场景**：正常流程（支付成功）、边界条件（金额为0）、异常场景（重复支付）；  
- **命名清晰**：如`testOrderPay_WhenStatusIsPending_ThenStatusChangesToPaid()`；  
- **断言精准**：验证领域对象的状态变化（如订单状态是否更新），而非中间过程。  

##### 2. 测试示例（JUnit 5 + Mockito）  

###### （1）实体测试：订单支付功能  

```java
class OrderTest {

    // 测试1：正常场景：待支付订单支付后状态变为“已支付”
    @Test
    void testPay_WhenStatusIsPending_ThenStatusChangesToPaid() {
        // 准备：创建待支付订单
        OrderId orderId = OrderId.generate();
        Money amount = Money.of(new BigDecimal("100"), "CNY");
        List<OrderItem> items = List.of(
            new OrderItem(1L, new ProductId("PROD_1"), amount, 1)
        );
        Order order = new Order(orderId, new UserId("USER_1"), items, "北京市");

        // 执行：调用支付方法
        order.pay();

        // 验证：状态是否正确更新
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPayTime()).isNotNull();
    }

    // 测试2：异常场景：已支付订单再次支付应抛出异常
    @Test
    void testPay_WhenStatusIsAlreadyPaid_ThenThrowException() {
        // 准备：创建已支付订单
        OrderId orderId = OrderId.generate();
        Money amount = Money.of(new BigDecimal("100"), "CNY");
        List<OrderItem> items = List.of(
            new OrderItem(1L, new ProductId("PROD_1"), amount, 1)
        );
        Order order = new Order(orderId, new UserId("USER_1"), items, "北京市");
        order.pay(); // 先支付，使状态变为已支付

        // 执行并验证：再次支付是否抛出异常
        assertThatThrownBy(order::pay)
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("当前状态不可支付：已支付");
    }
}
```

##### （2）值对象测试：金额校验  

```java
class MoneyTest {

    // 测试1：创建合法金额（正数）
    @Test
    void testOf_WithPositiveAmount_ThenCreateSuccess() {
        Money money = Money.of(new BigDecimal("50.5"), "CNY");
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("50.5"));
        assertThat(money.getCurrency()).isEqualTo("CNY");
    }

    // 测试2：创建非法金额（负数）应抛出异常
    @Test
    void testOf_WithNegativeAmount_ThenThrowException() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-10"), "CNY"))
            .isInstanceOf(InvalidDomainException.class)
            .hasMessageContaining("金额不能为负数");
    }

    // 测试3：币种不匹配时加法应抛出异常
    @Test
    void testAdd_WithDifferentCurrency_ThenThrowException() {
        Money cnyMoney = Money.of(new BigDecimal("100"), "CNY");
        Money usdMoney = Money.of(new BigDecimal("100"), "USD");

        assertThatThrownBy(() -> cnyMoney.add(usdMoney))
            .isInstanceOf(InvalidDomainException.class)
            .hasMessageContaining("币种不匹配");
    }
}
```

##### （3）领域服务测试：创建订单时检查库存  

```java
@ExtendWith(MockitoExtension.class)
class OrderDomainServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderDomainService orderDomainService;

    // 测试1：库存充足时，创建订单成功
    @Test
    void testCreateOrder_WhenInventorySufficient_ThenOrderCreated() {
        // 准备：模拟数据
        ProductId productId = new ProductId("PROD_1");
        UserId userId = new UserId("USER_1");
        // 模拟商品存在
        Product product = new Product(productId, "测试商品", Money.of(new BigDecimal("50"), "CNY"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        // 模拟库存充足
        when(inventoryRepository.findByProductId(productId))
            .thenReturn(new Inventory(productId, 10));

        // 执行：调用领域服务
        Order order = orderDomainService.createOrder(List.of(productId), userId, "北京市");

        // 验证：订单创建成功，库存被扣减
        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(1);
        verify(inventoryRepository).decreaseStock(productId, 1); // 验证库存扣减
        verify(productRepository).findById(productId); // 验证商品查询
    }

    // 测试2：库存不足时，创建订单失败
    @Test
    void testCreateOrder_WhenInventoryInsufficient_ThenThrowException() {
        // 准备：模拟库存不足
        ProductId productId = new ProductId("PROD_1");
        when(productRepository.findById(productId))
            .thenReturn(Optional.of(new Product(productId, "测试商品", Money.of(new BigDecimal("50"), "CNY"))));
        when(inventoryRepository.findByProductId(productId))
            .thenReturn(new Inventory(productId, 0)); // 库存为0

        // 执行并验证：抛出库存不足异常
        assertThatThrownBy(() -> orderDomainService.createOrder(
                List.of(productId), new UserId("USER_1"), "北京市"))
            .isInstanceOf(InsufficientInventoryException.class)
            .hasMessageContaining("商品库存不足");

        // 验证：库存扣减和订单创建未执行
        verify(inventoryRepository, never()).decreaseStock(any(), anyInt());
    }
}
```

#### 五、注意事项  

1. **避免“贫血模型”**  
   禁止将领域模型设计为仅含getter/setter的数据容器，必须将业务行为（如`pay()`、`cancel()`）封装在模型中，确保业务规则内聚。  

2. **领域事件的轻量化**  
   领域事件仅用于传递状态变化（如“订单已支付”），不应包含复杂业务逻辑。事件处理逻辑应放在应用层或领域服务中，避免事件与业务逻辑耦合。  

3. **与应用层的边界清晰**  
   - 领域层：回答“**做什么是对的**”（如“订单未支付时可取消”）；  
   - 应用层：回答“**如何做**”（如“调用`order.cancel()`并记录日志”）。  
   禁止在领域层处理事务、权限等技术细节，这些应放在应用层。  

4. **测试聚焦业务规则**  
   领域层测试的核心是验证业务逻辑的正确性（如“状态流转是否符合规则”），而非技术实现（如“是否调用数据库”）。  

5. **模型的可演进性**  
   领域模型应随业务发展逐步优化，避免初期过度设计。例如，可先通过简单实体实现核心逻辑，待业务复杂度提升后再引入聚合、事件等概念。  
