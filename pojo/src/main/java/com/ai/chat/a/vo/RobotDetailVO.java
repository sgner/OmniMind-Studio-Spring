package com.ai.chat.a.vo;

import com.ai.chat.a.dto.PageDTO;
import com.ai.chat.a.po.Comments;
import com.ai.chat.a.po.Robot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RobotDetailVO{
    private Robot robot;
    private Boolean subscribed;
    private PageDTO<CommentsVO> comments;
}
