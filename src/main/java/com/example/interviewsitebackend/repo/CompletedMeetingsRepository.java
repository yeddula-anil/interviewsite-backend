package com.example.interviewsitebackend.repo;

import com.example.interviewsitebackend.model.CompletedMeetings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedMeetingsRepository extends MongoRepository<CompletedMeetings, String> {
    List<CompletedMeetings> findByRecruiterEmail(String userId);
    Optional<CompletedMeetings> findByMeetingId(String meetingId);
    List<CompletedMeetings> findByCandidateEmail(String userId);
}

