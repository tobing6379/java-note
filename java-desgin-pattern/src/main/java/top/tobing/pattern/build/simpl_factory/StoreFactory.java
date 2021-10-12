package top.tobing.pattern.build.simpl_factory;

import top.tobing.pattern.build.simpl_factory.impl.CardCommodity;
import top.tobing.pattern.build.simpl_factory.impl.CouponCommodity;
import top.tobing.pattern.build.simpl_factory.impl.GoodsCommodity;

/**
 * @author tobing
 * @date 2021/10/10 0:36
 * @description 发放奖品工厂
 */
public class StoreFactory {

    public ICommodity getCommodity(int type) {
        if (1 == type) {
            return new CardCommodity();
        } else if (2 == type) {
            return new CouponCommodity();
        } else if (3 == type) {
            return new GoodsCommodity();
        }

        throw new IllegalArgumentException("不存在这种奖品类型！" + type);
    }
}
