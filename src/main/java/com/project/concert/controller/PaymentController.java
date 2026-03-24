package com.project.concert.controller;

import com.project.concert.model.Payment;
import com.project.concert.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:5174")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ---------------- CREATE QR PAYMENT ----------------
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> payload) {

        try {
            // ✅ safer parsing
            if (payload.get("userId") == null ||
                    payload.get("concertId") == null ||
                    payload.get("seatIds") == null) {
                throw new RuntimeException("Missing required payment data");
            }

            Long userId = Long.valueOf(payload.get("userId").toString());
            Long concertId = Long.valueOf(payload.get("concertId").toString());

            List<Long> seatIds = ((List<?>) payload.get("seatIds"))
                    .stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .collect(Collectors.toList());

            String deliveryMethod = (String) payload.getOrDefault("deliveryMethod", "CONCERT");

            // ✅ create payment
            Payment payment = paymentService.initiatePayment(
                    userId,
                    seatIds,
                    concertId,
                    deliveryMethod,
                    true
            );

            System.out.println("[PaymentController] Payment created ID: " + payment.getId());

            // ✅ generate QR
            String qrCode = paymentService.createQrCode(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("qrCode", qrCode);
            response.put("paymentId", payment.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage() != null ? e.getMessage() : "Payment creation failed");
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
            String orderId = params.get("out_trade_no");

            if (tradeStatus == null || orderId == null) {
                System.out.println("[Notify] Missing params");
                return "fail";
            }

            if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
                paymentService.handleAlipayNotify(Long.valueOf(orderId), tradeStatus);
                System.out.println("[Notify] Payment success handled");
            }

            return "success";

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