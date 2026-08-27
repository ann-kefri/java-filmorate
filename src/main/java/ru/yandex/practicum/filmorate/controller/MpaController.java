package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaRatingDbStorage mpaRatingStorage;

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaRatingStorage.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга MPA с id: {}", id);
        return mpaRatingStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Рейтинг MPA с id {} не найден", id);
                    return new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг MPA с id " + id + " не найден");
                });
    }
}