package com.example.interviewsitebackend.model;

import com.example.interviewsitebackend.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder // ✅ Enables User.builder()
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    @JsonIgnore
    private String password;

    private Role role; // enum for role (CANDIDATE, RECRUITER, ADMIN, etc.)

    private String profilePicUrl; // for both roles
    private String bannerUrl;     // only for recruiters


}
