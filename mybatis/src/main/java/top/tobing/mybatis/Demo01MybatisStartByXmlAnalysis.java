package top.tobing.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import top.tobing.dao.UserDao;
import top.tobing.dto.UserDTO;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author tobing
 * @date 2021/10/3 23:32
 * @description 基于XML的初始化过程
 */
public class Demo01MybatisStartByXmlAnalysis {


    /**
     * 重点分析
     * SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
     */
    public static void main(String[] args) throws IOException {
        // 读取配置文件
        InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
        // 创建 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
        // 创建 SqlSession
        SqlSession sqlSession = sqlSessionFactory.openSession();
        // 使用SqlSession获取代理对象
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        // 使用代理对象查询数据库
        UserDTO userDTO = mapper.findOneById(1);
        // 输出结果
        System.out.println(userDTO);
        // 关闭资源
        sqlSession.close();
        in.close();
    }
}
