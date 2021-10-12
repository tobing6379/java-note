package top.tobing.pattern.build.abstract_factory.impl;

import top.tobing.pattern.build.abstract_factory.ICacheAdapter;
import top.tobing.pattern.build.abstract_factory.actually.IIR;

import java.util.concurrent.TimeUnit;

/**
 * @author tobing
 * @date 2021/10/10 11:36
 * @description
 */
public class IIRCacheAdapter implements ICacheAdapter {

    IIR iir = new IIR();

    @Override
    public String get(String key) {
        return iir.get(key);
    }

    @Override
    public void set(String key, String value) {
        iir.set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        iir.setExpire(key, value, timeout, timeUnit);
    }

    @Override
    public void del(String key) {
        iir.del(key);
    }
}
