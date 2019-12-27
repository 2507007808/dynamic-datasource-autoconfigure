package club.niblet.www.configuration;

import club.niblet.www.datasource.DynamicDataSource;
import club.niblet.www.datasource.DynamicDataSourceContextHolder;
import club.niblet.www.mapper.DynamicSqlSessionFactoryContextHolder;
import club.niblet.www.util.DataSourceConstant;
import com.google.common.base.Throwables;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static club.niblet.www.util.DataSourceConstant.*;

/**
 * 功能描述 : 动态数据源注册
 * @author niblet
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@MapperScan(MAPPER_PACKAGE_NAME)
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 默认数据源(主数据源)
     */
    private DataSource defaultDataSource;

    /**
     * 动态数据源
     */
    public static Map<String, DataSource> dynamicDataSources = new ConcurrentHashMap<>();

    /**
     * 加载多数据源配置
     *
     * @param env 配置信息
     */
    @Override
    public void setEnvironment(Environment env) {
        initDynamicDataSource(env);
    }


    /**
     * 初始化动态数据源
     *
     * @param env 配置信息
     */
    private void initDynamicDataSource(Environment env) {
        Map<String, DataSourceProperties> dynamicDataSourceProperties = Binder
                .get(env)
                .bind(DataSourceConstant.MULTI_DATA_SOURCE_PREFIX, Bindable.mapOf(String.class, DataSourceProperties.class))
                .get();
        DataSource dataSource = null;
        Map<String, Object> dsm = new HashMap<>(4);
        DataSourceProperties dataSourceProperties;
        for (String key : dynamicDataSourceProperties.keySet()) {
            dsm.clear();
            dataSourceProperties = Binder.get(env).bind(DataSourceConstant.MULTI_DATA_SOURCE_PREFIX.concat(".").concat(key), DataSourceProperties.class).get();
            dsm.put(TYPE, dataSourceProperties.getType());
            dsm.put(DRIVER_CLASS_NAME, dataSourceProperties.getDriverClassName());
            dsm.put(URL, dataSourceProperties.getUrl());
            dsm.put(USER_NAME, dataSourceProperties.getUsername());
            dsm.put(PASSWORD, dataSourceProperties.getPassword());
            dataSource = buildDataSource(dsm);
            if (DEFAULT.equalsIgnoreCase(key)) {
                defaultDataSource = dataSource;
            }
            dynamicDataSources.put(key, dataSource);
            this.bindDataSource(key, dataSource, dsm);
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<Object, Object> targetDataSources = new HashMap<>(4);
        //TODO 将默认数据源添加到动态数据源池
        targetDataSources.put(DATA_SOURCE_PROPERTY, defaultDataSource);
        //TODO 注册默认的SqlSessionFactoryBean
        SqlSessionFactory defaultSqlSessionFactory = registerSqlSessionFactoryBean(defaultDataSource);
        if (DynamicSqlSessionFactoryContextHolder.getRegistry() == null) {
            DynamicSqlSessionFactoryContextHolder.setRegistry(registry);
        }
        DynamicSqlSessionFactoryContextHolder.setDefaultSqlSessionFactory(defaultSqlSessionFactory);
        //TODO 添加其它数据源到动态数据源池
        targetDataSources.putAll(dynamicDataSources);
        for (String key : dynamicDataSources.keySet()) {
            DynamicDataSourceContextHolder.getDataSourcePools().add(key);
            //TODO 注册动态的的SqlSessionFactoryBean
            SqlSessionFactory sqlSessionFactory = registerSqlSessionFactoryBean(dynamicDataSources.get(key));
            DynamicSqlSessionFactoryContextHolder.addSqlSessionFactory(key, sqlSessionFactory);
        }
        //TODO 创建DynamicDataSource
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicDataSource.class);
        beanDefinition.setSynthetic(true);
        MutablePropertyValues mpv = beanDefinition.getPropertyValues();
        mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
        mpv.addPropertyValue("targetDataSources", targetDataSources);
        registry.registerBeanDefinition(DATA_SOURCE_PROPERTY, beanDefinition);
        logger.debug("dynamic datasource is registered, these are {} number datasource.", targetDataSources.size());
    }


    /**
     * 创建数据源实例
     *
     * @param dsm 配置参数
     * @return 实例化的数据源
     */
    public DataSource buildDataSource(Map<String, Object> dsm) {
        try {
            Class dataSourceTypeClass = (Class) dsm.get(TYPE);
            if (dataSourceTypeClass == null) {
                dataSourceTypeClass = DEFAULT_DATASOURCE_CLASS;
            }
            Class<? extends DataSource> dataSourceType;
            dataSourceType = (Class<? extends DataSource>) Class.forName(dataSourceTypeClass.getName());
            String driverClassName = (String) dsm.get(DRIVER_CLASS_NAME);
            String url = (String) dsm.get(URL);
            String username = (String) dsm.get(USER_NAME);
            String password = (String) dsm.get(PASSWORD);
            DataSourceBuilder builder = DataSourceBuilder
                    .create()
                    .driverClassName(driverClassName)
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(dataSourceType);
            return builder.build();
        } catch (ClassNotFoundException e) {
            logger.error("datasource type instantiate error, cause is :{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 绑定数据源
     *
     * @param key        数据源key
     * @param dataSource 数据源实例
     * @param dsm        数据源配置属性
     */
    public void bindDataSource(String key, DataSource dataSource, Map<String, Object> dsm) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(dsm);
        Binder binder = new Binder(new ConfigurationPropertySource[]{source});
        if (StringUtils.isEmpty(key) || DEFAULT.equalsIgnoreCase(key)) {
            binder.bind(ConfigurationPropertyName.of(DEFAULT), Bindable.ofInstance(dataSource));
        } else {
            binder.bind(ConfigurationPropertyName.of(key), Bindable.ofInstance(dataSource));
        }
    }

    /**
     * 注册SqlSessionFactoryBean
     *
     * @param dataSource 数据源实例
     */
    private SqlSessionFactory registerSqlSessionFactoryBean(DataSource dataSource) {
        try {
            final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(dataSource);
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources(MAPPER_XML_PATH));
            return sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            logger.error("init SqlSessionFactory failure，cause :{}", Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    @Primary
    @Bean
    public SqlSessionFactory sqlSessionFactory() {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(defaultDataSource);
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            sessionFactoryBean.setMapperLocations(resolver.getResources(MAPPER_XML_PATH));
            return sessionFactoryBean.getObject();
        } catch (Exception e) {
            logger.error("init SqlSessionFactory failure，cause :{}", Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(defaultDataSource);
    }

}
