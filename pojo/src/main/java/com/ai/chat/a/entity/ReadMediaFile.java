package com.ai.chat.a.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadMediaFile implements Serializable {
    private String type;
    private String desc;
    private String prompt;
    private Boolean fetch;
}
