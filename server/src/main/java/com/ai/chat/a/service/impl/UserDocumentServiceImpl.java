package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.UserDocumentMapper;
import com.ai.chat.a.po.UserDocument;
import com.ai.chat.a.service.UserDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserDocumentServiceImpl extends ServiceImpl<UserDocumentMapper, UserDocument> implements UserDocumentService  {
}
