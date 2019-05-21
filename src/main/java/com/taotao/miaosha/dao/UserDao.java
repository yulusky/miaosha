package com.taotao.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.taotao.miaosha.domain.User;
@Mapper
public interface UserDao {
	@Select("select * from user where id=#{id}")
	public User getUserById(int id) ;
	@Insert("insert into user(id, name)values(#{id}, #{name})")
	public int createUser(User user);
}
