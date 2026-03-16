package com.project.concert.controller;

import com.project.concert.dto.PaymentRequest;
import com.project.concert.model.Payment;
import com.project.concert.service.PaymentService;
import com.alipay.api.AlipayApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:5174")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create payment and return Alipay sandbox form
     * Seats already locked by this user are allowed
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.initiatePayment(

                            request.getUserId(),
                            new ArrayList<>(request.getSeatIds()), // convert Set to List
                            request.getConcertId(),
                            request.getDeliveryMethod(),
                            true // skipLock

            );

            String formHtml = paymentService.createPaymentForm(payment);
            return ResponseEntity.ok(formHtml);
        } catch (RuntimeException | AlipayApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    /**
     * Alipay notify endpoint
     * Called by Alipay server after payment completed
     */
    @PostMapping("/notify")
    public ResponseEntity<String> handleAlipayNotify(@RequestParam Long paymentId,
                                                     @RequestParam String tradeStatus) {
        try {
            boolean success = paymentService.handleAlipayNotify(paymentId, tradeStatus);
            return ResponseEntity.ok(success ? "SUCCESS" : "FAILED");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: " + e.getMessage());
        }
    }
}