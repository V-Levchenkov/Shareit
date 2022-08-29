package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.storage.ItemStorage;

@Service
public class ItemServiceImpl {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage)(
            this.itemStorage = itemStorage;
            this.userStorage = userStorage;
            )
}
