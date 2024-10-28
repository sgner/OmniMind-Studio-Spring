package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscribe {
 @TableId(type = IdType.AUTO)
 private Long id;
 private String userId;
 private String robotId;
 private int status;
 private LocalDateTime createTime;
 private LocalDateTime updateTime;
 private LocalDateTime endTime;
}
