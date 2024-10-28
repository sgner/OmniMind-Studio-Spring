package com.ai.chat.a.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.constant.JwtClaimsConstant;
import com.ai.chat.a.constant.UserConstant;
import com.ai.chat.a.dto.*;
import com.ai.chat.a.entity.CommentsIsMe;
import com.ai.chat.a.entity.RobotSquare;
import com.ai.chat.a.entity.UserUploadFile;
import com.ai.chat.a.po.*;
import com.ai.chat.a.properties.JWTProperties;
import com.ai.chat.a.query.CommentQuery;
import com.ai.chat.a.redis.RedisComponent;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.*;
import com.ai.chat.a.utils.*;
import com.ai.chat.a.vo.LoginVO;
import com.ai.chat.a.vo.UserRobotQueryVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static com.ai.chat.a.enums.ErrorCode.NO_AUTH_ERROR;
import static com.ai.chat.a.enums.ErrorCode.PARAMS_ERROR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController{
    private final UserService userService;
    private final VerifyService verifyService;
    private final JWTProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final SubscribeService subscribeService;
    private final RedisUtil redisUtil;
    private final RobotService robotService;
    private final GroupService groupService;
    private final RedisComponent redisComponent;
    private final CommentService commentService;
    private final UserCommentLikeService userCommentLikeService;
    @PostMapping("/login")
    public R login(@RequestBody LoginDTO loginDTO){
        log.info("用户{}登录",loginDTO.getAccount());
        if(!verifyService.checkCheckCode(loginDTO.getCheckCode(),loginDTO.getTemporaryId())){
            return R.error(PARAMS_ERROR, "验证码错误");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername,loginDTO.getAccount())
                .or().eq(User::getEmail,loginDTO.getAccount())
                .or().eq(User::getAccount,loginDTO.getAccount());
        User user = userService.getOne(lambdaQueryWrapper);
        if(user == null){
            return R.error(NO_AUTH_ERROR, "用户不存在");
        }
        if(!PBKDF2Util.validatePassword(loginDTO.getPassword(),user.getPassword(),user.getSalt())){
            return R.error(NO_AUTH_ERROR, "密码错误");
        }

        String userHeartBeat = redisUtil.getUserHeartBeat(UserConstant.USER_WS_HEART_BEAT + user.getId());
        if(userHeartBeat != null){
            return R.error(PARAMS_ERROR, "用户已在其他地方登录");
        }
        Map<String,Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.USER_ID,user.getId());
        map.put(JwtClaimsConstant.SALT,user.getSalt());
       //TODO TOKEN
        String jwt = JWTUtils.createJWT(jwtProperties.getTtl(), jwtProperties.getSecretKey(), map);
        log.info("JWT:{}",jwt);
        stringRedisTemplate.opsForValue().set(jwt,jwt);
        return R.success(LoginVO.builder().userId(user.getId()).jwt(jwt).email(user.getEmail()).username(user.getUsername()).build());
    }

   @PostMapping("/register")
   @Transactional
    public  R register(@RequestBody RegisterDTO registerDTO){
       LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())
               .or().eq(User::getEmail, registerDTO.getEmail());
       if(userService.getOne(queryWrapper) != null){
           return R.error(PARAMS_ERROR, "用户名或邮箱已存在");
       }
       log.info("用户{}注册",registerDTO.getUsername());
        User user = BeanUtil.copyProperties(registerDTO,User.class);
        user.setAccount(UserConstant.USER_ACCOUNT_PREFIX+ UUID.randomUUID());
        String salt = PBKDF2Util.generateSalt();
        user.setPassword(PBKDF2Util.hashPassword(registerDTO.getPassword(),salt));
        user.setSalt(salt);
        redisComponent.saveSystemSettingInRedis(null);
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
        userService.addContact4Robot(user.getId());
        return R.success();
    }

    @GetMapping("/{id}")
