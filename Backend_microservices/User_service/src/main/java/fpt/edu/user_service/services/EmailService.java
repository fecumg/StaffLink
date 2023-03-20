package fpt.edu.user_service.services;

import org.springframework.stereotype.Service;

/**
 * @author Truong Duc Duong
 */

@Service
public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
}
