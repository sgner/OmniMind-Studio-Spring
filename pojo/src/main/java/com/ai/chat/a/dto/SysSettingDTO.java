package com.ai.chat.a.dto;
import com.ai.chat.a.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingDTO {
    private Integer maxGroupCount = 5;
    private Integer maxGroupMemberCount = 500;
    private Integer maxImageSize = 2;
    private Integer maxVideoSize = 5;
    private Integer maxFileSize = 5;
    private String robotUid = Constants.ROBOT_UID;
    private String robotNickName = "A-AI";
    private String robotWelcome = "欢迎使用A-AI";
}
