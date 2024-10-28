package com.ai.chat.a.service.impl;

import com.ai.chat.a.po.Group;
import com.ai.chat.a.mapper.GroupMapper;
import com.ai.chat.a.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
}
