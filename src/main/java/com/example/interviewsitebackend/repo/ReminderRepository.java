package com.example.interviewsitebackend.repo;


import com.example.interviewsitebackend.model.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    Optional<Reminder> findByMeetingId(String meetingId);
}

