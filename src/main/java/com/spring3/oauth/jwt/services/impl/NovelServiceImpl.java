package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Genre;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.entity.UserLike;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.NovelDetailResponseDTO;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PagedResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PaginationDTO;
import com.spring3.oauth.jwt.models.request.UpsertNovelRequest;
import com.spring3.oauth.jwt.repositories.*;
import com.spring3.oauth.jwt.services.NovelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NovelServiceImpl implements NovelService {

    private final NovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;


    @Override
    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }

    @Override
    public List<NovelResponseDTO> getAllNovelDtos() {
        return novelRepository.findAllByClosedFalse()
            .stream()
            .map(this::convertToDto)
            .toList();
    }

    @Override
    public PagedResponseDTO getAllTrendingNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByClosedFalseOrderByLikeCountsDesc(pageable);
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public PagedResponseDTO getAllNovelsByGenreName(String genreName, Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByGenreName(genreName, pageable);
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }


    @Override
    public PagedResponseDTO getAllNovelsRecommend( Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id " + userId));
        if(user.getSelectedGenres() == null) {
            throw new NotFoundException("User not selected any genre");
        }
        List<Integer> userSelectedGenreIds = user.getSelectedGenres()
            .stream()
            .map(Genre::getId)
            .toList();
        Page<Novel> recommendNovelList = novelRepository.findAllByGenres_IdIn(userSelectedGenreIds, pageable);

        List<NovelResponseDTO> novelDTOs = recommendNovelList.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(recommendNovelList.getNumber(), recommendNovelList.getSize(), recommendNovelList.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public boolean isNovelLikedByUser(long userId, String novelSlug) {
        return userLikeRepository.findByUser_IdAndNovel_Slug(userId, novelSlug)
            .isPresent();
    }

    @Override
    public List<String> getLikedNovelSlugsByUser(Long userId) {
        return userLikeRepository.findLikedNovelSlugsByUser(userId);
    }

    @Override
    public List<String> getLikedNovelIdsByUserForSpecificNovels(Long userId, List<Integer> novelIds) {
        return userLikeRepository.findLikedNovels(userId, novelIds);
    }

    @Override
    public boolean likeNovel( long userId, String slug) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Novel novel = novelRepository.findBySlug(slug);
        if(novel == null) {
            throw new NotFoundException("Novel not found with id: " + slug);
        }
        Optional<UserLike> existingLike = userLikeRepository
            .findByUser_IdAndNovel_Slug(userId, slug);
        if (existingLike.isPresent()) {
            // Nếu đã like rồi thì xóa like (unlike)
            userLikeRepository.delete(existingLike.get());
            log.info("Removed like for novel: {} by user: {}", slug, userId);
            return false;
        } else {
            // Nếu chưa like thì tạo like mới
            UserLike newLike = new UserLike();
            newLike.setUser(user);
            newLike.setNovel(novel);
            newLike.setLikedAt(LocalDateTime.now());
            userLikeRepository.save(newLike);
            log.info("Added like for novel: {} by user: {}", slug, userId);
            return true;
        }
    }


    @Override
    public PagedResponseDTO findAllByAuthorName(String authorName, Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByAuthorName(authorName, pageable);
        if(novels.isEmpty()) {
            throw new NotFoundException("Novel not found with author name " + authorName);
        }
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public PagedResponseDTO findAllByAuthorId(Integer authorId, Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByAuthor_Id(authorId, pageable);
        if(novels.isEmpty()) {
            throw new NotFoundException("Novel not found with author id " + authorId);
        }
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public PagedResponseDTO findAllByTitle(String title, Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByTitleContaining(title, pageable);
        if (novels.isEmpty()) {
            throw new NotFoundException("Novel not found with title " + title);
        }
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public PagedResponseDTO getAllTopNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByClosedFalseOrderByReadCountsDesc(pageable);
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public PagedResponseDTO findAllByReleasedAtWithinLast7Days(Pageable pageable) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Page<Novel> novels = novelRepository.findAllByReleasedAtWithinLast7Days(sevenDaysAgo, pageable);
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    public List<NovelResponseDTO> findSomeNovelsSameGenre(Integer genreId) {
        return null;
    }

    @Override
    public PagedResponseDTO findAllByGenre(List<Integer> genreIds, Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByGenres_IdIn(genreIds, pageable);
        // Mapping từ Novel sang NovelResponseDTO
        List<NovelResponseDTO> novelDTOs = novels.stream()
            .map(this::convertToDto)
            .toList();

        // Tạo đối tượng PaginationDTO
        PaginationDTO pagination = new PaginationDTO(novels.getNumber(), novels.getSize(), novels.getTotalElements());
        return new PagedResponseDTO(novelDTOs, pagination);
    }

    @Override
    public NovelDetailResponseDTO getDetailNovel(String slug, long userId) {
        Novel novel = novelRepository.findBySlug(slug);
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug " + slug);
        }
        return convertToDtoDetail(novel, userId);
    }

    @Override
    public NovelResponseDTO updateLikeCount(String slug) {
        Novel novel = novelRepository.findBySlug(slug);
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug " + slug);
        }
        novel.setLikeCounts(novel.getLikeCounts() + 1);
        return convertToDto(novelRepository.save(novel));
    }


    @Override
    public NovelResponseDTO getNovelById(Integer id) {
        return novelRepository.findNovelDtoById(id)
            .orElseThrow(() -> new NotFoundException("Novel not found with id " + id));
    }

    @Override
    public NovelResponseDTO saveNovel(UpsertNovelRequest request) {
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setSlug(request.getSlug());
        novel.setDescription(request.getDescription());
        novel.setReleasedAt(LocalDateTime.now());
        novel.setStatus(request.getStatus());
        novel.setThumbnailImageUrl(request.getThumbnailImageUrl());
        novel.setClosed(request.isClosed());
        novel.setReadCounts(request.getReadCounts());
        novel.setTotalChapters(request.getTotalChapters());
        novel.setAverageRatings(request.getAverageRatings());
        novel.setLikeCounts(request.getLikeCounts());
        novel.setAuthor(authorRepository.findById(request.getAuthorId())
            .orElseThrow(() -> new NotFoundException("Author not found with id " + request.getAuthorId())));
        novel.setGenres(genreRepository.findAllById(request.getGenreIds()));
        return convertToDto(novelRepository.save(novel));

    }

    @Override
    public NovelResponseDTO updateNovel(String novelSlug, UpsertNovelRequest request) {
        Novel novel = novelRepository.findBySlug(novelSlug);
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug: " + novelSlug);
        }
        novel.setTitle(request.getTitle());
        novel.setSlug(request.getSlug());
        novel.setDescription(request.getDescription());
        novel.setStatus(request.getStatus());
        novel.setThumbnailImageUrl(request.getThumbnailImageUrl());
        novel.setClosed(request.isClosed());
        novel.setReadCounts(request.getReadCounts());
        novel.setTotalChapters(request.getTotalChapters());
        novel.setAverageRatings(request.getAverageRatings());
        novel.setLikeCounts(request.getLikeCounts());
        novel.setAuthor(authorRepository.findById(request.getAuthorId())
            .orElseThrow(() -> new NotFoundException("Author not found with id " + request.getAuthorId())));
        novel.setGenres(genreRepository.findAllById(request.getGenreIds()));
        return convertToDto(novelRepository.save(novel));
    }

    @Override
    public void deleteNovel(Integer id) {
        Novel novel = novelRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Novel not found with id " + id));
        novelRepository.delete(novel);
    }

    NovelResponseDTO convertToDto(Novel novel) {
        NovelResponseDTO dto = new NovelResponseDTO();

        dto.setTitle(novel.getTitle());
        dto.setSlug(novel.getSlug());
        dto.setDescription(novel.getDescription());
        dto.setReleasedAt(novel.getReleasedAt());
        dto.setStatus(novel.getStatus());
        dto.setClosed(novel.isClosed());
        dto.setThumbnailImageUrl(novel.getThumbnailImageUrl());
        dto.setReadCounts(novel.getReadCounts());
        dto.setTotalChapters(novel.getTotalChapters());
        dto.setAverageRatings(novel.getAverageRatings());
        dto.setLikeCounts(novel.getLikeCounts());
        if(novel.getAuthor() == null) {
            dto.setAuthorName(null);
        }else {
            dto.setAuthorName(novel.getAuthor().getName());
        }
        if(novel.getGenres() == null) {
            dto.setGenreNames(null);
        }
        else {
            dto.setGenreNames(novel.getGenres()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList())
            );
        }
        return dto;
    }

    NovelDetailResponseDTO convertToDtoDetail(Novel novel, long userId) {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        boolean isLiked = userLikeRepository.findByUser_IdAndNovel_Slug(userId, novel.getSlug())
            .isPresent();

        dto.setTitle(novel.getTitle());
        dto.setSlug(novel.getSlug());
        dto.setDescription(novel.getDescription());
        dto.setReleasedAt(novel.getReleasedAt());
        dto.setStatus(novel.getStatus());
        dto.setClosed(novel.isClosed());
        dto.setThumbnailImageUrl(novel.getThumbnailImageUrl());
        dto.setReadCounts(novel.getReadCounts());
        dto.setTotalChapters(novel.getTotalChapters());
        dto.setAverageRatings(novel.getAverageRatings());
        dto.setLikeCounts(novel.getLikeCounts());
        dto.setLiked(isLiked);
        if(novel.getAuthor() == null) {
            dto.setAuthorName(null);
        }else {
            dto.setAuthorName(novel.getAuthor().getName());
        }
        if(novel.getGenres() == null) {
            dto.setGenreNames(null);
        }
        else {
            dto.setGenreNames(novel.getGenres()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
