package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Hobby;

import java.util.List;

public interface HobbyService {
    List<Hobby> getAllHobbies();
    Hobby getHobbyById(Integer id);
    Hobby getHobbyByName(String name);
    Hobby saveHobby(Hobby hobby);
    Hobby updateHobby(Integer id, Hobby hobby);
}
