package top.tobing.pattern.build.abstract_factory.actually;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 11:22
 * @description 集群服务操作接口
 */
public class IIR {

    private Map<String, String> dataMap =
            new ConcurrentHashMap<>();

    public String get(String key) {
        System.out.println("集群服务：IIR::get()");
        return dataMap.get(key);
    }

    public void set(String key, String value) {
        System.out.println("集群服务：IIR::set()");
        dataMap.put(key, value);
    }

    public void setExpire(String key, String value, long timeout, TimeUnit timeUnit) {
        System.out.println("集群服务：IIR::setExpire()");

        dataMap.put(key, value);
    }

    public void del(String key) {
        System.out.println("集群服务：IIR::del()");
        dataMap.remove(key);
    }
}
