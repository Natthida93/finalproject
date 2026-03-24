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

    // ------------------------
    // Initiate Payment (UNCHANGED except bug fix)
    // ------------------------
    @Transactional
    public Payment initiatePayment(Long userId,
                                   List<Long> seatIds,
                                   Long concertId,
                                   String deliveryMethod,
                                   boolean skipLock) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("Concert not found"));

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (Seat seat : seats) {

            if (!skipLock) {

                boolean expired = seat.getLockedUntil() == null
                        || seat.getLockedUntil().isBefore(LocalDateTime.now());

                if (seat.getStatus() == SeatStatus.LOCKED
                        && !expired
                        && !user.getId().equals(seat.getLockedById())) {

                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
                }

                if (seat.getStatus() == SeatStatus.AVAILABLE || expired) {

                    seat.setStatus(SeatStatus.LOCKED);
                    seat.setLockedById(user.getId());
                    seat.setLockedUntil(LocalDateTime.now().plusMinutes(10));

                    seatRepository.save(seat);
                }
            }
        }

        double totalPrice = seats.stream()
                .mapToDouble(Seat::getPrice)
                .sum();

        String seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .sorted()
                .collect(Collectors.joining(","));

        Optional<Payment> existingPayment =
                paymentRepository.findPendingPayment(
                        user.getId(),
                        concert.getId(),
                        seatNumbers
                );

        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setConcert(concert);
        payment.setSeats(new HashSet<>(seats));
        payment.setDeliveryMethod(deliveryMethod);
        payment.setPrice(totalPrice);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSeatNumber(seatNumbers);


        return paymentRepository.save(payment);
    }



    public String createQrCode(Payment payment) throws AlipayApiException {

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        String bizContent = "{"
                + "\"out_trade_no\":\"" + payment.getId() + "\","
                + "\"total_amount\":\"" + String.format("%.2f", payment.getPrice()) + "\","
                + "\"subject\":\"Concert Booking #" + payment.getConcert().getTitle() + "\","
                + "\"store_id\":\"STORE001\","

                // 🔥 CRITICAL FIXES
                + "\"product_code\":\"FACE_TO_FACE_PAYMENT\","
                + "\"timeout_express\":\"5m\""

                + "}";

        System.out.println("Sending to Alipay: " + bizContent);

        request.setBizContent(bizContent);

        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            System.out.println("✅ QR Code created: " + response.getQrCode());
            return response.getQrCode();
        } else {
            System.out.println("❌ Alipay error: " + response.getSubMsg());
            throw new RuntimeException("QR creation failed: " + response.getSubMsg());
        }
    }



    // Handle Notify

    @Transactional
    public boolean handleAlipayNotify(Long paymentId, String tradeStatus) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return true;
        }

        if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            Set<Seat> seats = payment.getSeats();

            for (Seat seat : seats) {

                seat.setStatus(SeatStatus.BOOKED);
                seat.setLockedById(null);
                seat.setLockedUntil(null);

                seatRepository.save(seat);
            }

            Booking booking = new Booking();

            booking.setUser(payment.getUser());
            booking.setConcert(payment.getConcert());
            booking.setSeats(new HashSet<>(seats));
            booking.setPayment(payment);
            booking.setBookedAt(LocalDateTime.now());

            bookingRepository.save(booking);

            paymentRepository.save(payment);

            return true;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        return false;
    }


    // Get Pending

    @Transactional(readOnly = true)
    public List<Payment> getPendingPayments(Long userId, Long concertId) {
        return paymentRepository.findPendingPaymentsWithSeats(userId, concertId);
    }



    // Get Status

    @Transactional(readOnly = true)
    public String getPaymentStatus(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return payment.getStatus().name();
    }
}