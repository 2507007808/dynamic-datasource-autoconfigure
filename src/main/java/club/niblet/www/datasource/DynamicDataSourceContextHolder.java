package club.niblet.www.datasource;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态数据源上下文管理
 * @author niblet
 */
public final class DynamicDataSourceContextHolder {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    private static final ThreadLocal<String> DATA_SOURCE_KEY = new ThreadLocal<>();

    @Getter
    @Setter
    private static List<String> dataSourcePools = new ArrayList<>();

    /**
     * 设置数据源名
     * @param key 数据源名
     */
    public static void changeDataSource(final String key) {
        LOGGER.debug("change to data source is :{}", key);
        DATA_SOURCE_KEY.set(key);
    }

    /**
     * 获取数据源名
     * @return 数据源名
     */
    public static String getDataSource() {
        return DATA_SOURCE_KEY.get();
    }

    /**
     * 清除数据源名
     */
    public static void clearDataSource() {
        LOGGER.debug("clear current data source!");
        DATA_SOURCE_KEY.remove();
    }

    /**
     * 判断数据源池中是否存在key对应数据源
     * @param key 数据源名
     * @return 是否存在
     */
    public static boolean containsDataSource(final String key) {
        return dataSourcePools.contains(key);
    }

}
