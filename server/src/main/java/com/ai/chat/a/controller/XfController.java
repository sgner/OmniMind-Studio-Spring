package com.ai.chat.a.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.builder.EqualsBuilder;
import com.ai.chat.a.api.xfXh.XfXhCosClient;
import com.ai.chat.a.api.xfXh.dto.InteractiveRequest;
import com.ai.chat.a.api.xfXh.response.AgentCharacter;
import com.ai.chat.a.api.xfXh.utils.AgentUtil;
import com.ai.chat.a.dto.*;
import com.ai.chat.a.enums.ErrorCode;
import com.ai.chat.a.po.*;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.*;
import com.ai.chat.a.utils.StringTools;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.ai.chat.a.api.xfXh.utils.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/xf/cos")
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
    private PlayerService playerService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private CosContextService cosContextService;
    @Autowired
    private SessionService sessionService;
    @PostMapping("/play")
    public R play(@RequestBody PlayDto playDto) throws Exception {
        UserPlayerAgent one = xfService.getOne(new LambdaQueryWrapper<UserPlayerAgent>().
                eq(UserPlayerAgent::getUserId, ThreadLocalUtil.get())
                .eq(UserPlayerAgent::getAgentId,playDto.getAgentId()));
        if(one == null){
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该角色不存在");
        }
        CosContext cosContext = cosContextService.getOne(new LambdaQueryWrapper<CosContext>()
                .eq(CosContext::getUserId, ThreadLocalUtil.get())
                .eq(CosContext::getRoleId, playDto.getAgentId())
                .eq(CosContext::getPlayerId, one.getPlayerId()));
        String chatId = UUID.randomUUID().toString().substring(0, 10);
        String preChatId = "";
        List<InteractiveRequest.Text> context = new ArrayList<>();
        if(cosContext != null){
          context =   JSONObject.parseArray(cosContext.getText(),InteractiveRequest.Text.class);
          preChatId = cosContext.getChatId();
        }
        if(!Objects.equals(playDto.getPrompt(), "")){
            InteractiveRequest.Text text1 = new InteractiveRequest.Text();
            text1.setContent(playDto.getPrompt());
            text1.setRole(one.getPlayerName());
            context.add(text1);
            if(cosContext == null){
                cosContextService.save(CosContext.builder()
                        .roleId(playDto.getAgentId())
                        .chatId(chatId)
                        .text(JSONObject.toJSONString(context))
                        .playerId(one.getPlayerId())
                        .userId(ThreadLocalUtil.get())
                        .build());
            }else {
                 cosContextService.update(new LambdaUpdateWrapper<CosContext>()
                         .eq(CosContext::getUserId, ThreadLocalUtil.get())
                         .eq(CosContext::getRoleId,playDto.getAgentId())
                         .eq(CosContext::getPlayerId,one.getPlayerId())
                         .set(CosContext::getText,JSONObject.toJSONString(context))
                         .set(CosContext::getChatId,chatId));
            }

        }
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getAgentId, playDto.getAgentId()));
        Player player = playerService.getOne(new LambdaQueryWrapper<Player>().eq(Player::getPlayerId, one.getPlayerId()));
        xfXhCosClient.call(
                playDto.getAgentId(),
                one.getPlayerId(),
                one.getDescription(),
                chatId,
                preChatId,
                context,
                role,
                player,
                playDto.getSessionId(),
                ThreadLocalUtil.get()
        );
        return R.success();
    }
    @PostMapping("/customize/quickStart")
    @Transactional
    public R playStartCustomize(@RequestBody PlayCustomizeDto playDTO) throws Exception {
        String userId = ThreadLocalUtil.get();
        Integer num = xfService.countPlayerNum(userId);
        log.info("num{}",num);
        if(num == 5){
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"已经创建了5个玩家啦！");
        }
        String playerId = "";
        String agentId = "";
        String name  = playDTO.getPlayerName();
        String agentName = playDTO.getAgentName();
        if(name == null || agentName == null){
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"请输入角色名称！");
        }
        if(!playerUtil.ifRegister(name)){
            if(playDTO.getMission() == null){
                 return R.error(ErrorCode.PARAMS_ERROR.getCode(),"请输入场景描述！");
            }
            playerId = playerUtil.register(name,playDTO.getPlayerType(),playDTO.getDescription(),playDTO.getSenderIdentity());
            log.info("playerId{}",playerId);
            agentId = agentUtil.createAgentCharacter(
                    playDTO.getMission(),
                    playDTO.getKeyPersonality(),
                    playerId,
                    agentName,
                    playDTO.getAgentType(),
                    playDTO.getAgentDescription(),
                    playDTO.getPersonalityDescription(),
                    playDTO.getAgentIdentity(),
                    playDTO.getHobby(),
                    playDTO.getOpeningIntroduction());

            String sessionId = StringTools.getChatSessionId4User(new String[]{playerId, agentId});
            Player player = BeanUtil.copyProperties(playDTO, Player.class);
            Role role = BeanUtil.copyProperties(playDTO, Role.class);
            player.setPlayerId(playerId);
            role.setAgentId(agentId);
            player.setUserId(ThreadLocalUtil.get());
            playerService.save(player);
            roleService.save(role);
            List<InteractiveRequest.Text> text = new ArrayList<>();
            String chatId = UUID.randomUUID().toString().substring(0, 10);
            CosContext cosContext = CosContext.builder()
                    .text(JSONObject.toJSONString(text))
                    .chatId(chatId)
                    .roleId(agentId)
                    .playerId(playerId)
                    .userId(userId).build();
            cosContextService.save(cosContext);

            xfXhCosClient.call(agentId, playerId, playDTO.getMission(), chatId,"",text,role,player,sessionId,userId);

        }else {
             return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该用户名已经被注册啦！");
        }
        return R.success() ;
    }
    @PostMapping("/register")
    public R registerPlayer(@RequestBody PlayCustomizeDto playDTO){
        try{
            String playerName = playDTO.getPlayerName();
            if(playerName == null || playerName.isEmpty()){
                return R.error(ErrorCode.PARAMS_ERROR.getCode(),"请输入玩家名称！");
            }
            log.info(playDTO.toString());
            Integer num = xfService.countPlayerNum(ThreadLocalUtil.get());
            if(num == 5){
                return R.error(ErrorCode.PARAMS_ERROR.getCode(),"已经创建了5个玩家啦！");
            }
            String name = playDTO.getPlayerName();
            if(!playerUtil.ifRegister(name)){
                String  playerId = playerUtil.register(name,playDTO.getPlayerType(),playDTO.getDescription(),playDTO.getSenderIdentity());
                log.info("playerId{}",playerId);
                Player player = BeanUtil.copyProperties(playDTO, Player.class);
                player.setPlayerId(playerId);
                player.setUserId(ThreadLocalUtil.get());
                playerService.save(player);
            }else {
                return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该用户名已经被注册啦！");
            }
            return R.success();
        }catch (Exception e){
              return R.error(ErrorCode.OPERATION_ERROR.getCode(),"服务异常");
        }
    }

    @PostMapping("/addRole")
    public R addRole(@RequestBody PlayCustomizeDto playDto) {
       try{
           String agentName = playDto.getAgentName();
           if(agentName == null || agentName.isEmpty()){
               return R.error(ErrorCode.PARAMS_ERROR.getCode(),"请输入角色名称！");
           }
           if(playDto.getMission() == null || playDto.getMission().isEmpty()){
               return R.error(ErrorCode.PARAMS_ERROR.getCode(),"请输入场景描述！");
           }
           Integer num = xfService.countAgentNum(ThreadLocalUtil.get(),playDto.getPlayerId());
           if(num == 5){
               return R.error(ErrorCode.PARAMS_ERROR.getCode(),"该玩家账号已经创建了5个角色啦！");
           }
           Role role = BeanUtil.copyProperties(playDto, Role.class);
           String agentId = agentUtil.createAgentCharacter(
                   role.getMission(),
                   role.getKeyPersonality(),
                   playDto.getPlayerId(),
                   role.getAgentName(),
                   role.getAgentType(),
                   role.getAgentDescription(),
                   role.getPersonalityDescription(),
                   role.getAgentIdentity(),
                   role.getHobby(),
                   playDto.getOpeningIntroduction());
           Role rolen = Role.builder()
                   .agentAvatar(role.getAgentAvatar())
                   .agentDescription(role.getAgentDescription())
                   .agentIdentity(role.getAgentIdentity())
                   .agentId(agentId)
                   .agentType(role.getAgentType())
                   .agentName(role.getAgentName())
                   .personalityDescription(role.getPersonalityDescription())
                   .mission(role.getMission())
                   .keyPersonality(role.getKeyPersonality())
                   .hobby(role.getHobby()).build();
           roleService.save(rolen);
           Player player = playerService.getOne(new LambdaQueryWrapper<Player>().eq(Player::getPlayerId, playDto.getPlayerId()));
           UserPlayerAgent userPlayerAgent = UserPlayerAgent.builder()
                   .userId(ThreadLocalUtil.get())
                   .playerName(player.getPlayerName())
                   .playerId(player.getPlayerId())
                   .description(rolen.getMission())
                   .agentName(rolen.getAgentName())
                   .agentId(rolen.getAgentId())
                   .build();
           xfService.save(userPlayerAgent);
           return R.success();
       }catch (Exception e){
            return R.error(ErrorCode.OPERATION_ERROR.getCode(),"服务异常");
       }
    }

    // TODO 查询角色列表

    @GetMapping()
    public R getRoleList(){
        List<PlayerQueryDTO> resultList = new ArrayList<>();
        playerQueryDTOList(resultList);
        log.info("resultList{}",resultList);
        return R.success(resultList);
    }

    @PutMapping
    public R modifyPlayer(@RequestBody PlayCustomizeDto playDTO) {
        // TODO 修改完后需要清理上下文和短期记忆

        Player player = BeanUtil.copyProperties(playDTO, Player.class);
        player.setUserId(ThreadLocalUtil.get());
        Player before = playerService.getById(playDTO.getPlayerId());
        if(EqualsBuilder.reflectionEquals(player,before)){
             log.info("没有修改");
             log.info("player{}",player);
             return R.success();
        }
        try{
            // 修改player
            playerUtil.modify(
                    playDTO.getPlayerId(),
                    playDTO.getPlayerName(),
                    playDTO.getPlayerType(),
                    playDTO.getDescription(),
                    playDTO.getSenderIdentity()
            );
            // 更新player表
            playerService.updateById(player);

            // 更新user_player_agent表
            xfService.update(new LambdaUpdateWrapper<UserPlayerAgent>()
                    .eq(UserPlayerAgent::getPlayerId,playDTO.getPlayerId())
                    .eq(UserPlayerAgent::getUserId,ThreadLocalUtil.get())
                    .set(UserPlayerAgent::getPlayerName,player.getPlayerName()));

            // 查询该player下的sessionId
            List<UserPlayerAgent> userPlayerAgents = xfService.list(new LambdaQueryWrapper<UserPlayerAgent>().eq(UserPlayerAgent::getPlayerId, playDTO.getPlayerId())
                    .eq(UserPlayerAgent::getUserId, ThreadLocalUtil.get()));
            List<String> sessionIds = userPlayerAgents.stream().map(UserPlayerAgent::getSessionId).toList();


            // 更新session表
            if(!sessionIds.isEmpty()){
                sessionService.update(new LambdaUpdateWrapper<Session>()
                        .set(Session::getUserAvatar,player.getPlayerAvatar())
                        .set(Session::getUserName,player.getPlayerName())
                        .in(Session::getSessionId,sessionIds));
            }
            // 将更新后的所有角色返回
            ArrayList<PlayerQueryDTO> resultList = new ArrayList<>();
            playerQueryDTOList(resultList);
            return R.success(UpdatePlayerDTO.builder().currentPlayer(player).playerQueries(resultList).build());
        }catch (Exception e){
            return R.error(ErrorCode.OPERATION_ERROR.getCode(),"服务异常");
        }
    }
    @PutMapping("/role")
    public R modifyRole(@RequestBody PlayCustomizeDto playDTO){

        // TODO 修改完后需要清理上下文和短期记忆
        Role role = BeanUtil.copyProperties(playDTO, Role.class);
        log.info(playDTO.getPlayerId());
        Role before = roleService.getById(role.getAgentId());
        if(EqualsBuilder.reflectionEquals(role,before)){
            log.info("没有修改");
            return R.success();
        }
        try{
            // 编辑role
            agentUtil.editAgentCharacter(
                    playDTO.getPlayerId(),
                    role.getAgentId(),
                    role.getAgentName(),
                    role.getAgentType(),
                    role.getAgentDescription(),
                    role.getAgentIdentity(),
                    role.getPersonalityDescription(),
                    role.getHobby(),
                    role.getKeyPersonality(),
                    role.getMission());
            // 更新role表
            roleService.updateById(role);

            // 更新user_player_agent表
            xfService.update(new LambdaUpdateWrapper<UserPlayerAgent>()
                    .eq(UserPlayerAgent::getPlayerId,playDTO.getPlayerId())
                    .eq(UserPlayerAgent::getUserId,ThreadLocalUtil.get())
                    .eq(UserPlayerAgent::getAgentId,role.getAgentId())
                    .set(UserPlayerAgent::getDescription,role.getMission())
                    .set(UserPlayerAgent::getAgentName,role.getAgentName()));

            UserPlayerAgent one = xfService.getOne(new LambdaQueryWrapper<UserPlayerAgent>().eq(UserPlayerAgent::getPlayerId, playDTO.getPlayerId())
                    .eq(UserPlayerAgent::getUserId, ThreadLocalUtil.get())
                    .eq(UserPlayerAgent::getAgentId, role.getAgentId()));

            if(one.getSessionId() != null){
                sessionService.update(new LambdaUpdateWrapper<Session>()
                        .eq(Session::getSessionId,one.getSessionId())
                        .set(Session::getRobotAvatar,role.getAgentAvatar())
                        .set(Session::getRobotName,role.getAgentName()));
            }
            ArrayList<PlayerQueryDTO> playerQueryDTOS = new ArrayList<>();
            playerQueryDTOList(playerQueryDTOS);
            return R.success(UpdateRoleDTO.builder().currentRole(role).playerQueries(playerQueryDTOS).build());
        }catch (Exception e){
                return R.error(ErrorCode.OPERATION_ERROR.getCode(),"服务异常");
        }
    }

    @DeleteMapping("/{id}")
    public R deletePlayer(@PathVariable("id") String id,@RequestParam("name") String name){
        try {
            playerUtil.delete(id,name);
            // TODO 清除所有记录
        } catch (Exception e) {
           return R.error(ErrorCode.OPERATION_ERROR.getCode(),"删除时异常");
        }
        return R.success();
    }
    @DeleteMapping("/role/{id}")
    public R deleteRole(@PathVariable("id") String id,@RequestParam("name") String name){
        try {
            agentUtil.deleteAgentCharacter(id,name);
            // TODO 清除所有记录
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return R.success();
    }

    @PostMapping("/session")
    public R session(@RequestBody XfCosSessionDTO xfCosSessionDTO) throws Exception {
        log.info("xfCosSessionDTO{}",xfCosSessionDTO);
        UserPlayerAgent userPlayerAgent = xfService.getOne(new LambdaQueryWrapper<UserPlayerAgent>()
                .eq(UserPlayerAgent::getPlayerId, xfCosSessionDTO.getPlayer().getPlayerId())
                .eq(UserPlayerAgent::getAgentId, xfCosSessionDTO.getRole().getAgentId()));
        String sessionId = userPlayerAgent.getSessionId();
        if(sessionId!= null){
            Session session = sessionService.getOne(new LambdaQueryWrapper<Session>().eq(Session::getSessionId, sessionId));
            return R.success(session);
        }else{
            sessionId = StringTools.getChatSessionId4User(new String[]{xfCosSessionDTO.getPlayer().getPlayerId(),xfCosSessionDTO.getRole().getAgentId()});
            List<InteractiveRequest.Text> text = new ArrayList<>();
            String chatId = UUID.randomUUID().toString().substring(0, 10);
            CosContext cosContext = CosContext.builder()
                    .text(JSONObject.toJSONString(text))
                    .chatId(chatId)
                    .roleId(xfCosSessionDTO.getRole().getAgentId())
                    .playerId(xfCosSessionDTO.getPlayer().getPlayerId())
                    .userId(ThreadLocalUtil.get())
                    .build();
            cosContextService.save(cosContext);
            xfXhCosClient.call(xfCosSessionDTO.getRole().getAgentId(),
                   xfCosSessionDTO.getPlayer().getPlayerId(),
                    xfCosSessionDTO.getRole().getMission(),
                    chatId,
                    "",
                    text,
                    xfCosSessionDTO.getRole(),
                    xfCosSessionDTO.getPlayer(),
                    sessionId,ThreadLocalUtil.get());
            return R.success();
        }
    }

    private void playerQueryDTOList(List<PlayerQueryDTO> resultList){
        List<Player> playerList = playerService.list(new LambdaQueryWrapper<Player>().eq(Player::getUserId, ThreadLocalUtil.get()));
        playerList.forEach(player ->{
            List<UserPlayerAgent> userPlayerAgents = xfService.list(new LambdaQueryWrapper<UserPlayerAgent>().eq(UserPlayerAgent::getPlayerId, player.getPlayerId()));
            List<String> agentIds = userPlayerAgents.stream().map(UserPlayerAgent::getAgentId).toList();
            List<Role> roles = new ArrayList<>();
            if(!agentIds.isEmpty()){
                roles = roleService.listByIds(agentIds);
            }
            PlayerQueryDTO playerQueryDTO = PlayerQueryDTO.builder().player(player).roles(roles).build();
            resultList.add(playerQueryDTO);
        });
    }
}
