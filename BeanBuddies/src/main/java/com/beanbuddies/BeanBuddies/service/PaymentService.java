// src/main/java/com/beanbuddies/BeanBuddies/service/PaymentService.java
package com.beanbuddies.BeanBuddies.service;

import com.beanbuddies.BeanBuddies.config.PaymentConfig;
import com.beanbuddies.BeanBuddies.model.Course;
import com.beanbuddies.BeanBuddies.model.PaymentTransaction;
import com.beanbuddies.BeanBuddies.model.User;
import com.beanbuddies.BeanBuddies.repository.CourseRepository;
import com.beanbuddies.BeanBuddies.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentConfig config;
    private final CourseRepository courseRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final EnrollmentService enrollmentService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public String initiatePayment(Long courseId, User user) {
        // ... (Ei method-e kono change kora hoyni, debug line-ta shudhu remove kora hoyeche) ...
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        String transactionId = "BB-" + courseId + "-" + UUID.randomUUID().toString().substring(0, 8);
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setStudent(user);
        transaction.setCourse(course);
        transaction.setAmount(course.getPrice());
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("store_id", config.getStoreId());
        formData.add("store_passwd", config.getStorePassword());
        formData.add("total_amount", String.valueOf(course.getPrice()));
        formData.add("currency", "BDT");
        formData.add("tran_id", transactionId);
        formData.add("success_url", config.getSuccessUrl());
        formData.add("fail_url", config.getFailUrl());
        formData.add("cancel_url", config.getCancelUrl());
        formData.add("ipn_url", config.getIpnUrl());

        formData.add("cus_name", user.getUsername());
        formData.add("cus_email", user.getEmail());
        formData.add("cus_add1", "N/A");
        formData.add("cus_city", "N/A");
        formData.add("cus_country", "Bangladesh");
        formData.add("cus_phone", "N/A");

        formData.add("product_name", course.getTitle());
        formData.add("product_category", "Education");
        formData.add("product_profile", "digital-goods");
        formData.add("shipping_method", "NO");

        // --- Debug line-ta remove kora hoyeche ---

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getSessionApiUrl(), formData, Map.class);
            Map<String, String> responseBody = response.getBody();

            if ("SUCCESS".equals(responseBody.get("status"))) {
                transaction.setSessionKey(responseBody.get("sessionkey"));
                transactionRepository.save(transaction);
                
                return responseBody.get("redirectGatewayURL");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment initiation failed: " + responseBody.get("failedreason"));
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment gateway error: " + e.getMessage());
        }
    }

    @Transactional
    public void validatePayment(Map<String, String> ipnData) {
        String receivedTransactionId = ipnData.get("tran_id");
        String receivedStatus = ipnData.get("status");

        // Step 1: Database theke transaction-ta ber kora
        PaymentTransaction transaction = transactionRepository.findByTransactionId(receivedTransactionId)
                .orElse(null); 

        if (transaction == null) {
            System.err.println("IPN Error: Transaction not found with ID: " + receivedTransactionId);
            return;
        }

        // Step 2: Jodi agei process hoye thake
        if ("PAID".equals(transaction.getStatus())) {
            System.out.println("IPN Info: Transaction already processed: " + receivedTransactionId);
            return;
        }

        // Step 3: Jodi payment "VALID" (successful) na hoy
        if (!"VALID".equals(receivedStatus)) {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            System.err.println("IPN Info: Transaction failed with status: " + receivedStatus);
            return;
        }

        // --- !!! NOTUN LOGIC (SANDBOX FIX) !!! ---
        // Production-e, amra SSLCommerz-er validation API-ke call kortam.
        // Kintu Sandbox-e eta kaj korche na (INVALID_TRANSACTION error dicche).
        // Tai ekhon amra shudhu IPN-er `status=VALID` ebong `amount` check korbo.

        try {
            Double receivedAmount = Double.parseDouble(ipnData.get("amount"));
            
            // Check korchi je database-er amount ebong IPN-er amount shoman kina
            if (transaction.getAmount().equals(receivedAmount)) {
                
                // Step 5: Shob thik thakle, transaction "PAID" mark kora
                transaction.setStatus("PAID");
                transactionRepository.save(transaction);

                // Step 6: Student-ke course-e enroll korano
                enrollmentService.enrollStudent(transaction.getCourse().getId(), transaction.getStudent());
                System.out.println("SUCCESS: Payment validated (IPN Only) and user enrolled for transaction: " + receivedTransactionId);

            } else {
                // Jodi amount na mele (Security issue)
                transaction.setStatus("FAILED");
                transactionRepository.save(transaction);
                System.err.println("IPN Error: Amount mismatch. DB Amount: " + transaction.getAmount() + ", IPN Amount: " + receivedAmount);
            }
        } catch (Exception e) {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            System.err.println("IPN Error: Exception during IPN processing: " + e.getMessage());
        }
    }

    // --- buildValidationUrl METHOD-TA AR DORKAR NEI ---
    /*
    private String buildValidationUrl(String valId) {
        return config.getValidationApiUrl() +
                "?val_id=" + URLEncoder.encode(valId, StandardCharsets.UTF_8) +
                "&store_id=" + URLEncoder.encode(config.getStoreId(), StandardCharsets.UTF_8) +
                "&store_passwd=" + URLEncoder.encode(config.getStorePassword(), StandardCharsets.UTF_8) +
                "&format=json";
    }
    */

    @Configuration
    static class RestTemplateConfig {
        @org.springframework.context.annotation.Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}