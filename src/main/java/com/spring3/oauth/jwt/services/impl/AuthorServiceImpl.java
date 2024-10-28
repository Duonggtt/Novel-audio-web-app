package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Author;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.AuthorRepository;
import com.spring3.oauth.jwt.services.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author getAuthorById(Integer id) {
        return authorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + id));
    }

    @Override
    public Author getAuthorByName(String name) {
        Author author = authorRepository.findByName(name);
        if(author == null) {
            throw new NotFoundException("Author not found with name: " + name);
        }
        return author;
    }

    @Override
    public Author saveAuthor(Author author) {
        Author authorNew = new Author();
        authorNew.setName(author.getName());
        authorNew.setDob(author.getDob());
        return authorRepository.save(authorNew);
    }

    @Override
    public Author updateAuthor(Integer id, Author request) {
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + id));
        author.setName(request.getName());
        author.setDob(request.getDob());
        return authorRepository.save(author);
    }

    @Override
    public void deleteAuthor(Integer id) {
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + id));
        authorRepository.delete(author);
    }
}
