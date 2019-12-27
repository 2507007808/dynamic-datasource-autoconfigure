package club.niblet.www.util;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

/**
 * 数据源常量值
 * @author niblet
 */
public class DataSourceConstant {

    public static final String MAPPER_PACKAGE_NAME = "club.niblet.www.mapper";

    public static final String DATASOURCE_POINTCUT_EXPRESSION = "execution(* club.niblet.www.mapper.*.*(..)";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String URL = "url";

    public static final String DRIVER_CLASS_NAME = "driver-class-name";

    public static final String USER_NAME = "username";

    public static final String PASSWORD = "password";

    /**
     * 默认数据源Class为DruidDataSource.class
     */
    public static final Class<? extends DataSource> DEFAULT_DATASOURCE_CLASS = DruidDataSource.class;

    /**
     * 默认数据源名称
     */
    public static final String DEFAULT = "default";

    /**
     * 默认数据源的配置前缀
     */
    public static final String DEFAULT_DATA_SOURCE_PREFIX = "spring.datasource.dynamic.default";

    /**
     * 动态数据源的配置前缀
     */
    public static final String MULTI_DATA_SOURCE_PREFIX = "spring.datasource.dynamic";

    /**
     * 默认SqlSessionFactory
     */
    public static final String DEFAULT_SQL_SESSION_FACTORY = "sqlSessionFactory";

    /**
     * 动态SqlSessionFactory后缀
     */
    public static final String MULTI_SQL_SESSION_FACTORY_SUFFIX = "SqlSessionFactory";

    /**
     * datasoure属性名
     */
    public static final String DATA_SOURCE_PROPERTY = "dataSource";

    /**
     * mapperLocations属性名
     */
    public static final String MAPPER_LOCATIONS_PROPERTY = "mapperLocations";

    /**
     * XML文件路径
     */
    public static final String MAPPER_XML_PATH = "classpath:/mapper/*.xml";

    /**
     * 默认事务管理名
     */
    public static final String DEFAULT_TRANSACTION_MANAGER_NAME = "transactionManager";

}
