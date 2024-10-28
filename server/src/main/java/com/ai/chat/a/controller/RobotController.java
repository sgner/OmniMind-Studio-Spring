package com.ai.chat.a.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.dto.PageDTO;
import com.ai.chat.a.po.Comments;
import com.ai.chat.a.po.Subscribe;
import com.ai.chat.a.po.UserLikeComment;
import com.ai.chat.a.query.CommentQuery;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.CommentService;
import com.ai.chat.a.service.RobotService;
import com.ai.chat.a.service.SubscribeService;
import com.ai.chat.a.service.UserCommentLikeService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.ai.chat.a.vo.CommentsVO;
import com.ai.chat.a.vo.RobotDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.ai.chat.a.po.Robot;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/robot")
public class RobotController {
    private final RobotService robotService;
    private final CommentService commentService;
    private final SubscribeService subscribeService;
    private final UserCommentLikeService userLikeCommentService;
    @PostMapping("/detail")
    public R getRobotDetail(@RequestBody CommentQuery commentQuery) {
        boolean subscribe = false;
        Robot one = robotService.getOne(new LambdaQueryWrapper<Robot>().eq(Robot::getId, commentQuery.getRobotId()));

        if (one != null) {
            Subscribe subscribeServiceOne = subscribeService.getOne(
                    new LambdaQueryWrapper<Subscribe>()
                            .eq(Subscribe::getUserId, ThreadLocalUtil.get())
                            .eq(Subscribe::getRobotId, commentQuery.getRobotId())
            );
            subscribe = subscribeServiceOne != null;
        }
        PageDTO<CommentsVO> commentsVOs = getCommentVOs(commentQuery);
        return R.success(new RobotDetailVO(one, subscribe, commentsVOs));
    }
    @PostMapping("/comments")
    public R getComments(@RequestBody CommentQuery commentQuery){
        PageDTO<CommentsVO> commentVOs = getCommentVOs(commentQuery);
        return R.success(commentVOs);
    }

    private PageDTO<CommentsVO> getCommentVOs(CommentQuery query) {
        Page<Comments> mpPageDefaultSortByLike = query.toMpPageDefaultSortByLike();
        Page<Comments> commentsPage = commentService.lambdaQuery()
                .eq(Comments::getRobotId, query.getRobotId())
                .eq(Comments::getType, query.getType())
                .page(mpPageDefaultSortByLike);
        PageDTO<CommentsVO> commentsVOPageDTO = PageDTO.of(commentsPage, CommentsVO.class);
        Set<Integer> likedCommentIds = userLikeCommentService.list(new LambdaQueryWrapper<UserLikeComment>()
                        .eq(UserLikeComment::getUserId, ThreadLocalUtil.get())
                        .eq(UserLikeComment::getRobotId, query.getRobotId()))
                .stream()
                .map(UserLikeComment::getCommentId)
                .collect(Collectors.toSet());
        commentsVOPageDTO.getList().forEach(commentsVO -> commentsVO.setIsLike(likedCommentIds.contains(commentsVO.getId())));
        return commentsVOPageDTO;
    }
}
