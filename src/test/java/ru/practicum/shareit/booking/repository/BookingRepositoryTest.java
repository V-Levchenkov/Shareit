package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private Item item;
    private User owner;
    private User booker;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        owner = userRepository.save(new User(1L, "user1", "user1@mail.ru"));
        booker = userRepository.save(new User(2L, "user2", "user2@mail.ru"));
        item = itemRepository.save(new Item(1L, "item", "description",
                true, owner, null));
        booking = bookingRepository.save(new Booking(1L, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(2),
                item, booker, Status.APPROVED));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findByBooker_Id() {
        final List<Booking> bookings = bookingRepository.findByBookerId(booker.getId(),
                Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByItemOwnerId() {
        final List<Booking> bookings = bookingRepository.searchBookingByItemOwnerId(owner.getId(),
                Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus() {
        final List<Booking> bookings = bookingRepository
                .searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(booker.getId(),
                        item.getId(),
                        LocalDateTime.now(),
                        Status.APPROVED);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void searchBookingByItemOwnerIdAndStartIsAfter() {
        List<Booking> bookings = bookingRepository
                .searchBookingByItemOwnerIdAndStartIsAfter(owner.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L, LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .searchBookingByItemOwnerIdAndStartIsAfter(owner.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findByBookerIdAndStartAfter() {
        List<Booking> bookings = bookingRepository
                .findByBookerIdAndStartAfter(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findByBookerIdAndStartAfter(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByItemIdAndEndIsBeforeOrderByEndDesc() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItemIdAndEndIsBeforeOrderByEndDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItemIdAndStartIsAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository
                .findBookingsByItemIdAndStartIsAfterOrderByStartDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findBookingsByItemIdAndStartIsAfterOrderByStartDesc(item.getId(),
                        LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByBookerIdAndStatus() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByBookerIdAndStatus(booker.getId(),
                        Status.APPROVED,
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItemOwnerId() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItemOwnerId(item.getOwner().getId(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findCurrentBookingsByBookerId() {
        List<Booking> bookings = bookingRepository
                .findCurrentBookingsByBookerId(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findCurrentBookingsByBookerId(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findCurrentBookingsByItemOwnerId() {
        List<Booking> bookings = bookingRepository
                .findCurrentBookingsByItemOwnerId(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(0, bookings.size());

        final Booking booking1 = bookingRepository.save(new Booking(2L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED));
        bookings = bookingRepository
                .findCurrentBookingsByItemOwnerId(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking1, bookings.get(0));
    }

    @Test
    void findBookingsByBookerIdAndEndIsBefore() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByBookerIdAndEndIsBefore(booker.getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }

    @Test
    void findBookingsByItemOwnerIdAndEndIsBefore() {
        final List<Booking> bookings = bookingRepository
                .findBookingsByItemOwnerIdAndEndIsBefore(item.getOwner().getId(),
                        LocalDateTime.now(),
                        Pageable.unpaged());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertSame(booking, bookings.get(0));
    }
}