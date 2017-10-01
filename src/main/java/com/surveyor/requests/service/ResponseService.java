package com.surveyor.requests.service;

import com.surveyor.requests.config.KafkaProducerConfig;
import com.surveyor.requests.data.dao.SurveyDAOService;
import com.surveyor.requests.data.dto.AnswerDTO;
import com.surveyor.requests.data.dto.QuestionDTO;
import com.surveyor.requests.data.dto.ResponseDTO;
import com.surveyor.requests.data.entity.Answer;
import com.surveyor.requests.data.entity.Question;
import com.surveyor.requests.data.entity.Survey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ResponseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseService.class);

    @Autowired
    private SurveyDAOService service;

    @Autowired
    private KafkaTemplate<String, QuestionDTO> template;

    @Autowired
    private KafkaProducerConfig config;

    @Async
    @Transactional
    public CompletableFuture<ResponseDTO<?>> respond(String surveyId, List<AnswerDTO> answers) {
        Optional<Survey> find = service.findOne(surveyId);
        if (!find.isPresent()) {
            ResponseDTO<String> responseDTO = new ResponseDTO<>(false, "No such survey!");
            return CompletableFuture.completedFuture(responseDTO);
        }
        Survey survey = find.get();
        Map<String, AnswerDTO> results =
                answers.stream().collect(Collectors.toMap(AnswerDTO::getAnswerId, c -> c));
        List<String> unanswered = checkResponse(survey, results);
        ResponseDTO<List> responseDTO = new ResponseDTO<>(true, unanswered);
        return CompletableFuture.completedFuture(responseDTO);
    }

    /**
     * Answer questions, send answers to kafka survey's country_code topic.
     *
     * @param survey  survey to be answered
     * @param answers list of answeres
     * @return list of unanswered question ids
     */
    private List<String> checkResponse(Survey survey, Map<String, AnswerDTO> answers) {
        List<String> unanswered = new ArrayList<>();
        ListIterator<Question> iter = survey.getQuestions().listIterator();
        while (iter.hasNext()) { //remove answered questions to avoid multiple response for one
            Question toBeAnswered = iter.next();
            QuestionDTO questionDTO = new QuestionDTO(toBeAnswered.getId());
            answer(questionDTO, toBeAnswered.getAnswers(), answers);
            if (!questionDTO.isAnswered())
                unanswered.add(toBeAnswered.getId());
            else {
                LOGGER.debug("send {} to {}", questionDTO, config.getKafkaTopic());
                template.send(config.getKafkaTopic(), questionDTO);
            }
            iter.remove();
        }
        return unanswered;
    }

    private void answer(QuestionDTO questionDTO,
                        List<Answer> possibleAnswers,
                        Map<String, AnswerDTO> answers) {
        for (Answer a : possibleAnswers) {
            if (answers.containsKey(a.getId())) {
                questionDTO.answer(a.getId());
                break; //Question answered. No more attempts for it.
            }
        }
    }
}
