package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`group`")
public class Group implements Serializable {
    @TableId
    private String id;
    private String name;
    private String userId;
    private Integer status;
    private LocalDateTime createTime;
    private String robotNumber;
    private String avatar;
}
