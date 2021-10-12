package top.tobing.pattern.build.simpl_factory;

/**
 * @author tobing
 * @date 2021/10/10 0:24
 * @description 需求：
 * 【模拟发奖多种商品】
 * 在营销场景经常会有某些用户做一些操作：打卡、分享、留言、邀请注册等等，进行返利积分，最后通过积分再兑现商品，从而促活和拉新。</br>
 * 在对于不同的奖励，提供了奖励工厂来进行按类型统一发放。
 */
public class Demo {
    public static void main(String[] args) throws Exception {
        StoreFactory storeFactory = new StoreFactory();
        ICommodity commodity1 = storeFactory.getCommodity(1);
        ICommodity commodity2 = storeFactory.getCommodity(2);
        ICommodity commodity3 = storeFactory.getCommodity(3);
        commodity1.sendCommodity(null, null, null, null);
        commodity2.sendCommodity(null, null, null, null);
        commodity3.sendCommodity(null, null, null, null);
    }
}
