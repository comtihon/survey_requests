package com.surveyor.requests.data.dao;

import com.surveyor.requests.data.entity.Survey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyDAO extends CrudRepository<Survey, String> {
}
