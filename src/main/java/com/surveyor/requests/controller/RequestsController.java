package com.surveyor.requests.controller;

import com.surveyor.requests.data.dto.AnswerDTO;
import com.surveyor.requests.data.dto.ResponseDTO;
import com.surveyor.requests.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class RequestsController {

    private static final MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    @Autowired
    private ResponseService responseService;

    @RequestMapping(path = "/respond/{survey}", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> respond(
            @PathVariable("survey") String id, @Valid @RequestBody List<AnswerDTO> answers) {
        CompletableFuture<ResponseDTO<?>> respond = responseService.respond(id, answers);
        return respond.thenApply(this::returnResult);
    }

    private ResponseEntity<ResponseDTO<?>> returnResult(ResponseDTO<?> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CONTENT_TYPE);
        if (result.isResult())
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        return new ResponseEntity<>(result, headers, HttpStatus.BAD_REQUEST);
    }
}
