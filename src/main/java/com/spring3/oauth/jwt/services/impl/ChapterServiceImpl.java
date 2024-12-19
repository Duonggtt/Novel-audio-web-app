package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Chapter;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.*;
import com.spring3.oauth.jwt.models.request.UpsertChapterRequest;
import com.spring3.oauth.jwt.repositories.ChapterRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.services.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        // Validate file
        validateImageFile(file);

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + "." + fileExtension;

        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get("/www/wwwroot/Audio/photo").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Save file to server
        Path targetLocation = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Construct URL path
        String fileUrl = "/Audio/photo/" + uniqueFilename;
        return fileUrl;
    }

    private void validateImageFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        // Check file size (e.g., max 5MB)
        long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum limit of 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    @Override
    public List<ChapterResponseDTO> getAllChaptersInNovel(String slug) {
        if(chapterRepository.findAllByNovelSlug(slug).isEmpty()) {
            throw new NotFoundException("Chapter not found in novel with slug : " + slug);
        }
        return chapterRepository.findAllByNovelSlug(slug)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Override
    public PagedChapterResponseDTO getAllChapterInNovelBySlug(String slug, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findAllByNovelSlugPage(slug, pageable);
        if(chapters.isEmpty()) {
            throw new NotFoundException("Chapter not found in novel with slug : " + slug);
        }
        // Mapping từ Chapter sang ChapterResponseDTO
        List<ChapterResponseDTO> chapterDTOs = chapters.stream()
            .map(this::convertToDTOForPage)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(chapters.getNumber(), chapters.getSize(), chapters.getTotalElements());
        return new PagedChapterResponseDTO(chapterDTOs, pagination);
    }

    @Override
    public ChapterResponseDTO getChapterByChapNoInNovel(String slug, int chapNo) {
        if(chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug) == null) {
            throw new NotFoundException("Chapter not found with chapter number: " + chapNo + " with novel slug: " + slug);
        }
        return convertToDTO(chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug));
    }

    @Override
    public ChapterResponseDTO saveChapter(UpsertChapterRequest request) {
        Novel novel = novelRepository.findBySlug(request.getSlug());
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug: " + request.getSlug());
        }
        novel.setTotalChapters(novel.getTotalChapters() + 1);
        novelRepository.save(novel);

        Chapter chapter = new Chapter();
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle());
        chapter.setReleasedAt(LocalDateTime.now());
        chapter.setContentDoc(request.getContentDoc());
        chapter.setThumbnailImageUrl(request.getThumbnailImageUrl());
        chapter.setNovel(novel);
        return convertToDTO(chapterRepository.save(chapter));
    }

    @Override
    public ChapterResponseDTO updateChapter(int chapNo, String slug, UpsertChapterRequest request) {
        Chapter chapter = chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug);

        if(chapter == null) {
            throw new NotFoundException("Chapter not found with chapter number: " + chapNo + " with novel slug : " + slug);
        }
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle());
        chapter.setContentDoc(request.getContentDoc());
        chapter.setThumbnailImageUrl(request.getThumbnailImageUrl());
        return convertToDTO(chapterRepository.save(chapter));
    }

    ChapterResponseDTO convertToDTOForPage(Chapter chapter) {
        ChapterResponseDTO chapterResponseDTO = new ChapterResponseDTO();
        chapterResponseDTO.setId(chapter.getId());
        chapterResponseDTO.setChapterNo(chapter.getChapterNo());
        chapterResponseDTO.setTitle(chapter.getTitle());
        chapterResponseDTO.setReleasedAt(chapter.getReleasedAt());
        chapterResponseDTO.setContentDoc(chapter.getContentDoc()
            .substring(0, Math.min(chapter.getContentDoc().length(), 200)));
        chapterResponseDTO.setThumbnailImageUrl(chapter.getThumbnailImageUrl());
        chapterResponseDTO.setNovelId(chapter.getNovel().getId());
        return chapterResponseDTO;
    }

    ChapterResponseDTO convertToDTO(Chapter chapter) {
        ChapterResponseDTO chapterResponseDTO = new ChapterResponseDTO();
        chapterResponseDTO.setId(chapter.getId());
        chapterResponseDTO.setChapterNo(chapter.getChapterNo());
        chapterResponseDTO.setTitle(chapter.getTitle());
        chapterResponseDTO.setReleasedAt(chapter.getReleasedAt());
        chapterResponseDTO.setContentDoc(chapter.getContentDoc());
        chapterResponseDTO.setThumbnailImageUrl(chapter.getThumbnailImageUrl());
        chapterResponseDTO.setNovelId(chapter.getNovel().getId());
        return chapterResponseDTO;
    }
}
