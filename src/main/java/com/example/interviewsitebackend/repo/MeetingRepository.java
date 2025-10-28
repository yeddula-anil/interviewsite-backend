package com.example.interviewsitebackend.repo;

import com.example.interviewsitebackend.model.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MeetingRepository extends MongoRepository<Meeting, String> {

    List<Meeting> findByCandidateEmail(String candidateEmail);

    List<Meeting> findByRecruiterEmail(String recruiterEmail);
}
