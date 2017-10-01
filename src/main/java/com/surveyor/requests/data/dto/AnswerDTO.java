package com.surveyor.requests.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AnswerDTO {
    @NotEmpty
    @JsonProperty("id")
    private String answerId;

    public AnswerDTO() {
    }

    public AnswerDTO(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerId() {
        return answerId;
    }

    @Override
    public String toString() {
        return "AnswerDTO{" +
                "answerId='" + answerId + '\'' +
                '}';
    }
}
