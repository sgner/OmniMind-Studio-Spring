package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.PlayerMapper;
import com.ai.chat.a.po.Player;
import com.ai.chat.a.service.PlayerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl extends ServiceImpl<PlayerMapper, Player> implements PlayerService {
}
