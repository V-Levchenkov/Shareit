package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    ItemDtoWithBooking findById(long itemId, long userId);

    List<ItemDtoWithBooking> findAll(long userId, int from, int size);

    ItemDto save(long userId, ItemDto itemDto);

    ItemDto update(long userId, long id, ItemDto itemDto);

    void deleteById(long itemId);

    List<ItemDto> searchItem(String text, int from, int size);

    CommentDto saveComment(long userId, long itemId, CommentDto commentDto);
}
