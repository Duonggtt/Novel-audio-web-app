package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Genre;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.GenreRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.services.GenreService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final NovelRepository novelRepository;

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Override
    public Genre getGenreById(Integer id) {
        return genreRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Genre not found with id: " + id));
    }

    @Override
    public Genre getGenreByName(String name) {
        Genre genre = genreRepository.findByName(name);
        if(genre == null) {
            throw new NotFoundException("Genre not found with name: " + name);
        }
        return genre;
    }

    @Override
    public Genre saveGenre(Genre genre) {
        Genre newGenre = new Genre();
        newGenre.setName(genre.getName());
        return genreRepository.save(newGenre);
    }

    @Override
    public Genre updateGenre(Integer id, Genre genre) {
        Genre genreUpd = genreRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Genre not found with id: " + id));
        genreUpd.setName(genre.getName());
        return genreRepository.save(genreUpd);
    }

    @Override
    public void deleteGenre(Integer id) {
        Genre genre = genreRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Genre not found with id: " + id));
        List<Novel> novels = novelRepository.findAllByGenreId(id);
        for(Novel novel : novels) {
            novel.getGenres().remove(genre);
        }
        genreRepository.delete(genre);
    }
}
