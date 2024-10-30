package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Hobby;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.HobbyRepository;
import com.spring3.oauth.jwt.services.HobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HobbyServiceImpl implements HobbyService {
    private final HobbyRepository hobbyRepository;

    @Override
    public List<Hobby> getAllHobbies() {
        return hobbyRepository.findAll();
    }

    @Override
    public Hobby getHobbyById(Integer id) {
        return hobbyRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Hobby not found with id: " + id));
    }

    @Override
    public Hobby getHobbyByName(String name) {
        Hobby hobby = hobbyRepository.findByName(name);
        if(hobby == null) {
            throw new NotFoundException("Hobby not found with name: " + name);
        }
        return hobby;
    }

    @Override
    public Hobby saveHobby(Hobby hobby) {
        Hobby newHobby = new Hobby();
        if (hobby.getName() == null) {
            throw new NotFoundException("Hobby name is required");
        }
        if(hobbyRepository.findByName(hobby.getName()) != null) {
            throw new NotFoundException("Hobby already exists with name: " + hobby.getName());
        }
        newHobby.setName(hobby.getName());
        return hobbyRepository.save(newHobby);
    }

    @Override
    public Hobby updateHobby(Integer id, Hobby hobby) {
        Hobby hobbyUpd = hobbyRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Hobby not found with id: " + id));
        if (hobby.getName() == null) {
            throw new NotFoundException("Hobby name is required");
        }
        if(hobbyRepository.findByName(hobby.getName()) != null) {
            throw new NotFoundException("Hobby already exists with name: " + hobby.getName());
        }
        hobbyUpd.setName(hobby.getName());
        return hobbyRepository.save(hobbyUpd);
    }
}
