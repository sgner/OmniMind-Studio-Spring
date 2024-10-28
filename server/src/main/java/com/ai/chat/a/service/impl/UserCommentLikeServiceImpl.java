package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.UserCommentLikeMapper;
import com.ai.chat.a.po.UserLikeComment;
import com.ai.chat.a.service.UserCommentLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserCommentLikeServiceImpl extends ServiceImpl<UserCommentLikeMapper, UserLikeComment> implements UserCommentLikeService {
}
