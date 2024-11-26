package com.ai.chat.a.controller;
import com.ai.chat.a.api.xfXh.XfXhCosClient;
import com.ai.chat.a.api.xfXh.utils.AgentUtil;
import com.ai.chat.a.dto.PlayCustomizeDto;
import com.ai.chat.a.dto.PlayDto;
import com.ai.chat.a.enums.ErrorCode;
import com.ai.chat.a.po.UserPlayerAgent;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.service.UserPlayerAgentService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ai.chat.a.api.xfXh.utils.PlayerUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/xf")
public class XfController {
    @Autowired
    private PlayerUtil playerUtil;
    @Autowired
    private AgentUtil agentUtil;
    @Autowired
    private UserPlayerAgentService xfService;
    @Autowired
    private XfXhCosClient xfXhCosClient;
    @Autowired
    private SessionService sessionService;
    @PostMapping("/cos/play")
    public R play(@RequestBody PlayDto playDto) throws Exception {
        UserPlayerAgent one = xfService.getOne(new LambdaQueryWrapper<UserPlayerAgent>().
                eq(UserPlayerAgent::getUserId, ThreadLocalUtil.get())
                .eq(UserPlayerAgent::getAgentId,playDto.getAgentId())
                .eq(UserPlayerAgent::getPlayerId,playDto.getPlayerId()));
        // TODO 查询
        return R.success(xfXhCosClient.call(playDto.getAgentId(),playDto.getPlayerId(),playDto.getPrompt(),one.getDescription(),one.getPlayerName()));
    }
    @PostMapping("/cos/customize/quickStart")
    public R playStartCustomize(@RequestBody PlayCustomizeDto playDTO) throws Exception {
        String userId = ThreadLocalUtil.get();
        Integer num = xfService.countPlayerNum(userId);
        log.info("num{}",num);
        if(num == 5){
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"已经创建了5个角色啦！");
        }
        String playerId = "";
        String agentId = "";
        String name  = playDTO.getPlayerName();
        String agentName = playDTO.getAgentName();
        if(!playerUtil.ifRegister(name)){

            playerId = playerUtil.register(name,playDTO.getPlayerType(),playDTO.getPlayerDescription(),playDTO.getSenderIdentity());
            log.info("playerId{}",playerId);
            agentId = agentUtil.createAgentCharacter(
                    playDTO.getMission(),
                    playDTO.getKeyPersonality(),
                    playerId,
                    agentName,
                    playDTO.getAgentType(),
                    playDTO.getDescription(),
                    playDTO.getPersonalityDescription(),
                    playDTO.getIdentity(),
                    playDTO.getHobby(),
                    playDTO.getOpeningIntroduction());
            xfService.save(UserPlayerAgent.builder()
                    .agentAvatar(playDTO.getAgentAvatar())
                    .playerAvatar(playDTO.getPlayerAvatar())
                    .playerName(name)
                    .agentId(agentId)
                    .userId(userId)
                    .playerId(playerId)
                    .description(playDTO.getMission())
                    .agentName(playDTO.getAgentName())
                    .build());
        }else {
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该用户名已经被注册啦！");
        }
        return R.success() ;
    }
    @PostMapping("/cos/register")
    public R registerPlayer(@RequestBody PlayCustomizeDto playDTO) throws Exception {
        Integer num = xfService.countPlayerNum(ThreadLocalUtil.get());
        if(num == 5){
            return R.error(ErrorCode.PARAMS_ERROR.getCode(),"已经创建了5个角色啦！");
        }
        String name = playDTO.getPlayerName();
        if(!playerUtil.ifRegister(name)){
            String  playerId = playerUtil.register(name,playDTO.getPlayerType(),playDTO.getPlayerDescription(),playDTO.getSenderIdentity());
            log.info("playerId{}",playerId);
            xfService.save(UserPlayerAgent.builder()
                    .playerName(name)
                    .userId(ThreadLocalUtil.get())
                    .playerId(playerId)
                    .build());
        }else {
            return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该用户名已经被注册啦！");
        }
        return R.success();
    }
}
