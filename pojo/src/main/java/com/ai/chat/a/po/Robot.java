package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Robot implements Serializable {
    @TableId
    private String id;
    private String name;
    private String avatar;
    private int status;
    @TableField("info")
    private String information;
    private Integer categoryId;
    private String categoryName;
}
