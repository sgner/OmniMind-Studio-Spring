package com.ai.chat.a.mq;

import com.ai.chat.a.api.xfXh.dto.InteractiveRequest;
import com.ai.chat.a.dto.CosResponseDTO;
import com.ai.chat.a.entity.ContextText;
import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import com.ai.chat.a.po.Session;
import com.ai.chat.a.po.UserPlayerAgent;
import com.ai.chat.a.service.SessionService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.ai.chat.a.service.UserPlayerAgentService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CosResponseReceive {
    private final UserPlayerAgentService xfService;
    private final SessionService sessionService;
    private final static String QUEUE_NAME = "cos-response";
    private final static String EXCHANGE_NAME = "cos-response-exchange";
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = QUEUE_NAME,durable = "true"),
                    exchange = @Exchange(name = EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void receive(CosResponseDTO message){
        try{
            log.info("收到cos消息:{}",message);
            String result = message.getMessage();
            Role role = message.getRole();
            Player player = message.getPlayer();
            String userId = message.getUserId();
            log.info("userId:{}",userId);
            String sessionId = message.getSessionId();
            String context = message.getContext();
            log.info(context);
            List<ContextText> contextTexts = JSONObject.parseArray(context, ContextText.class);
            Session session = sessionService.getOne(new LambdaQueryWrapper<Session>().eq(Session::getSessionId, sessionId));
            if(session == null){
                xfService.createSession(role,player,userId,result,sessionId);
                xfService.update(new LambdaUpdateWrapper<UserPlayerAgent>()
                        .set(UserPlayerAgent::getSessionId,sessionId)
                        .eq(UserPlayerAgent::getUserId,userId)
                        .eq(UserPlayerAgent::getPlayerId,player.getPlayerId())
                        .eq(UserPlayerAgent::getAgentId,role.getAgentId()));
            }else {
                sessionService.updateSession(userId,session,contextTexts.get(contextTexts.size()-1).getContent(),result,player,role);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
