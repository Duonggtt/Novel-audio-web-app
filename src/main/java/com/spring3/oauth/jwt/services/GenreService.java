package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> getAllGenres();
    Genre getGenreById(Integer id);
    Genre getGenreByName(String name);
    Genre saveGenre(Genre genre);
    Genre updateGenre(Integer id, Genre genre);
    void deleteGenre(Integer id);
}
