package com.example.interviewsitebackend.repo;


import com.example.interviewsitebackend.model.Evaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends MongoRepository<Evaluation, String> {
}

