### 应用层（Application）

#### 一、总体概述

应用层是系统的**流程编排中枢**，负责将用户需求转化为具体业务流程，协调领域层、基础设施层等组件完成操作。它不包含核心业务规则（由领域层承载），主要职责是：  

- 封装业务用例（如“创建订单”“取消支付”）；  
- 处理参数校验、事务管理、异常转换等横切关注点；  
- 通过DTO（数据传输对象）实现与外部接口的解耦。  

应用层是连接用户接口（如API层）与领域核心的桥梁，确保业务流程的完整性和一致性。

#### 二、基本原则

1. **流程编排而非业务实现**  
   应用层只负责“如何串联步骤”（如“先查用户→再调领域服务→最后保存结果”），不定义“业务规则”（如“订单金额必须大于0”由领域层实现）。

2. **依赖抽象接口**  
   依赖领域层的接口（如`OrderDomainService`）和仓储接口（如`OrderRepository`），而非具体实现，通过依赖注入降低耦合。

3. **事务边界明确**  
   一个用例通常对应一个事务（如“创建订单”需保证“订单保存”和“库存扣减”原子性），通过注解（如`@Transactional`）管理。

4. **输入输出标准化**  
   用DTO定义输入（`*Request`）和输出（`*Response`），避免外部接口直接依赖领域模型。


#### 三、示例代码

以电商“创建订单”用例为例，展示应用层核心组件及单元测试。


##### 1. DTO（数据传输对象）

```java
// 下单请求DTO
public record CreateOrderRequest(
    Long userId,
    List<OrderItemRequest> items,
    String shippingAddress
) {}

// 订单项请求DTO
public record OrderItemRequest(Long productId, Integer quantity) {}

// 下单响应DTO
public record OrderResponse(
    String orderId,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createTime
) {}
```


##### 2. 应用服务（核心用例实现）

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderApplicationService {
    // 依赖领域服务、仓储、外部服务
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final UserService userService;

    // 构造函数注入（依赖注入）
    public OrderApplicationService(OrderDomainService orderDomainService,
                                   OrderRepository orderRepository,
                                   UserService userService) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    /**
     * 创建订单用例
     */
    @Transactional // 事务管理：确保流程原子性
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. 校验输入参数
        validateRequest(request);

        // 2. 调用外部服务查询用户
        User user = userService.getUserById(request.userId());
        if (user == null) {
            throw new UserNotFoundException("用户不存在（ID：" + request.userId() + "）");
        }

        // 3. 转换DTO为领域对象ID
        List<ProductId> productIds = request.items().stream()
                .map(item -> new ProductId(item.productId()))
                .collect(Collectors.toList());

        // 4. 调用领域服务执行核心业务
        Order order = orderDomainService.createOrder(
                productIds,
                new UserId(request.userId()),
                request.shippingAddress()
        );

        // 5. 保存订单到仓储
        Order savedOrder = orderRepository.save(order);

        // 6. 转换领域对象为响应DTO
        return new OrderResponse(
                savedOrder.getId().getValue(),
                savedOrder.getTotalAmount().getAmount(),
                savedOrder.getStatus().name(),
                savedOrder.getCreateTime()
        );
    }

    /**
     * 校验请求参数合法性
     */
    private void validateRequest(CreateOrderRequest request) {
        if (request.userId() == null || request.userId() <= 0) {
            throw new InvalidRequestException("用户ID必须为正数");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidRequestException("订单至少包含一件商品");
        }
        request.items().forEach(item -> {
            if (item.productId() == null || item.productId() <= 0) {
                throw new InvalidRequestException("商品ID必须为正数");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new InvalidRequestException("商品数量必须为正数");
            }
        });
    }
}
```


##### 3. 异常类（应用层自定义异常）

```java
// 参数无效异常
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}

