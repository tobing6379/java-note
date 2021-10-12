package top.tobing.pattern.build.abstract_factory.actually;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 11:22
 * @description 集群服务操作接口
 */
public class EGM {

    private Map<String, String> dataMap =
            new ConcurrentHashMap<>();

    public String gain(String key) {
        System.out.println("集群服务：EGM::gain()");
        return dataMap.get(key);
    }

    public void set(String key, String value) {
        System.out.println("集群服务：EGM::set()");
        dataMap.put(key, value);
    }

    public void setEx(String key, String value, long timeout, TimeUnit timeUnit) {
        System.out.println("集群服务：EGM::setEx()");
        dataMap.put(key, value);
    }

    public void delete(String key) {
        System.out.println("集群服务：EGM::delete()");
        dataMap.remove(key);
    }
}
