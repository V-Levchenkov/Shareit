package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.StorageException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto findById(long userId) {
        log.info("Запрошен метод поиска запроса по userId: {}", userId);
        return userMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new StorageException("Пользователя с Id = " + userId + " нет в БД")));
    }

    @Override
    public List<UserDto> findAll() {
        log.info("Запрошен метод поиска всех пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto save(UserDto userDto) {
        log.info("Запрошен метод сохранения объекта user");
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        log.info("Запрошен метод обновления userId: {}", userId);
        UserDto oldUserDto = findById(userId);
        if (userDto.getName() != null) {
            oldUserDto.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            oldUserDto.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(oldUserDto)));
    }

    @Override
    public void deleteById(long userId) {
        log.info("Запрошен метод удаления userId: {}", userId);
        userRepository.deleteById(userId);
    }

}

