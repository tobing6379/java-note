package top.tobing.dao;

import org.apache.ibatis.annotations.Param;
import top.tobing.dto.UserDTO;

/**
 * @author tobing
 * @date 2021/10/3 23:32
 * @description
 */
public interface UserDao {

    /**
     * 根据id查询用户
     */
    UserDTO findOneById(@Param("id") Integer id);
}
