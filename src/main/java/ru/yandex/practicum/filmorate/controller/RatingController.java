package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService service;

    @GetMapping
    public Collection<MpaRating> getAllRatings() {
        log.info("Запрошен полный список рейтингов.");
        return service.getRatingList();
    }

    @GetMapping("/{id}")
    public MpaRating getRatingById(@PathVariable int id) {
        log.info("Запрошена информация о рейтинге с id={}", id);
        return service.findRatingById(id);
    }
}