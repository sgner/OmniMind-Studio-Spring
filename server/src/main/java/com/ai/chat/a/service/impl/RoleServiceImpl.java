package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.RoleMapper;
import com.ai.chat.a.po.Role;
import com.ai.chat.a.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
