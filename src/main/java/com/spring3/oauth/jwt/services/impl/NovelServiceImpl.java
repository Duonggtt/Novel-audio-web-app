package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.*;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.NovelDetailResponseDTO;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PagedResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PaginationDTO;
import com.spring3.oauth.jwt.models.request.UpdateNovelRequest;
import com.spring3.oauth.jwt.models.request.UpsertNovelRequest;
import com.spring3.oauth.jwt.repositories.*;
import com.spring3.oauth.jwt.services.NovelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NovelServiceImpl implements NovelService {

    private final NovelRepository novelRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final RateRepository rateRepository;
    private final UserRateRepository userRateRepository;


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
    public Map<String, Integer> getNovelCountByGenre() {
        Map<String, Integer> result = new HashMap<>();
        List<Genre> genres = genreRepository.findAll();
        for (Genre genre : genres) {
            int count = novelRepository.countByGenres(genre.getId());
            result.put(genre.getName(), count);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getLikeCountsForLastWeek() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<Object[]> results = novelRepository.findLikeCountsByDay(startDate);

        Map<String, Integer> likeCountsByDay = new HashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Integer likeCounts = ((Number) result[1]).intValue();
            likeCountsByDay.put(date, likeCounts);
        }

        return likeCountsByDay;
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
    public List<NovelResponseDTO> findAllByAuthorUsername(String username) {
        List<Novel> novels = novelRepository.findAllByAuthorAuthName(username);
        if(novels.isEmpty()) {
            throw new NotFoundException("Novel not found with author username " + username);
        }
        // Mapping từ Novel sang NovelResponseDTO
        return novels.stream()
                .map(this::convertToDto)
                .toList();
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
        Novel isExist = novelRepository.findBySlug(request.getSlug());
        if(isExist != null) {
            throw new NotFoundException("Novel already exists with slug " + request.getSlug());
        }

        Novel isExist2 = novelRepository.findByTitle(request.getTitle());
        if(isExist2 != null) {
            throw new NotFoundException("Novel already exists with title " + request.getTitle());
        }
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
        novel.setAuthor(userRepository.findById(request.getAuthorId())
            .orElseThrow(() -> new NotFoundException("Author not found with id " + request.getAuthorId())));
        novel.setGenres(genreRepository.findAllById(request.getGenreIds()));

        Rate rate = new Rate();
        rate.setRate(BigDecimal.ZERO);
        rate.setRateQuantity(0);
        rate.setNovel(novel);
        rateRepository.save(rate);

        return convertToDto(novelRepository.save(novel));

    }

    @Override
    public NovelResponseDTO updateNovel(String novelSlug, UpdateNovelRequest request) {
        Novel novel = novelRepository.findBySlug(novelSlug);
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug: " + novelSlug);
        }
        Novel isExist = novelRepository.findByTitle(request.getTitle());
        if(isExist != null) {
            throw new NotFoundException("Novel already exists with title " + request.getTitle());
        }
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription());
        novel.setStatus(request.getStatus());
        novel.setThumbnailImageUrl(request.getThumbnailImageUrl());
        novel.setClosed(request.isClosed());
        novel.setAuthor(userRepository.findById(request.getAuthorId())
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
            dto.setAuthorName(novel.getAuthor().getFullName());
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

        // Lấy thông tin rate của user nếu có
        BigDecimal userRate = userRateRepository.findByUser_IdAndNovel_Slug(userId, novel.getSlug())
            .map(UserRate::getRatePoint)
            .orElse(null);

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
        dto.setUserRate(userRate);
        if(novel.getAuthor() == null) {
            dto.setAuthorName(null);
        }else {
            dto.setAuthorName(novel.getAuthor().getFullName());
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
