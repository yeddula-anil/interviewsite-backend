package com.example.interviewsitebackend.dto;

public class SignalMessage {
    private String type;      // "offer" | "answer" | "candidate" | "chat"
    private Object data;      // sdp object / candidate object / chat text
    private String sender;    // human-readable name e.g. "Recruiter John"
    private String role;      // optional: "recruiter" or "candidate"

    public SignalMessage() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
