package com.cloud.demo.service;

import com.cloud.demo.entity.User;
import com.cloud.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Base64;
import java.util.Date;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    private String salt="busyjzy599";

    /**
     * 获取user
     *
     * @return
     */
    public User getUser(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    /**
     * 验证用户名和密码
     *
     * @param name
     * @param password
     * @return
     */
    public User checkUser(String name, String password) {
        Example example = new Example(User.class);
        String s = password  + salt;
        example.createCriteria().andEqualTo("name", name).andEqualTo("password", DigestUtils.md5DigestAsHex(s.getBytes()));
        User user = userMapper.selectOneByExample(example);
        return user;
    }

    public void updateUser(User user) {
        userMapper.updateByPrimaryKey(user);
    }
}
