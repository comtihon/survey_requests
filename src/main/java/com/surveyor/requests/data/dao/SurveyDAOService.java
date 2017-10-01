package com.surveyor.requests.data.dao;

import com.surveyor.requests.data.entity.Survey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SurveyDAOService {
    @Autowired
    private SurveyDAO surveyDAO;

    public Optional<Survey> findOne(String id) {
        return Optional.ofNullable(surveyDAO.findOne(id));
    }

    public List<Survey> findAll() {
        Iterable<Survey> itr = surveyDAO.findAll();
        return new ArrayList<>((Collection<Survey>) itr);
    }
}
