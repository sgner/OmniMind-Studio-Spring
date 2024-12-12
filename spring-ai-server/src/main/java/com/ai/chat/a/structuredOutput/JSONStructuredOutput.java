package com.ai.chat.a.structuredOutput;

import com.ai.chat.a.entity.UserIdea;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JSONStructuredOutput {
    private final OpenAiChatModel chatModel;
    public UserIdea userIdeaOutput(String idea) {
        try{
            BeanOutputConverter<UserIdea> userIdeaBeanOutputConverter = new BeanOutputConverter<>(UserIdea.class);
            String format = userIdeaBeanOutputConverter.getFormat();
            String template = """
          {idea}
          Now please infer whether the above text is requesting the generation of an image, video, or audio, or if it is none of these.
          Note: You can only select one among generating an image, video, or audio; multiple selections like\s
          generateImage:true, generateVideo:true, generateVoice:false are not allowed. It can also be none, i.e., generateImage:false, generateVideo:false, generateVoice:false
          If it is not for generating an image, video, or audio, then there is no need to infer the style. If it is for generating an image or video, please infer the requested style, and the style can only be one of the following:
          Base: Basic Style
          3D Model
          Analog Film
          Anime
          Cinematic
          Comic Book
          Craft Clay
          Digital Art
          Enhance
          Fantasy Art
          Isometric
          Line Art
          Lowpoly
          Neonpunk
          Origami
          Photographic
          Pixel Art
          Texture
          If it is not for generating an image or video, then there is no need to provide prompts. If the text means generating similar images or videos (i.e., the user uploaded reference files), then prompts do not need to be provided.
          If it is for generating an image or video, please provide your recommended prompts; the prompts must be in English. Most importantly, you must give the following JSON format text at the end, with no other responses. Again, do not include any other responses; you must provide the following JSON format:
          {format}
        """;
            PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("idea", idea, "format", format));
            Prompt prompt = new Prompt(promptTemplate.createMessage(), OpenAiChatOptions.builder()
                    .withModel("gpt-4o-mini")
                    .withHttpHeaders(Map.of("Accept-Encoding","identity"))
                    .build());
            chatModel.call(prompt);
            Generation result = chatModel.call(prompt).getResult();
            return userIdeaBeanOutputConverter.convert(result.getOutput().getContent());
        }catch (Exception e){
             return UserIdea.builder().Style("").generateImage(false).generateVideo(false).generateVoice(false).prompt("").build();
        }
    }
}
