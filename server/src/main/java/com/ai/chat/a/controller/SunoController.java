package com.ai.chat.a.controller;

import com.ai.chat.a.api.gcuiArtAPI.dto.SunoCustomDTO;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoFastDTO;
import com.ai.chat.a.api.gcuiArtAPI.util.RequestGcui;
import com.ai.chat.a.result.R;
import com.ai.chat.a.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/suno")
@RestController
@RequiredArgsConstructor
public class SunoController {
    private final RequestGcui requestGcui;
    @PostMapping("/fast/{sessionId}")
      public R generateSong(@RequestBody SunoFastDTO sunoDTO, @PathVariable String sessionId){
          requestGcui.GenerateSongRequest(sunoDTO, ThreadLocalUtil.get(),sessionId);
          return R.success();
      }

      @PostMapping("/custom/{sessionId}")
    public R generateSongCustom(@RequestBody SunoCustomDTO sunoDTO, @PathVariable String sessionId){
          requestGcui.GenerateSongRequest(sunoDTO, ThreadLocalUtil.get(),sessionId);
          return R.success();
      }
}
