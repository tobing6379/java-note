package top.tobing.pattern.build.simpl_factory;

import java.util.Map;

/**
 * @author tobing
 * @date 2021/10/10 0:29
 * @description 发奖接口
 */
public interface ICommodity {
    /**
     * 发放奖励
     */
    void sendCommodity(String uid,
                       String commodityId,
                       String bizId,
                       Map<String, String> extMap
    ) throws Exception;
}
