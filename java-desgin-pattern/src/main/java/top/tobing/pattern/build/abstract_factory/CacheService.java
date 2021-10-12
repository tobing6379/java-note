package top.tobing.pattern.build.abstract_factory;

import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 0:46
 * @description 缓存操作接口
 */
public interface CacheService {
    String get(final String key);

    void set(String key, String value);

    void set(String key, String value, long timeout, TimeUnit timeUnit);

    void del(String key);
}
