package com.ai.chat.a.service.impl;

import com.ai.chat.a.po.Category;
import com.ai.chat.a.mapper.CategoryMapper;
import com.ai.chat.a.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

}