// 用户不存在异常
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```


#### 四、应用层单元测试

应用层单元测试聚焦**流程编排的正确性**，验证用例是否按预期调用依赖、处理异常、转换数据。

##### 1. 测试依赖

- JUnit 5（测试框架）；  
- Mockito（模拟依赖组件）；  
- AssertJ（增强断言库）。  

##### 2. 测试示例

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // 启用Mockito
class OrderApplicationServiceTest {

    // 模拟依赖组件
    @Mock
    private OrderDomainService orderDomainService; // 领域服务
    @Mock
    private OrderRepository orderRepository; // 仓储接口
    @Mock
    private UserService userService; // 外部用户服务

    // 注入被测试的应用服务
    @InjectMocks
    private OrderApplicationService orderApplicationService;

    /**
     * 测试正常流程：创建订单成功
     */
    @Test
    void createOrder_WithValidRequest_ReturnsOrderResponse() {
        // 1. 准备测试数据
        CreateOrderRequest request = new CreateOrderRequest(
                1L, // userId
                List.of(new OrderItemRequest(100L, 2)), // 商品ID:100，数量:2
                "北京市海淀区"
        );

        // 2. 模拟依赖行为
        User mockUser = new User(1L, "testUser");
        when(userService.getUserById(1L)).thenReturn(mockUser); // 模拟用户存在

        Order mockOrder = new Order(
                new OrderId("ORDER_123"),
                Money.of(new BigDecimal("200"), "CNY"),
                OrderStatus.PENDING_PAYMENT,
                LocalDateTime.now()
        );
        when(orderDomainService.createOrder(anyList(), any(), any())).thenReturn(mockOrder); // 模拟领域服务返回订单
        when(orderRepository.save(any())).thenReturn(mockOrder); // 模拟仓储保存成功

        // 3. 执行测试方法
        OrderResponse response = orderApplicationService.createOrder(request);

        // 4. 验证结果
        // 4.1 响应数据正确
        assertThat(response.orderId()).isEqualTo("ORDER_123");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(response.status()).isEqualTo("PENDING_PAYMENT");

        // 4.2 依赖被正确调用
        verify(userService).getUserById(1L); // 验证查询用户
        verify(orderDomainService).createOrder( // 验证调用领域服务
                List.of(new ProductId(100L)),
                new UserId(1L),
                "北京市海淀区"
        );
        verify(orderRepository).save(mockOrder); // 验证保存订单
    }

    /**
     * 测试异常流程：用户不存在
     */
    @Test
    void createOrder_WhenUserNotFound_ThrowsUserNotFoundException() {
        // 1. 准备测试数据
        CreateOrderRequest request = new CreateOrderRequest(
                999L, // 不存在的用户ID
                List.of(new OrderItemRequest(100L, 1)),
                "上海市"
        );

        // 2. 模拟依赖行为：用户服务返回null
        when(userService.getUserById(999L)).thenReturn(null);

        // 3. 执行并验证异常
        assertThatThrownBy(() -> orderApplicationService.createOrder(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("用户不存在（ID：999）");

        // 4. 验证后续依赖未被调用
        verify(orderDomainService, never()).createOrder(any(), any(), any());
        verify(orderRepository, never()).save(any());
    }

    /**
     * 测试异常流程：参数无效（商品数量为0）
     */
    @Test
    void createOrder_WithInvalidItemQuantity_ThrowsInvalidRequestException() {
        // 1. 准备无效请求（数量为0）
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
                1L,
                List.of(new OrderItemRequest(100L, 0)), // 数量为0
                "广州市"
        );

        // 2. 执行并验证异常
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("商品数量必须为正数");

        // 3. 验证依赖未被调用
        verify(userService, never()).getUserById(any());
        verify(orderDomainService, never()).createOrder(any(), any(), any());
    }
}
```

#### 五、注意事项

1. **Mock所有外部依赖**  
   应用层依赖领域服务、仓储、外部服务等，测试时需通过Mockito模拟这些依赖的返回值或异常，确保测试聚焦应用层自身逻辑。

2. **验证流程完整性**  
   不仅要验证最终结果，还要通过`verify()`确认依赖被按预期调用（如“先校验参数→再查用户→最后调用领域服务”）。

3. **覆盖异常分支**  
   重点测试参数校验失败、依赖抛出异常等场景，确保应用层能正确捕获并转换异常。

4. **避免测试领域逻辑**  
   应用层测试不验证“订单金额计算是否正确”（领域层职责），只验证“是否调用了领域服务计算金额”。

5. **与集成测试区分**  
   单元测试不涉及数据库、网络等真实资源，若需验证“订单是否正确存入数据库”，应放在基础设施层的集成测试中。
