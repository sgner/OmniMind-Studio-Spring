package com.ai.chat.a.query;

import com.ai.chat.a.properties.PageSizeProperties;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
public class PageQuery {
    private Integer pageNo;
    private Integer pageSize;
    private String sortBy;
    private Boolean isAsc;
    public <T> Page<T> toMpPage(OrderItem... orders){
        if(pageSize == null){
             pageSize = 10;
        }
        Page<T> p = Page.of(pageNo, pageSize);
        if (sortBy != null) {
            p.addOrder(new OrderItem().setColumn(sortBy).setAsc(isAsc));
            return p;
        }
        if(orders != null){
            p.addOrder(orders);
        }
        return p;
    }

    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc){
        return this.toMpPage(new OrderItem().setColumn(defaultSortBy).setAsc(isAsc));
    }
    public <T> Page<T> toMpPageTwoColumn(boolean  isAsc, String ... defaultSortBy){
        return this.toMpPage(new OrderItem().setColumn(defaultSortBy[0]).setAsc(isAsc).setColumn(defaultSortBy[1]).setAsc(isAsc));
    }
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage("create_time", false);
    }

}
