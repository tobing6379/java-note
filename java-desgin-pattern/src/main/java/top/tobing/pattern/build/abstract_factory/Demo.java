package top.tobing.pattern.build.abstract_factory;

import top.tobing.pattern.build.abstract_factory.impl.EGMCacheAdapter;
import top.tobing.pattern.build.abstract_factory.impl.IIRCacheAdapter;

/**
 * @author tobing
 * @date 2021/10/10 11:40
 * @description 测试
 */
public class Demo {
    public static void main(String[] args) {
        CacheService proxyEGM = JDKProxy.getProxy(CacheService.class, new EGMCacheAdapter());
        CacheService proxyIIR = JDKProxy.getProxy(CacheService.class, new IIRCacheAdapter());
    }
}
