# 动态数据源（多数据源的自动化配置）实现原理

### 1、定义注解类@DataSource,其值value默认为default,即为默认数据源

### 2、DynamicDataSource继承AbstractRoutingDataSource达到动态路由数据源

### 3、DynamicDataSourceRegister实现接口ImportBeanDefinitionRegistrar和EnvironmentAware,将动态数据源、SqlSessionFactory及TransactionManager注册到Ioc容器

### 4、DynamicDataSourceAspect使用Aspect切入Mapper方法执行,在执行不同Mapper方法时动态切换数据源
