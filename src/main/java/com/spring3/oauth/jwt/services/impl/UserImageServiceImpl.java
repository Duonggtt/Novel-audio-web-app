package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.entity.UserImage;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.UserImageRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserImageServiceImpl {

    private final UserImageRepository userImageRepository;

    private final UserRepository userRepository;

    public List<UserImage> getFilesOfCurrentUser(Integer userId) {
        return userImageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public UserImage uploadFile(MultipartFile file,Integer userId) throws IOException {

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));
        UserImage image = new UserImage();
        image.setType(file.getContentType());
        image.setData(file.getBytes());
        image.setUser(user);
        userImageRepository.save(image);

        return image;
    }

    public UserImage getFileById(Integer id) {
        return userImageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found with id " + id));
    }

    public void deleteFile(Integer id) {
        UserImage file = getFileById(id);
        userImageRepository.delete(file);
    }
}
