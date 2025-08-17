### 应用架构设计

项目工程秉承 DDD（领域驱动设计）思想，使用 Maven Moudles 方式构建，采用分层架构设计，将项目工程内部拆分成 4 层结构。

#### 一、parent 说明

虽然项目工程使用 Maven Moudles 方式构建，但最外层的 pom.xml 仅作为管理 modules 实现模块聚合，内部各层仍采用统一封装的 parent 依赖包。如此设计的主要思考如下：

1. 为确保项目工程依赖包版本的统一性，需引入统一的 parent 包进行依赖管理。
2. 项目工程最外层的 pom.xml 仅承担 modules 的聚合管理，不依赖 parent ，内部模块自行依赖 parent，这样可减少包依赖层级的复杂度。


#### 二、DDD 四层结构

##### 1. 领域层（domain）

- 核心职责：根据业务抽象出领域模型（充血模型）、领域服务接口、领域事件、值对象等，对业务进行具象化（实现业务规则和逻辑）。
- 技术实现：纯 Java 实现，不依赖任何 Spring 组件。
- 核心内容
    - 充血模型的领域对象（包含业务规则）
    - 领域服务接口（领域内跨实体的业务逻辑）
    - 仓储接口（Repository 接口，定义数据操作契约）
    - 值对象、枚举、领域事件等

##### 2. 应用层（application）

- 核心职责：通过构建应用服务接口、命令 / 查询对象、 DTO 等对领域模型进行逻辑编排，实现业务交互。
- 技术实现：纯 Java 接口，定义用例流程，不依赖任何 Spring 组件。
- 核心内容：
    - 应用服务接口（编排领域服务，实现用例）
    - 命令 / 查询对象（封装请求参数，避免贫血模型）
    - DTO（数据传输对象，定义接口层与应用层的数据格式）

##### 3. 接口层（interfaces）

- 核心职责：定义对外暴露访问的 API 接口，保障服务可访问性、可协同工作。
- 技术实现：仅定义接口和转换规则，依赖 Spring 注解，如`@GetMapping`、`@PostMapping`、`@FeignClient`等。
- 核心内容
    - Controller 接口（定义 HTTP 接口）
    - Feign 客户端接口（定义服务间调用契约）
    - DTO 转换器（领域对象 ↔ DTO 的转换规则）

##### 4. 基础设施层（infrastructure）

- 核心职责：通过数据库、缓存、消息队列、Spring 配置等进行技术实现。
- 技术实现：实现所有技术细节，依赖 Spring Boot 及其他框架。
- 核心内容：
    - Controller 实现（HTTP 接口的具体逻辑）
    - 仓储实现（数据库操作，如 JPA/MyBatis 实现）
    - 应用服务实现（编排领域服务，处理事务）
    - Spring 配置（Bean 注册、事务管理等）

#### 三、模块依赖关系

1. 严格遵循**单向依赖**原则，避免循环依赖。

```plaintext

infrastructure → interfaces → application → domain

# 基础设施层依赖接口层
# 接口层依赖应用层
# 应用层依赖领域层

```

2. 父模块 pom.xml 中定义依赖顺序

```xml
<modules>
    <module>domain</module>
    <module>application</module>
    <module>interfaces</module>
    <module>infrastructure</module>
</modules>
```

#### 四、核心优势

1. 业务与技术解耦：领域层和应用层纯 Java 实现，可独立测试，不受框架变更影响。
2. 可替换性：基础设施层的技术实现（如从 JPA 换为 MyBatis）不影响上层业务。
3. 团队协作：领域专家可专注于 domain 模块，开发人员专注于 infrastructure 模块。
4. 可测试性：领域层可通过单元测试验证业务规则，无需启动 Spring 容器。

#### 五、注意事项

1. 接口层的 FeignClient 接口需在基础设施层通过`@EnableFeignClients`扫描注册
2. 仓储接口的实现类需用`@Repository`注解，确保 Spring 自动扫描
3. 事务注解`@Transactional`仅在基础设施层的服务实现类上使用
4. 避免在领域层引入任何框架注解（如`@Entity`应放在基础设施层的数据库实体上）

