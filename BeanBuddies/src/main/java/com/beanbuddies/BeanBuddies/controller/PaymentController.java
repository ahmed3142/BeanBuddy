// src/main/java/com/beanbuddies/BeanBuddies/controller/PaymentController.java
package com.beanbuddies.BeanBuddies.controller;

import com.beanbuddies.BeanBuddies.model.User;
import com.beanbuddies.BeanBuddies.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * PROTECTED ENDPOINT
     * User ei endpoint-e call korbe payment shuru korar jonno.
     * Response-e redirectGatewayURL pabe.
     */
    @PostMapping("/initiate/{courseId}")
    public ResponseEntity<Map<String, String>> initiatePayment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user
    ) {
        String redirectUrl = paymentService.initiatePayment(courseId, user);
        return ResponseEntity.ok(Map.of("redirectGatewayURL", redirectUrl));
    }

    /**
     * PUBLIC ENDPOINT
     * SSLCommerz ei endpoint-e IPN data pathabe (server-to-server).
     * Etai payment validation korbe ebong user-ke enroll korabe.
     */
    @PostMapping(value = "/ipn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleIpn(
            @RequestParam Map<String, String> ipnData
    ) {
        // Shob IPN data ekta Map hishebe neya hocche
        System.out.println("Received IPN Data: " + ipnData);
        paymentService.validatePayment(ipnData);
        return ResponseEntity.ok().build();
    }

    /**
     * PUBLIC ENDPOINT
     * User payment shesh kore ei URL-e fire ashbe (frontend URL).
     * Frontend ei endpoint-ke call korbe status janar jonno.
     * NOTE: Amra ekhon frontend-ke redirect korchi, tai ei endpoint-gulo
     * shudhu success/fail message dibe, user ekhane direct ashbe na.
     */
    @PostMapping("/success")
    public ResponseEntity<String> paymentSuccess() {
        // Frontend ei endpoint-e ashbe na, shudhu frontend-er page-e redirect hobe
        return ResponseEntity.ok("Payment Success page (handled by frontend)");
    }

    @PostMapping("/fail")
    public ResponseEntity<String> paymentFail() {
        return ResponseEntity.ok("Payment Fail page (handled by frontend)");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("Payment Cancel page (handled by frontend)");
    }
}