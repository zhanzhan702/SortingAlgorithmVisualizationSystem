package com.sorting.visualization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sorting.visualization.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
