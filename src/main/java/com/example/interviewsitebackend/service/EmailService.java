package com.example.interviewsitebackend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 🧠 Reuse this for any email
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("📧 Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // 🕐 New method: send meeting reminder email
    public void sendMeetingReminder(String to, String role, String date, String time, String companyName) {
        String subject = "⏰ Interview Reminder — " + companyName + " (" + role + ")";
        String body = """
            <div style='font-family:Arial;line-height:1.6'>
              <h2 style='color:#16a085'>Interview Reminder</h2>
              <p>This is a reminder for your upcoming interview with <b>%s</b> for the role <b>%s</b>.</p>
              <p><b>Scheduled Time:</b> %s at %s</p>
              <p>Please ensure you are ready 10 minutes before the scheduled time.</p>
              <hr>
              <p style='font-size:12px;color:gray'>This is an automated message from the Interview Platform.</p>
            </div>
        """.formatted(companyName, role, date, time);

        sendEmail(to, subject, body);
    }
}

