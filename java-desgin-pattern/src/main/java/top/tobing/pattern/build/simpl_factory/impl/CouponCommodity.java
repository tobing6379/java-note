package top.tobing.pattern.build.simpl_factory.impl;

import top.tobing.pattern.build.simpl_factory.ICommodity;

import java.util.Map;

/**
 * @author tobing
 * @date 2021/10/10 0:30
 * @description 发放优惠卷
 */
public class CouponCommodity implements ICommodity {
    @Override
    public void sendCommodity(String uid, String commodityId, String bizId, Map<String, String> extMap) throws Exception {
        System.out.println("CouponCommodity::发放优惠卷");
    }
}
