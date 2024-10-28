package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Author;

import java.util.List;

public interface AuthorService {
    List<Author> getAllAuthors();
    Author getAuthorById(Integer id);
    Author getAuthorByName(String name);
    Author saveAuthor(Author author);
    Author updateAuthor(Integer id, Author author);
    void deleteAuthor(Integer id);
}
