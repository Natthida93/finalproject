package com.project.concert.service;

import com.project.concert.config.AlipayConfig;
import com.project.concert.model.*;
import com.project.concert.repository.*;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    private final AlipayClient alipayClient;

    public PaymentService(UserRepository userRepository,
                          SeatRepository seatRepository,
                          ConcertRepository concertRepository,
                          PaymentRepository paymentRepository,
                          BookingRepository bookingRepository) {

        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;

        this.alipayClient = new DefaultAlipayClient(
                AlipayConfig.GATEWAY_URL,
                AlipayConfig.APP_ID,
                AlipayConfig.MERCHANT_PRIVATE_KEY,
                AlipayConfig.FORMAT,
                AlipayConfig.CHARSET,
                AlipayConfig.ALIPAY_PUBLIC_KEY,
                AlipayConfig.SIGN_TYPE
        );
    }

    // ---------------- INITIATE PAYMENT ----------------
    @Transactional
    public Payment initiatePayment(Long userId,
                                   List<Long> seatIds,
                                   Long concertId,
                                   String deliveryMethod,
                                   String shippingAddress,
                                   boolean lockSeats) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("Concert not found"));

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        BigDecimal seatTotal = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = "SHIPPED".equalsIgnoreCase(deliveryMethod)
                ? BigDecimal.valueOf(50)
                : BigDecimal.ZERO;

        BigDecimal totalPrice = seatTotal.add(shippingFee);

        String seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .sorted()
                .collect(Collectors.joining(","));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setConcert(concert);
        payment.setSeats(new HashSet<>(seats));
        payment.setPrice(totalPrice);
        payment.setSeatNumber(seatNumbers);
        payment.setDeliveryMethod(deliveryMethod);
        payment.setShippingAddress(
                "SHIPPED".equalsIgnoreCase(deliveryMethod) ? shippingAddress : null
        );
        payment.setUserName(user.getFullName());
        payment.setUserEmail(user.getEmail());
        payment.setConcertTitle(concert.getTitle());
        payment.setStatus(PaymentStatus.PENDING);

        // ⚡ NEW: Generate unique trade number (outTradeNo)
        payment.setOutTradeNo(generateOutTradeNo());

        if (lockSeats) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(5);
            for (Seat seat : seats) {
                seat.setLockedById(user.getId());
                seat.setLockedUntil(lockUntil);
                seatRepository.save(seat);
            }
        }

        return paymentRepository.save(payment);
    }

    // ---------------- GENERATE UNIQUE TRADE NO ----------------
    private String generateOutTradeNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    // ---------------- GET OR CREATE QR ----------------
    @Transactional
    public String getOrCreateQrCode(Payment payment) throws AlipayApiException {

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment already completed");
        }

        if (payment.getQrCode() != null) {
            return payment.getQrCode();
        }

        String qrCode = createQrCode(payment);
        payment.setQrCode(qrCode);
        paymentRepository.save(payment);

        return qrCode;
    }

    // ---------------- CREATE QR ----------------
    @Transactional(readOnly = true)
    public String createQrCode(Payment payment) throws AlipayApiException {

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        String totalAmount = payment.getPrice()
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        String bizContent = "{"
                + "\"out_trade_no\":\"" + payment.getOutTradeNo() + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"Concert Booking #" + payment.getConcertTitle() + "\","
                + "\"store_id\":\"STORE001\","
                + "\"product_code\":\"FACE_TO_FACE_PAYMENT\","
                + "\"timeout_express\":\"5m\""
                + "}";

        request.setBizContent(bizContent);

        System.out.println("=== ALIPAY REQUEST ===");
        System.out.println("Trade No: " + payment.getOutTradeNo());
        System.out.println("Total Amount: " + totalAmount);

        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            return response.getQrCode();
        } else {
            throw new RuntimeException("QR creation failed: " + response.getSubMsg());
        }
    }

    // ---------------- HANDLE NOTIFY ----------------
    @Transactional
    public boolean handleAlipayNotify(Long paymentId, String tradeStatus) {

        Payment payment = paymentRepository
                .findByIdWithConcertAndSeats(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) return true;

        if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            for (Seat seat : payment.getSeats()) {
                seat.setStatus(SeatStatus.BOOKED);
                seat.setLockedById(null);
                seat.setLockedUntil(null);
                seatRepository.save(seat);
            }

            Booking booking = new Booking();
            booking.setUser(payment.getUser());
            booking.setConcert(payment.getConcert());
            booking.setSeats(new HashSet<>(payment.getSeats()));
            booking.setPayment(payment);
            booking.setBookedAt(LocalDateTime.now());
            booking.setDeliveryMethod(payment.getDeliveryMethod());

            bookingRepository.save(booking);
            paymentRepository.save(payment);

            return true;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        return false;
    }

    // ---------------- GET STATUS ----------------
    @Transactional(readOnly = true)
    public String getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository
                .findByIdWithConcertAndSeats(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return payment.getStatus().name();
    }
}