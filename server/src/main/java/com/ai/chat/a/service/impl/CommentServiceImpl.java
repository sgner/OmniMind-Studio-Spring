package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.CommentMapper;
import com.ai.chat.a.po.Comments;
import com.ai.chat.a.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comments> implements CommentService {

}
