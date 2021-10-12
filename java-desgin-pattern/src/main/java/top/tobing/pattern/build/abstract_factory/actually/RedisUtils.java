package top.tobing.pattern.build.abstract_factory.actually;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 11:22
 * @description 单机服务
 * 模拟Redis功能，即指定目前所有系统都在使用该服务
 * 类和方法名固定写死在业务系统中，该动起来稍微有点困难
 */
public class RedisUtils {

    private Map<String, String> dataMap =
            new ConcurrentHashMap<>();

    public String get(String key) {
        System.out.println("单机服务：RedisUtils::get()");
        return dataMap.get(key);
    }

    public void set(String key, String value) {
        System.out.println("单机服务：RedisUtils::set()");
        dataMap.put(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        System.out.println("单机服务：RedisUtils::set()");
        dataMap.put(key, value);
    }

    public void del(String key) {
        System.out.println("单机服务：RedisUtils::del()");
        dataMap.remove(key);
    }
}