//    @Cacheable(cacheNames = "setRobotListCache",key = "#id")
    public R robotQuery(@PathVariable("id") String id){
        LambdaQueryWrapper<Subscribe> subscribeLambdaQueryWrapper = new LambdaQueryWrapper<Subscribe>().eq(Subscribe::getUserId, id);
        List<Subscribe> subscribes = subscribeService.list(subscribeLambdaQueryWrapper);
        List<String> robotIds = subscribes.stream().map(Subscribe::getRobotId).toList();
        List<Robot> robots = robotService.listByIds(robotIds);
        Map<Integer, List<Robot>> groupRobot = robots.stream().collect(Collectors.groupingBy(Robot::getCategoryId));
        if (groupRobot.get(6) != null && groupRobot.get(1) != null) {
            groupRobot.get(1).addAll(groupRobot.get(6));
            groupRobot.remove(6);
        }
        LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<Group>().eq(Group::getUserId, id);
        List<Group> groups = groupService.list(groupLambdaQueryWrapper);
        return R.success(UserRobotQueryVO.builder().robotMap(groupRobot).groupList(groups).build());
    }
    @PostMapping("/subscribe")
    public R subscribe(@RequestBody UserSubscribeDTO userSubscribeDTO){
        userService.subscribeRobot(ThreadLocalUtil.get(),userSubscribeDTO.getRobotId(),userSubscribeDTO.getEndTime());
        return R.success();
    }
   @GetMapping("/uploadData/{sessionId}")
    public R getUploadData(@PathVariable String sessionId){
         try{
             List<UserUploadFile> userUploadFiles = JSONObject.parseArray(redisComponent.getUploadFileFromRedis(ThreadLocalUtil.get() + sessionId), UserUploadFile.class);
             return R.success(userUploadFiles);
         }catch (Exception e){
             e.printStackTrace();
             log.error("获取上传数据异常",e);
             return R.error(PARAMS_ERROR, "获取上传数据异常");
         }
   }
   @DeleteMapping("/uploadData/{sessionId}/index")
    public R deleteUploadDataByIndex(){
      return null;
   }
   @GetMapping("/robotList/{type}")
    public R getRobotByType(@PathVariable("type") Integer type){
        if(type == null){
             return R.success();
        }
          LambdaQueryWrapper<Robot> lambdaQueryWrapper = new LambdaQueryWrapper<>();
       if (type == 1) {
           lambdaQueryWrapper.eq(Robot::getCategoryId, 4)
                   .or().eq(Robot::getCategoryId, 6)
                   .or().eq(Robot::getCategoryId, 1); // 添加 categoryId 为 1 的条件
       } else {
           lambdaQueryWrapper.eq(Robot::getCategoryId, type);
       }
       List<Robot> robots = robotService.list(lambdaQueryWrapper.eq(Robot::getCategoryId,type));
       List<Subscribe> subscribeList = subscribeService.list(new LambdaQueryWrapper<Subscribe>().eq(Subscribe::getUserId, ThreadLocalUtil.get()));
       List<Robot> subscribeRobots = robotService.listByIds(
               subscribeList.stream()
                       .map(Subscribe::getRobotId)
                       .collect(Collectors.toList())
       );
       Set<String> subscribeRobotIds = subscribeRobots.stream()
               .filter(robot -> {
                   if(type == 1){
                     return  robot.getCategoryId() == 1 || robot.getCategoryId() == 4 || robot.getCategoryId() == 6;
                   }
                   return robot.getCategoryId() == type;
               })
               .map(Robot::getId)
               .collect(Collectors.toSet());

       List<RobotSquare> robotSquares = BeanUtil.copyToList(robots, RobotSquare.class);
       robotSquares.forEach(robotSquare -> {
           if (subscribeRobotIds.contains(robotSquare.getId())) {
               robotSquare.setSubscribed(true);
           }
       });
      return R.success(robotSquares);
   }
   @GetMapping("/new/session")
  public R newSession(String robotId){
       return R.success(userService.newSession(robotId));
   }
   @GetMapping("/cancel/subscribe")
    public R cancelSubscribe(String robotId){
         subscribeService.remove(new LambdaQueryWrapper<Subscribe>()
                 .eq(Subscribe::getUserId,ThreadLocalUtil.get())
                 .eq(Subscribe::getRobotId,robotId));
         return R.success();
   }

   @PostMapping("/comment")
    public R comment(@RequestBody CommentDTO commentDTO){
       User user = userService.getById(ThreadLocalUtil.get());

       Comments comments = Comments.builder()
               .robotId(commentDTO.getRobotId())
               .userId(ThreadLocalUtil.get())
               .content(commentDTO.getContent())
               .createTime(LocalDateTime.now())
               .type(commentDTO.getType())
               .username(user.getUsername())
               .avatar(user.getAvatar())
               .build();
       switch (commentDTO.getType()){
           case 1:
               commentService.save(comments);
               break;
           case 2:
               commentService.save(comments);
               // TODO 发送消息到管理员
               break;
           case 3:
               commentService.save(comments);
               // TODO 发送消息到管理员
               break;
       }
       Page<Comments> mpPageDefaultSortByLike = commentDTO.toMpPageDefaultSortByLike();
       Page<Comments> commentsPage = commentService.lambdaQuery()
               .eq(Comments::getRobotId, commentDTO.getRobotId())
               .eq(Comments::getType, commentDTO.getType())
               .page(mpPageDefaultSortByLike);

       PageDTO<Comments> commentsPageList = PageDTO.of(commentsPage, Comments.class);
       List<Comments> commentsList = commentsPageList.getList();
       List<Comments> addMe = new ArrayList<>();
       addMe.add(comments);
       addMe.addAll(commentsList);
       commentsPageList.setList(addMe);
       log.info("评论列表:{}",commentsPageList.getList());
       return R.success(commentsPageList);
   }
    @PostMapping("/comment/like")

   public R commentLike(@RequestBody CommentQuery commentQuery){
       UserLikeComment commentLikeServiceOne = userCommentLikeService.getOne(new LambdaQueryWrapper<UserLikeComment>()
               .eq(UserLikeComment::getUserId, ThreadLocalUtil.get())
               .eq(UserLikeComment::getCommentId, commentQuery.getCommentId())
               .eq(UserLikeComment::getRobotId, commentQuery.getRobotId()));
       UpdateChainWrapper<Comments> update = commentService.update();
       update
               .eq("user_id",ThreadLocalUtil.get())
               .eq("comment_id",commentQuery.getCommentId())
               .eq("robot_id",commentQuery.getRobotId());
       if(commentLikeServiceOne != null){
             update.setSql("like = like-1");
             userCommentLikeService.removeById(commentLikeServiceOne.getId());
       }else {
           update.setSql("like = like+1");
           userCommentLikeService.save(UserLikeComment.builder()
                   .userId(ThreadLocalUtil.get())
                   .robotId(commentQuery.getRobotId())
                   .build());
       }
        commentService.update(update);
//       Page<Comments> mpPageDefaultSortByLike = commentQuery.toMpPageDefaultSortByLike();
//       Page<Comments> commentsPage = commentService.lambdaQuery().eq(Comments::getRobotId, commentQuery.getRobotId())
//               .eq(Comments::getType, commentQuery.getType())
//               .page(mpPageDefaultSortByLike);
//       commentService.list(new LambdaQueryWrapper<Comments>().eq(Comments::getRobotId,commentLikeDTO.getRobotId()));

       return R.success(true);
   }
   @PostMapping("/myComment")
    public R myComment(@RequestBody CommentQuery commentQuery){
       Page<Comments> mpPageDefaultSortByLike = commentQuery.toMpPageDefaultSortByLike();
       Page<Comments> commentsPage = commentService.lambdaQuery()
               .eq(Comments::getRobotId, commentQuery.getRobotId())
               .eq(Comments::getUserId, ThreadLocalUtil.get())
               .page(mpPageDefaultSortByLike);
       return R.success(PageDTO.of(commentsPage,Comments.class));
   }
}
