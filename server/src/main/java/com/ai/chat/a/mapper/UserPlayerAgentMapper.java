package com.ai.chat.a.mapper;

import com.ai.chat.a.po.UserPlayerAgent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

@Mapper
public interface UserPlayerAgentMapper extends BaseMapper<UserPlayerAgent> {
    @Select("select count(distinct player_id) from user_player_agent where user_id = #{userId}")
    public Integer countPlayerNum(String userId);
}
