package com.surveyor.requests.data.dto;

public class QuestionDTO {
    private String questionId;

    private String answerId;

    public QuestionDTO(String id) {
        this.questionId = id;
    }

    public QuestionDTO() {
    }

    public void answer(String answerId) {
        this.answerId = answerId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getAnswerId() {
        return answerId;
    }

    @Override
    public String toString() {
        return "QuestionDTO{" +
                "questionId='" + questionId + '\'' +
                ", answerId='" + answerId + '\'' +
                '}';
    }
}
