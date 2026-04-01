package com.project.concert.controller;

import com.alipay.api.AlipayApiException;
import com.project.concert.model.Payment;
import com.project.concert.service.PaymentService;
import com.project.concert.repository.PaymentRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payment")
@CrossOrigin(
        origins = "https://concertticketingsystem.netlify.app",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService,
                             PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    // ---------------- CREATE PAYMENT + QR ----------------
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> payload) throws AlipayApiException {
        Long userId = Long.valueOf(payload.get("userId").toString());
        List<Long> seatIds = ((List<?>) payload.get("seatIds"))
                .stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());

        // Check if there is already a pending or completed payment for the same user + seats
        Optional<Payment> existingPayment = paymentRepository
                .findPendingOrCompletedByUserAndSeats(userId, seatIds);

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            return ResponseEntity.ok(Map.of(
                    "paymentId", payment.getId(),
                    "qrCode", payment.getQrCode(),
                    "status", payment.getStatus().name()
            ));
        }

        // Otherwise, create new payment as usual
        Payment payment = paymentService.initiatePayment(
                userId,
                seatIds,
                Long.valueOf(payload.get("concertId").toString()),
                (String) payload.getOrDefault("deliveryMethod", "CONCERT"),
                (String) payload.getOrDefault("shippingAddress", null),
                true
        );

        String qrCode = paymentService.getOrCreateQrCode(payment);

        return ResponseEntity.ok(Map.of(
                "qrCode", qrCode,
                "paymentId", payment.getId(),
                "status", payment.getStatus().name()
        ));
    }
    // ---------------- REFRESH EXISTING PAYMENT QR ----------------
    @PostMapping("/refresh/{paymentId}")
    public ResponseEntity<?> refreshQr(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            String qrCode = paymentService.getOrCreateQrCode(payment);

            return ResponseEntity.ok(Map.of(
                    "qrCode", qrCode,
                    "paymentId", payment.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage() != null ? e.getMessage() : "QR refresh failed");
        }
    }

    // ---------------- ALIPAY NOTIFY ----------------
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        System.out.println("[Alipay Notify] params: " + params);

        try {
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no"); // ⚡ important

            if (tradeStatus == null || outTradeNo == null) return "fail";

            // Find payment by outTradeNo instead of DB id
            Payment payment = paymentRepository.findByOutTradeNo(outTradeNo)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            boolean success = paymentService.handleAlipayNotify(payment.getId(), tradeStatus);

            return success ? "success" : "fail";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // ---------------- CHECK PAYMENT STATUS ----------------
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long paymentId) {
        String status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }
}