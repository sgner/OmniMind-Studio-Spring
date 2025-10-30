package com.ai.chat.a.websocket;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.enums.MessageTypeEnum;
import com.ai.chat.a.enums.UserRobotTypeEnum;
import com.ai.chat.a.po.*;
import com.ai.chat.a.entity.WsInitData;
import com.ai.chat.a.query.SessionChatUserQuery;
import com.ai.chat.a.service.AnswersService;
import com.ai.chat.a.service.ConversationsService;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.utils.JsonUtils;
import com.ai.chat.a.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChannelContextUtils {
    private final RedisUtil redisUtil;
    private final SessionService sessionService;
    private final AnswersService answersService;
    private static ConversationsService conversationsService;
    @Autowired
    private void setConversationsService(ConversationsService conversationsService){
        ChannelContextUtils.conversationsService = conversationsService;
    }
    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();
      public void addContext(String id, Channel channel){
          try{
              String channelId = channel.id().toString();
              log.info("channelId:{}", channelId);
              AttributeKey<String> attribute = null;
              if(AttributeKey.exists(channelId)){
                  attribute = AttributeKey.newInstance(channelId);
              }else {
                  attribute = AttributeKey.valueOf(channelId);
              }
              channel.attr(attribute).set(String.valueOf(id));

              // TODO

              USER_CONTEXT_MAP.put(String.valueOf(id), channel);
              redisUtil.setUserHeartBeat(String.valueOf(id));

              SessionChatUserQuery sessionQuery = new SessionChatUserQuery();
              sessionQuery.setPageNo(0);
              sessionQuery.setPageSize(20);
              Page<Session> page = sessionQuery.toMpPage(new OrderItem().setColumn("last_time").setAsc(false));
              Page<Session> paged = sessionService.lambdaQuery().eq(Session::getUserId, id).page(page);
              List<Session> records = paged.getRecords();
              List<SessionChatUser> sessionChatUsers = BeanUtil.copyToList(records, SessionChatUser.class);


              //TODO 消息查询
              ArrayList<MessageDTO> messageDTOS = new ArrayList<>();
              MessageDTO.MessageDTOBuilder messageDTOBuilder = MessageDTO.builder();
              LambdaQueryWrapper<Conversations> lambdaQueryWrapper = new LambdaQueryWrapper<Conversations>().eq(Conversations::getUserId, id);
              List<Conversations> conversations = conversationsService.list(lambdaQueryWrapper);
              conversations.forEach(con -> {
                  LambdaQueryWrapper<Answers> queryWrapper = new LambdaQueryWrapper<Answers>().eq(Answers::getConversationId, con.getId());
                  List<Answers> list = answersService.list(queryWrapper);
                  MessageDTO messageDTO = messageDTOBuilder.question(con).answers(list).build();
                  messageDTOS.add(messageDTO);
              });
              WsInitData wsInitData = WsInitData.builder().sessionList(sessionChatUsers).messageList(messageDTOS).build();
              MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                      .messageType(MessageTypeEnum.INIT.getType())
                      .contactId(String.valueOf(id)).extendData(wsInitData).build();
              sendMsg(messageSendDTO, String.valueOf(id));

          }catch(Exception e){
              log.error("初始化链接失败", e);
          }

      }

      public static void sendMsg(MessageSendDTO messageSendDto, String reciveId){
               if (reciveId == null){
                   return;
               }
               Channel channel = USER_CONTEXT_MAP.get(reciveId);
               if(channel == null){
                   return;
               }
               if(MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())){
                   Robot robotInfo = (Robot) messageSendDto.getExtendData();
                   messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
                   messageSendDto.setContactId(String.valueOf(robotInfo.getId()));
                   messageSendDto.setContactName(robotInfo.getName());
                   messageSendDto.setExtendData(null);
               } else {
                   messageSendDto.setContactId(messageSendDto.getSendUserId());
                   messageSendDto.setContactName(messageSendDto.getSendUserNickName());
               }
          channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

      }

    public static void sendFluxMsg(Flux<ChatResponse> fluxMessage, String receiveId) {
        if (receiveId == null) {
            return;
        }
        Channel channel = USER_CONTEXT_MAP.get(receiveId);
        if (channel == null) {
            return;
        }

        fluxMessage.subscribe(
                message -> {
                    channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(message)));
                },
                throwable -> log.error("Error occurred while sending Flux message", throwable),
                () -> log.info("Completed sending all messages in Flux")
        );
    }

    /**
     * 发送流式对话消息
     * @param userId 用户ID
     * @param message 消息内容
     */
    public static void sendStreamMessage(String userId, String message) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel != null && channel.isActive()) {
            try {
                channel.writeAndFlush(new TextWebSocketFrame(message));
                log.info("发送流式对话消息给用户: {}, 消息长度: {}", userId, message.length());
            } catch (Exception e) {
                log.error("发送流式对话消息失败", e);
            }
        } else {
            log.warn("用户 {} 连接已关闭或不存在", userId);
        }
    }
    
    /**
     * 发送流式响应给用户
     * @param userId 用户ID
     * @param flux 流式响应
     */
    public static void sendFluxResponse(String userId, Flux<ChatResponse> flux) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel != null && channel.isActive()) {
            flux.subscribe(
                chatResponse -> {
                    try {
                        String content = chatResponse.getResult().getOutput().getContent();
                        channel.writeAndFlush(new TextWebSocketFrame(content));
                    } catch (Exception e) {
                        log.error("发送流式响应片段失败", e);
                    }
                },
                error -> {
                    log.error("流式响应处理失败", error);
                },
                () -> {
                    log.info("流式响应发送完成，用户ID: {}", userId);
                }
            );
        }
    }
      private void add2Group(String groupId,Channel channel){
            ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
            if(group == null){
                    group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                    GROUP_CONTEXT_MAP.put(groupId, group);
            }
            if(channel == null){
                    return;
            }
            group.add(channel);
      }

      private void send2Group(MessageSendDTO messageSendDto){
              // TODO 将消息推送给群组

      }

      private void send2User(MessageSendDTO messageSendDto){
            // TODO 将消息推送给用户
           sendMsg(messageSendDto,messageSendDto.getContactId());
      }


      public void sendMessage(MessageSendDTO messageSendDto){
          UserRobotTypeEnum prefix = UserRobotTypeEnum.getByPrefix(messageSendDto.getContactId());
          if(prefix != null){
              send2Group(messageSendDto);
          }else{
              send2User(messageSendDto);
          }
      }

}
