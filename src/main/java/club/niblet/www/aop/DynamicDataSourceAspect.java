package club.niblet.www.aop;

import club.niblet.www.annotation.DataSource;
import club.niblet.www.configuration.DynamicDataSourceRegister;
import club.niblet.www.datasource.DynamicDataSourceContextHolder;
import club.niblet.www.mapper.DynamicSqlSessionFactoryContextHolder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static club.niblet.www.util.DataSourceConstant.*;


/**
 * 动态数据源切入配置
 * @author niblet
 */
@Aspect
@Order(-1)
@Component
public class DynamicDataSourceAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    @Before("@annotation(ds)")
    public void changeDataSource(JoinPoint point, DataSource ds) {
        String key = ds.value();
        if (!DynamicDataSourceContextHolder.containsDataSource(key)) {
            LOGGER.error("datasource {} is not existed, use default datasource > {}", key, point.getSignature());
        } else {
            LOGGER.debug("use datasource {} > {}", key, point.getSignature());
            DynamicDataSourceContextHolder.changeDataSource(key);
        }
        //TODO 替换SqlSessionFactory的数据源为key对应数据源
        DefaultSqlSessionFactory target = (DefaultSqlSessionFactory) ((DefaultListableBeanFactory) DynamicSqlSessionFactoryContextHolder.getRegistry()).getBean(DEFAULT_SQL_SESSION_FACTORY);
        //TODO 默认列为驼峰命名
        target.getConfiguration().setMapUnderscoreToCamelCase(true);
        Environment env = target.getConfiguration().getEnvironment();
        Environment replaceEnv = new Environment(env.getId(), env.getTransactionFactory(), DynamicDataSourceRegister.dynamicDataSources.get(key));
        if (!Objects.isNull(target)) {
            target.getConfiguration().setEnvironment(replaceEnv);
        }
        //TODO 替换DataSourceTransactionManager的数据源为key对应数据源
        DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) ((DefaultListableBeanFactory) DynamicSqlSessionFactoryContextHolder.getRegistry()).getBean(DEFAULT_TRANSACTION_MANAGER_NAME);
        transactionManager.setDataSource(DynamicDataSourceRegister.dynamicDataSources.get(key));
    }

    @After("@annotation(ds)")
    public void releaseDataSource(JoinPoint point, DataSource ds) {
        LOGGER.debug("datasource {} is released --> {}", ds.value(), point.getSignature());
        DynamicDataSourceContextHolder.clearDataSource();
        //TODO 还原SqlSessionFactory的数据源为default
        DefaultSqlSessionFactory target = (DefaultSqlSessionFactory) ((DefaultListableBeanFactory) DynamicSqlSessionFactoryContextHolder.getRegistry()).getBean(DEFAULT_SQL_SESSION_FACTORY);
        Environment env = target.getConfiguration().getEnvironment();
        Environment replaceEnv = new Environment(env.getId(), env.getTransactionFactory(), DynamicDataSourceRegister.dynamicDataSources.get(DEFAULT));
        if (!Objects.isNull(target)) {
            target.getConfiguration().setEnvironment(replaceEnv);
        }
        //TODO 还原DataSourceTransactionManager的数据源为key对应数据源
        DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) ((DefaultListableBeanFactory) DynamicSqlSessionFactoryContextHolder.getRegistry()).getBean(DEFAULT_TRANSACTION_MANAGER_NAME);
        transactionManager.setDataSource(DynamicDataSourceRegister.dynamicDataSources.get(DEFAULT));
    }

}
