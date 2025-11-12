package com.example.interviewsitebackend.repo;

import com.example.interviewsitebackend.model.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends MongoRepository<Meeting, String> {

    List<Meeting> findByCandidateEmail(String candidateEmail);

    List<Meeting> findByRecruiterEmail(String recruiterEmail);


    Optional<Meeting> findById(String s);

    // We’ll fetch all meetings scheduled for today for simplicity.
    @Query("{ 'date': ?0 }")
    List<Meeting> findMeetingsByDate(String date);
}
