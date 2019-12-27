package club.niblet.www.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


/**
 * 动态数据源
 * @author niblet
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DynamicDataSource.class);

    /**
     * 指定实际的数据源
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = DynamicDataSourceContextHolder.getDataSource();
        LOGGER.info("current data source is {}", key);
        return key;
    }
}
