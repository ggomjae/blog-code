package com.jojoldu.blogcode.springboot.tips.validatemessage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by jojoldu@gmail.com on 19/11/2019
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */
@ToString
@Getter
@NoArgsConstructor
public class ValidateMessageRequestDto {
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate requestDate;

    public ValidateMessageRequestDto(String content, LocalDate requestDate) {
        this.content = content;
        this.requestDate = requestDate;
    }
}
