package test.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import test.model.User;

@Mapper
@Repository
public interface UserDAO {

    User getUser(@Param("id") int id);
}
