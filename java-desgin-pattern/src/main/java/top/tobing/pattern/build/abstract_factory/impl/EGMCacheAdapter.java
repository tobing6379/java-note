package top.tobing.pattern.build.abstract_factory.impl;

import top.tobing.pattern.build.abstract_factory.ICacheAdapter;
import top.tobing.pattern.build.abstract_factory.actually.EGM;

import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 11:14
 * @description 集群实现
 */
public class EGMCacheAdapter implements ICacheAdapter {

    private EGM egm = new EGM();

    @Override
    public String get(String key) {
        return egm.gain(key);
    }

    @Override
    public void set(String key, String value) {
        egm.set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        egm.setEx(key, value, timeout, timeUnit);
    }

    @Override
    public void del(String key) {
        egm.delete(key);
    }
}
