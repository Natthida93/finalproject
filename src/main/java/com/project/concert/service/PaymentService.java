package com.project.concert.service;

import com.project.concert.config.AlipayConfig;
import com.project.concert.model.*;
import com.project.concert.repository.*;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final PaymentRepository paymentRepository;

    private final AlipayClient alipayClient;

    public PaymentService(UserRepository userRepository,
                          SeatRepository seatRepository,
                          ConcertRepository concertRepository,
                          PaymentRepository paymentRepository) {

        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.paymentRepository = paymentRepository;

        // Initialize Alipay sandbox client
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

    /**
     * Initiate payment for seats already locked or available.
     * skipLock = true means seats are already locked and don't need locking again.
     */
    @Transactional
    public Payment initiatePayment(Long userId, List<Long> seatIds, Long concertId, String deliveryMethod, boolean skipLock) {
        System.out.println("[PaymentService] initiatePayment called");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("Concert not found"));

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (Seat seat : seats) {
            // Ensure all required fields exist
            if (seat.getSeatNumber() == null || seat.getPrice() == null
                    || seat.getConcert() == null || seat.getSection() == null) {
                throw new RuntimeException("Seat " + seat.getId() + " is incomplete or invalid");
            }

            // If skipping lock, assume seat is already locked by this user
            if (!skipLock) {
                if (seat.getStatus() == SeatStatus.LOCKED && !user.getId().equals(seat.getLockedById())) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
                }
                if (seat.getStatus() == SeatStatus.AVAILABLE) {
                    seat.setStatus(SeatStatus.LOCKED);
                    seat.setLockedById(user.getId());
                    seat.setLockedUntil(LocalDateTime.now().plusMinutes(10));
                    seatRepository.save(seat);
                    System.out.println("[PaymentService] Seat locked: " + seat.getSeatNumber());
                }
            }
        }

        // Calculate total price
        double totalPrice = seats.stream().mapToDouble(Seat::getPrice).sum();

        // Create Payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setConcert(concert);
        payment.setSeats(new HashSet<>(seats));
        payment.setDeliveryMethod(deliveryMethod);
        payment.setPrice(totalPrice);
        payment.setStatus(PaymentStatus.PENDING);

        // ✅ Fix: populate seatNumber to satisfy non-null DB constraint
        String seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .sorted()
                .collect(Collectors.joining(","));
        payment.setSeatNumber(seatNumbers);

        Payment savedPayment = paymentRepository.save(payment);
        System.out.println("[PaymentService] Payment created with ID: " + savedPayment.getId() + ", total: " + totalPrice);

        return savedPayment;
    }

    /**
     * Generate Alipay sandbox form for payment
     */
    public String createPaymentForm(Payment payment) throws AlipayApiException {
        System.out.println("[PaymentService] Creating Alipay form for Payment ID: " + payment.getId());

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setReturnUrl("http://localhost:5174/booking-success"); // Frontend redirect
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL); // Backend notify URL

        String bizContent = "{" +
                "\"out_trade_no\":\"" + payment.getId() + "\"," +
                "\"total_amount\":\"" + payment.getPrice() + "\"," +
                "\"subject\":\"Concert Booking #" + payment.getConcert().getTitle() + "\"," +
                "\"product_code\":\"QUICK_WAP_PAY\"" +
                "}";
        request.setBizContent(bizContent);

        String form = alipayClient.pageExecute(request).getBody();
        System.out.println("[PaymentService] Alipay form HTML generated");
        return form;
    }

    /**
     * Handle Alipay notify callback
     */
    @Transactional
    public boolean handleAlipayNotify(Long paymentId, String tradeStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Mark seats as BOOKED
            for (Seat seat : payment.getSeats()) {
                seat.setStatus(SeatStatus.BOOKED);
                seat.setLockedById(null);
                seat.setLockedUntil(null);
                seatRepository.save(seat);
                System.out.println("[PaymentService] Seat booked: " + seat.getSeatNumber());
            }

            paymentRepository.save(payment);
            System.out.println("[PaymentService] Payment marked as COMPLETED");
            return true;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            System.out.println("[PaymentService] Payment marked as FAILED");
            return false;
        }
    }
}