package com.minzetsu.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Value("${spring.mail.username:no-reply@local}")
    private String from;

    public void sendOtp(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        applyFromIfValid(msg);
        msg.setTo(requireValidEmail(to, "Recipient email is invalid"));
        msg.setSubject("Verify your account");
        msg.setText("Your verification code is: " + code + "\nCode expires in 1 minute.");
        mailSender.send(msg);
    }

    public void sendGroupInviteMail(String to, String groupName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        applyFromIfValid(msg);
        msg.setTo(requireValidEmail(to, "Recipient email is invalid"));
        msg.setSubject("Group invitation");
        msg.setText("You were invited to join group: " + groupName + ". Please open app notifications to accept.");
        mailSender.send(msg);
    }

    public void sendInviteAcceptedMail(String to, String groupName, String memberEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        applyFromIfValid(msg);
        msg.setTo(requireValidEmail(to, "Recipient email is invalid"));
        msg.setSubject("Invitation accepted");
        msg.setText(memberEmail + " accepted invitation to group: " + groupName);
        mailSender.send(msg);
    }

    private void applyFromIfValid(SimpleMailMessage msg) {
        if (from == null || from.isBlank()) {
            return;
        }
        String normalizedFrom = from.trim();
        if (!SIMPLE_EMAIL_PATTERN.matcher(normalizedFrom).matches()) {
            throw new IllegalArgumentException("Mail sender address is invalid. Please set spring.mail.username to a valid email.");
        }
        msg.setFrom(normalizedFrom);
    }

    private String requireValidEmail(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (!SIMPLE_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
