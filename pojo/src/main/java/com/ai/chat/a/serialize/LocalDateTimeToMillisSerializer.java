package com.ai.chat.a.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeToMillisSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException {
        ZonedDateTime zdt = value.atZone(ZoneId.systemDefault());
        gen.writeNumber(zdt.toInstant().toEpochMilli());
    }
}
