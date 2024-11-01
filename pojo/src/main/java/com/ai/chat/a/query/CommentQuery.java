package com.ai.chat.a.query;
import com.ai.chat.a.properties.PageSizeProperties;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class CommentQuery extends PageQuery{
    private Integer commentId;
    private Integer type;
    private String  userId;
    private String robotId;
    public <T> Page<T> toMpPageDefaultSortByLike(){
        return toMpPageTwoColumn(false,"`like`","create_time");
    };
}
