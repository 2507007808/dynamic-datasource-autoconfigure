package club.niblet.www.mapper;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 动态SqlSessionFactory上下文管理
 * @author niblet
 */
public class DynamicSqlSessionFactoryContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSqlSessionFactoryContextHolder.class);

    @Getter
    @Setter
    private static SqlSessionFactory defaultSqlSessionFactory;

    @Getter
    @Setter
    private static BeanDefinitionRegistry registry;

    private static final Map<String, SqlSessionFactory> SQL_SESSION_FACTORY_POOLS = new ConcurrentHashMap<>();

    /**
     * add SqlSessionFactory to
     * @param key
     * @param sqlSessionFactory
     */
    public static void addSqlSessionFactory(String key, SqlSessionFactory sqlSessionFactory) {
        LOGGER.debug("add key : {} , sqlSessionFactory : {} to SqlSessionFactoryPools.", key, sqlSessionFactory);
        SQL_SESSION_FACTORY_POOLS.put(key, sqlSessionFactory);
    }

    /**
     * get sqlSessionFactory from SqlSessionFactoryPools by key
     * @return key
     */
    public static SqlSessionFactory getSqlSessionFactory(String key) {
        if(SQL_SESSION_FACTORY_POOLS.containsKey(key)) {
            return SQL_SESSION_FACTORY_POOLS.get(key);
        } else {
            return defaultSqlSessionFactory;
        }
    }

    /**
     * remove SqlSessionFactory from SqlSessionFactoryPools by key
     * @param key
     */
    public static void removeSqlSessionFactory(String key) {
        LOGGER.debug("remove SqlSessionFactory by key : {} ", key);
        SQL_SESSION_FACTORY_POOLS.remove(key);
    }

}
