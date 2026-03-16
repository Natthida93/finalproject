package com.project.concert;

import com.project.concert.model.*;
import com.project.concert.repository.BookingRepository;
import com.project.concert.service.BookingService;
import com.project.concert.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private SeatService seatService;
    private BookingService bookingService;

    private User user;
    private Concert concert;
    private Section section;
    private Seat seat;

    @BeforeEach
    void setUp() {
        // Mock dependencies
        bookingRepository = mock(BookingRepository.class);
        seatService = mock(SeatService.class);

        // Service under test
        bookingService = new BookingService(bookingRepository, seatService);

        // Entities setup
        concert = new Concert();
        concert.setTitle("Marktuan Concert");
        concert.setDate(LocalDate.now().plusDays(1));

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        section = new Section();
        section.setName("VIP");
        section.setConcert(concert);

        seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setPrice(100.0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setConcert(concert);
        seat.setSection(section);

        // Mocks
        doNothing().when(seatService).saveSeat(any(Seat.class));
        when(seatService.getSeatById(anyLong())).thenReturn(seat);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createBooking_success_locksSeat() {
        Booking booking = bookingService.createBooking(user, seat);

        assertNotNull(booking);
        assertEquals("PENDING", booking.getStatus());
        assertEquals(seat.getId(), booking.getSeatId());
        assertEquals(seat.getSeatNumber(), booking.getSeatNumber());
        assertEquals(section.getName(), booking.getZoneName());
        assertEquals(SeatStatus.LOCKED, seat.getStatus());
        assertEquals(user.getId(), seat.getLockedById());
        assertNotNull(seat.getLockedUntil());
    }

    @Test
    void confirmBooking_marksSeatBooked() {
        Booking booking = bookingService.createBooking(user, seat);
        Booking confirmed = bookingService.confirmBooking(booking);

        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertNull(seat.getLockedById());
        assertNull(seat.getLockedUntil());
    }

    @Test
    void cancelBooking_releasesSeat() {
        Booking booking = bookingService.createBooking(user, seat);
        bookingService.cancelBooking(booking);

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getLockedById());
        assertNull(seat.getLockedUntil());
        verify(bookingRepository).delete(booking);
    }
}