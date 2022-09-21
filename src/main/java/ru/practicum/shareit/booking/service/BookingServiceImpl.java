package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ItemException;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository, BookingMapper mapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        log.info("Поиск по bookingId: " + bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new StorageException("Бронирования с Id = " + bookingId + " нет в БД"));
        if (booking.getBooker().getId() != userId
                && booking.getItem().getOwner().getId() != userId) {
            log.error("Incorrect userId" + userId);
            throw new StorageException("Incorrect userId");
        }
        return mapper.toBookingDto(booking);

    }

    @Override
    public List<BookingDto> findAll(long userId, String state, int from, int size) {
        log.info("Запрос на поиск всех аренд");
        userRepository.findById(userId).orElseThrow(() -> new StorageException("Incorrect userId"));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());

        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByBookerId(userId, pageable)
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByBookerId(userId, LocalDateTime.now(),
                            pageable).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByBookerIdAndEndIsBefore(userId, LocalDateTime.now(),
                            pageable).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.findByBookerIdAndStartAfter(userId, LocalDateTime.now(), pageable)
                            .stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByBookerIdAndStatus(userId,
                                    Status.WAITING, pageable)
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByBookerIdAndStatus(userId,
                                    Status.REJECTED, pageable)
                            .stream()
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                default:
                    log.warn("Unknown state: UNSUPPORTED_STATUS");
                    throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
            }
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: UNSUPPORTED_STATUS");
            throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public BookingDto save(BookingDtoSimple bookingDtoSimple, long userId) {
        log.info("Сохранение для userId:" + userId);
        if (bookingDtoSimple.getEnd().isBefore(bookingDtoSimple.getStart())) {
            log.error("Incorrect end time");
            throw new BookingException("Incorrect end time");
        }
        Booking booking = mapper.fromSimpleToBooking(bookingDtoSimple);
        booking.setBooker(userRepository.findById(userId).orElseThrow());
        Item item = itemRepository.findById(bookingDtoSimple.getItemId())
                .orElseThrow(() -> new StorageException("Вещи с Id = "
                        + bookingDtoSimple.getItemId() + " нет в базе данных"));
        if (!item.getAvailable()) {
            log.warn("Вещь с Id = " + bookingDtoSimple.getItemId() +
                    " не доступна для аренды");
            throw new ItemException("Вещь с Id = " + bookingDtoSimple.getItemId() +
                    " не доступна для аренды");
        }
        if (item.getOwner().getId() == userId) {
            log.warn("Владелец вещи: " + userId + " не может забронировать свою вещь");
            throw new StorageException("Владелец вещи не может забронировать свою вещь");
        } else {
            booking.setItem(item);
            return mapper.toBookingDto(bookingRepository.save(booking));
        }
    }

    @Override
    public BookingDto update(long bookingId, BookingDto bookingDto) {
        log.info("Запрос на обновление вещи: " + bookingId);
        BookingDto oldBookingDto = mapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow());
        if (bookingDto.getStart() != null) {
            oldBookingDto.setStart(bookingDto.getStart());
        }
        if (bookingDto.getEnd() != null) {
            oldBookingDto.setEnd(bookingDto.getEnd());
        }
        if (bookingDto.getItem() != null) {
            oldBookingDto.setItem(bookingDto.getItem());
        }
        if (bookingDto.getBooker() != null) {
            oldBookingDto.setBooker(bookingDto.getBooker());
        }
        if (bookingDto.getStatus() != null) {
            oldBookingDto.setStatus(bookingDto.getStatus());
        }
        return mapper.toBookingDto(bookingRepository.save(mapper.toBooking(oldBookingDto)));
    }

    @Override
    public void deleteById(long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingDto approve(long userId, long bookingId, Boolean approved) {
        log.info("Подтверждение аренды для вещи: " + bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        if (booking.getItem().getOwner().getId() != userId) {
            log.error("Подтвердить бронирование может только владелец вещи");
            throw new StorageException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus().equals(Status.APPROVED)) {
            log.warn("Бронирование уже подтверждено");
            throw new BookingException("Бронирование уже подтверждено");
        }
        if (approved == null) {
            log.warn("Необходимо указать approved");
            throw new BookingException("Необходимо указать approved");
        } else if (approved) {
            booking.setStatus(Status.APPROVED);
            return mapper.toBookingDto(bookingRepository.save(booking));
        } else {
            booking.setStatus(Status.REJECTED);
            return mapper.toBookingDto(bookingRepository.save(booking));
        }
    }

    @Override
    public List<BookingDto> findAllByItemOwnerId(long userId, String state, int from, int size) {
        log.info("Запрошен поиск по вещи и владельцу: " + userId);
        userRepository.findById(userId).orElseThrow(() -> new StorageException("Incorrect userId"));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());
        List<BookingDto> result = bookingRepository.searchBookingByItemOwnerId(userId, pageable).stream()
                .map(mapper::toBookingDto).collect(Collectors.toList());
        if (result.isEmpty()) {
            log.error("У пользователя " + userId + " нет вещей");
            throw new StorageException("У пользователя нет вещей");
        }
        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    return result;
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByItemOwnerId(userId, LocalDateTime.now(),
                            pageable).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByItemOwnerIdAndEndIsBefore(userId, LocalDateTime.now(),
                            pageable).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.searchBookingByItemOwnerIdAndStartIsAfter(userId, LocalDateTime.now(),
                            pageable).stream().map(mapper::toBookingDto).collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByItemOwnerId(userId, pageable).stream().skip(from)
                            .filter(booking -> booking.getStatus().equals(Status.WAITING))
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByItemOwnerId(userId, pageable).stream().skip(from)
                            .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                            .map(mapper::toBookingDto).collect(Collectors.toList());
                default:
                    log.error("Unknown state: UNSUPPORTED_STATUS");
                    throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
            }
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: UNSUPPORTED_STATUS");
            throw new BookingException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

}
