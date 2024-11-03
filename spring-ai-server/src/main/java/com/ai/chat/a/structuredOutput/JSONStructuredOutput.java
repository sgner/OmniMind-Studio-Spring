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
        BeanOutputConverter<UserIdea> userIdeaBeanOutputConverter = new BeanOutputConverter<>(UserIdea.class);
        String format = userIdeaBeanOutputConverter.getFormat();
        String template = """
          Guess and generate the user's thoughts based on the idea{idea}
          Only one option can be true, or all options are false.\s
          The prompt must be in English.\s
          The style for the generated content should be chosen from the following options: Base, 3D Model, Analog Film, Anime, Cinematic, Comic Book, Craft Clay, Digital Art, Enhance, Fantasy Art, Isometric, Line Art, Lowpoly, Neonpunk, Origami, Photographic, Pixel Art, Texture. \s
          If it's not for generating anything, then the prompt and style must be empty strings.\s
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
    }
}
