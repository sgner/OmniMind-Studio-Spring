package com.ai.chat.a.image.qianfan;
import jakarta.annotation.Resource;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AAIQianfanImageClient {
    @Resource
    private QianFanImageModel qianFanImageModel;
    public String getImageFromQianfanSDXL(String prompt){
        ImageResponse response = qianFanImageModel.call(new ImagePrompt(prompt));
        if (response== null) {
            return "";
        }
        return response.getResult().getOutput().getB64Json();
    }
}
