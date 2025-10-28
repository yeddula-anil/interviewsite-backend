package com.example.interviewsitebackend.repo;


import com.example.interviewsitebackend.model.MeetingInviteToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MeetingInviteTokenRepository extends MongoRepository<MeetingInviteToken, String> {
    Optional<MeetingInviteToken> findByToken(String token);
}

