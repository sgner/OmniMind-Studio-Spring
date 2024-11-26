package com.ai.chat.a.controller;

import com.ai.chat.a.api.gcuiArtAPI.dto.SunoCustomDTO;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoFastDTO;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoAudioResponse;
import com.ai.chat.a.api.gcuiArtAPI.util.RequestGcui;
import com.ai.chat.a.po.UserSunoAudio;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.UserSunoAudioService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/suno")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SunoController {
    private final RequestGcui requestGcui;
    private final UserSunoAudioService userSunoAudioService;
    @PostMapping("/fast")
      public R generateSong(@RequestBody SunoFastDTO sunoDTO){
          requestGcui.GenerateSongRequest(sunoDTO, ThreadLocalUtil.get());
          return R.success();
      }

      @PostMapping("/custom")
    public R generateSongCustom(@RequestBody SunoCustomDTO sunoDTO){
          requestGcui.GenerateSongRequest(sunoDTO, ThreadLocalUtil.get());
          return R.success();
      }
      @GetMapping("/result")
      public R getSongResult() throws IOException {
          List<UserSunoAudio> list = userSunoAudioService.list(new LambdaQueryWrapper<UserSunoAudio>().eq(UserSunoAudio::getUserId, ThreadLocalUtil.get()));
          List<String> ids = list.stream().map(UserSunoAudio::getId).toList();
          log.info(ids.toString());
          List<SunoAudioResponse> generatedSongRequest = requestGcui.getGeneratedSongRequest(""); // 暂时查询全部
          return R.success(generatedSongRequest);
      }
}
