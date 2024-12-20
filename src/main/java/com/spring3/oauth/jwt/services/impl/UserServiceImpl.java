package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.*;
import com.spring3.oauth.jwt.entity.Package;
import com.spring3.oauth.jwt.entity.enums.UserStatusEnum;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.*;
import com.spring3.oauth.jwt.models.request.*;
import com.spring3.oauth.jwt.models.response.AuthorResponse;
import com.spring3.oauth.jwt.models.response.UserResponse;
import com.spring3.oauth.jwt.repositories.*;
import com.spring3.oauth.jwt.repositories.itf.AuthorProjection;
import com.spring3.oauth.jwt.services.EmailService;
import com.spring3.oauth.jwt.services.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private TierRepository tierRepository;
    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private HobbyRepository hobbyRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private CoinWalletRepository walletRepository;
    @Autowired
    private LikedLibraryRepository likedLibraryRepository;

    // Hàm xử lý quên mật khẩu
    @Override
    public void forgotPass(ForgotPassRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("User with email: " + request.getEmail() + " is not found..!!");
        }

        try {
            // Tạo mã OTP ngẫu nhiên
            String otpCode = generateOTP();

            // Lưu OTP vào database
            user.setOtpCode(otpCode);  // Đảm bảo entity `User` có trường `otpCode`
            userRepository.save(user);

            // Gửi mã OTP qua email
            emailService.sendOtpToEmail(request.getEmail(), otpCode);
        } catch (Exception e) {
            // Log lỗi để xác định nguyên nhân
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }


    @Override
    public UserResponse resetPassword(String email, String newPassword) {
        // Tìm người dùng qua email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User with email: " + email + " not found..!!");
        }

        // Mã hóa mật khẩu mới

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(newPassword);

        // Cập nhật mật khẩu
        user.setPassword(encodedPassword);
        user.setOtpCode(null);
        userRepository.save(user);
        return modelMapper.map(user, UserResponse.class);
    }

    // Hàm tạo mã OTP
    private String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));  // Tạo mã OTP 6 chữ số
    }

    // Hàm xác nhận OTP
    @Override
    public String verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User with email: " + email + " is not found..!!");
        }

        // Kiểm tra xem OTP có khớp không
        if (!user.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("OTP is invalid");
        }

        // Nếu OTP đúng, trả về thông báo thành công
        return "OTP is valid";
    }

    @Override
    public void updateReadCountChapter(long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        int newCount = user.getChapterReadCount() + 1;
        user.setChapterReadCount(newCount);
        user.setPoint(user.getPoint() + 3);
        List<Tier> tierList = tierRepository.findAll();
        for (Tier value : tierList) {
            if (newCount == value.getReadCountRequired()) {
                Tier tier = tierRepository.findByReadCountRequired(newCount);
                user.setTier(tier);
                break;
            }
        }
        userRepository.save(user);
    }

    @Override
    public String confirmPaymentStatus(String username) {
        User user = userRepository.findByUsername(username);
        if(user == null) {
            throw new NotFoundException("Not found");
        }
        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);
        return "ACTIVE";
    }

    @Override
    public String getRole(String username) {
        User user = userRepository.findByUsername(username);
        return user.getRoles().stream().findFirst().get().getName();
    }

    @Override
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username);
        String role = null;
        if(user.getRoles().isEmpty()) {
            return false;
        }else {
            role = user.getRoles().stream().findFirst().get().getName();
        }
        if(role.equals("ROLE_ADMIN")) {
            return true;
        }
        return false;
    }

    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        if(userRequest.getUsername() == null){
            throw new RuntimeException("Parameter username is not found in request..!!");
        } else if(userRequest.getPassword() == null){
            throw new RuntimeException("Parameter password is not found in request..!!");
        }else if(userRequest.getEmail() == null){
            throw new RuntimeException("Parameter email is not found in request..!!");
        }

        List<User> userListExistUsername = userRepository.findAllByUsername(userRequest.getUsername());

        List<User> userListExistEmail = userRepository.findAllByEmail(userRequest.getEmail());

        if(!userListExistUsername.isEmpty()) {
            throw new RuntimeException("Username is already exist..!!");
        }
        if(!userListExistEmail.isEmpty()) {
            throw new RuntimeException("Email is already exist..!!");
        }

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
//        String usernameFromAccessToken = userDetail.getUsername();
//
//        User currentUser = userRepository.findByUsername(usernameFromAccessToken);

        User savedUser = null;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = userRequest.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        Set<Role> roles = roleRepository.findAllByName("ROLE_USER");
        User user = modelMapper.map(userRequest, User.class);
        user.setPassword(encodedPassword);
        user.setRoles(roles);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDob(null);
        user.setChapterReadCount(0);
        user.setPoint(1);
        user.setSelectedGenres(null);
        user.setHobbies(null);
        user.setWallet(null);
        if(userRequest.getId() != null){
            User oldUser = userRepository.findFirstById(userRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setEmail(user.getEmail());
                oldUser.setRoles(roles);
                oldUser.setStatus(UserStatusEnum.ACTIVE);
                oldUser.setImagePath(null);
                oldUser.setCreatedAt(LocalDateTime.now());
                oldUser.setUpdatedAt(LocalDateTime.now());
                oldUser.setDob(null);
                oldUser.setChapterReadCount(user.getChapterReadCount());
                oldUser.setPoint(user.getPoint());
                oldUser.setSelectedGenres(null);
                oldUser.setHobbies(null);
                oldUser.setWallet(user.getWallet());
                savedUser = userRepository.save(oldUser);
                userRepository.refresh(savedUser);
            } else {
                throw new RuntimeException("Can't find record with identifier: " + userRequest.getId());
            }
        } else {
//            user.setCreatedBy(currentUser);
            userRepository.save(user);

            LikedLibrary likedLibraryCheck = likedLibraryRepository.findByUser_Id((int) user.getId());
            if(likedLibraryCheck != null) {
                throw new NotFoundException("Liked Library already exists with user id: " + user.getId());
            }

            LikedLibrary likedLibrary = new LikedLibrary();
            likedLibrary.setUser(user);
            likedLibrary.setNovels(null);
            likedLibraryRepository.save(likedLibrary);

            CoinWallet wallet = new CoinWallet();
            wallet.setId("wallet_" + user.getId());
            wallet.setCoinAmount(0);
            wallet.setCoinSpent(0);
            wallet.setCreatedDate(LocalDate.now());
            walletRepository.save(wallet);
            user.setWallet(wallet);
            savedUser = userRepository.save(user);
        }
        userRepository.refresh(savedUser);
        UserResponse userResponse = modelMapper.map(savedUser, UserResponse.class);
        return userResponse;
    }

    @Override
    public UserResponse saveAuthor(AuthorRequest authorRequest) {
        if(authorRequest.getUsername() == null){
            throw new RuntimeException("Parameter username is not found in request..!!");
        } else if(authorRequest.getPassword() == null){
            throw new RuntimeException("Parameter password is not found in request..!!");
        }else if(authorRequest.getEmail() == null){
            throw new RuntimeException("Parameter email is not found in request..!!");
        }

        List<User> userListExistUsername = userRepository.findAllByUsername(authorRequest.getUsername());

        List<User> userListExistEmail = userRepository.findAllByEmail(authorRequest.getEmail());

        if(!userListExistUsername.isEmpty()) {
            throw new RuntimeException("Username is already exist..!!");
        }
        if(!userListExistEmail.isEmpty()) {
            throw new RuntimeException("Email is already exist..!!");
        }
        User savedUser = null;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = authorRequest.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        Set<Role> roles = roleRepository.findAllByName("ROLE_AUTHOR");
        User user = modelMapper.map(authorRequest, User.class);
        user.setPassword(encodedPassword);
        user.setRoles(roles);
        user.setFullName(authorRequest.getFullName());
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDob(authorRequest.getDob());
        user.setChapterReadCount(0);
        user.setPoint(1);
        user.setSelectedGenres(null);
        user.setHobbies(null);
        user.setWallet(null);
        if(authorRequest.getId() != null){
            User oldUser = userRepository.findFirstById(authorRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setEmail(user.getEmail());
                oldUser.setFullName(user.getFullName());
                oldUser.setRoles(roles);
                oldUser.setStatus(UserStatusEnum.ACTIVE);
                oldUser.setImagePath(null);
                oldUser.setCreatedAt(LocalDateTime.now());
                oldUser.setUpdatedAt(LocalDateTime.now());
                oldUser.setDob(user.getDob());
                oldUser.setChapterReadCount(user.getChapterReadCount());
                oldUser.setPoint(user.getPoint());
                oldUser.setSelectedGenres(null);
                oldUser.setHobbies(null);
                oldUser.setWallet(user.getWallet());
                savedUser = userRepository.save(oldUser);
                userRepository.refresh(savedUser);
            } else {
                throw new RuntimeException("Can't find record with identifier: " + authorRequest.getId());
            }
        } else {
//            user.setCreatedBy(currentUser);
            userRepository.save(user);

            LikedLibrary likedLibraryCheck = likedLibraryRepository.findByUser_Id((int) user.getId());
            if(likedLibraryCheck != null) {
                throw new NotFoundException("Liked Library already exists with user id: " + user.getId());
            }

            LikedLibrary likedLibrary = new LikedLibrary();
            likedLibrary.setUser(user);
            likedLibrary.setNovels(null);
            likedLibraryRepository.save(likedLibrary);

            CoinWallet wallet = new CoinWallet();
            wallet.setId("wallet_" + user.getId());
            wallet.setCoinAmount(0);
            wallet.setCoinSpent(0);
            wallet.setCreatedDate(LocalDate.now());
            walletRepository.save(wallet);
            user.setWallet(wallet);
            savedUser = userRepository.save(user);
        }
        userRepository.refresh(savedUser);
        UserResponse userResponse = modelMapper.map(savedUser, UserResponse.class);
        return userResponse;
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetail.getUsername();
        User user = userRepository.findByUsername(usernameFromAccessToken);
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        return userResponse;
    }

    @Override
    public UserResponseDTO getProfile(String username) {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username: " + username + " is not found..!!");
        }
        return convertToDTO(user);
    }

    @Override
    public boolean setStatusUser(String username) {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username: " + username + " is not found..!!");
        }
        if (user.getStatus().equals(UserStatusEnum.ACTIVE)) {
            user.setStatus(UserStatusEnum.INACTIVE);
            userRepository.save(user);
            return false;
        } else {
            user.setStatus(UserStatusEnum.ACTIVE);
            userRepository.save(user);
            return true;
        }
    }

    @Override
    public UserResponseDTO updateProfile(UpdateUserRequest request, String username) {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username: " + username + " is not found..!!");
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setUpdatedAt(LocalDateTime.now());
        user.setImagePath(request.getImagePath());
        user.setRoles(request.getRoles());
        userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO updateSelectedGenres(Long userId, GenresSelectedRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Genre> selectedGenres = genreRepository.findAllById(request.getSelectedGenreIds());
        if(selectedGenres.isEmpty()) {
            throw new NullPointerException("Genre ids is null!");
        }
        user.setSelectedGenres(selectedGenres);
        userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public List<TopReadResponseDTO> getTopRead() {
        List<User> topReads = userRepository.findFifthUsersReadingTheMost();
        if(topReads.isEmpty()) {
            throw new NotFoundException("No user found..!!");
        }
        return topReads.stream()
            .map(this::convertToTopReadDto)
            .toList();
    }

    @Override
    public List<TopScoreResponseDTO> getTopPoint() {
        List<User> topReads = userRepository.findFifthUsersReadingTheMost();
        if(topReads.isEmpty()) {
            throw new NotFoundException("No user found..!!");
        }
        return topReads.stream()
            .map(this::convertToTopScoreDto)
            .toList();
    }

    @Override
    public long getUserIdByUsername(String username) {
        long userId = userRepository.findByUsername(username).getId();
        if(userRepository.findByUsername(username) == null) {
            throw new NotFoundException("User not found with username: " + username);
        }
        return userId;
    }

    @Override
    public Map<String, Integer> getAllUsersQuantityForEachLevel() {
        Map<String, Integer> result = new HashMap<>();
        List<Tier> tiers = tierRepository.findAll();
        for (Tier tier : tiers) {
            int count = userRepository.countAllByTierId(tier.getId());
            result.put(tier.getName(), count);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getUserCountByScoreRange() {
        List<Object[]> results = userRepository.getUserCountByScoreRange();
        return results.stream()
            .collect(Collectors.toMap(
                arr -> (String) arr[0],
                arr -> ((Number) arr[1]).intValue(),
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));
    }

    @Override
    public Map<String, Integer> getTotalLikeCountsForLastWeek() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<Object[]> results = userLikeRepository.findTotalLikeCountsByDay(startDate);

        Map<String, Integer> likeCountsByDay = new HashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Integer likeCounts = ((Number) result[1]).intValue();
            likeCountsByDay.put(date, likeCounts);
        }

        // Fill in missing days with zero likes
        List<LocalDate> lastWeekDates = IntStream.rangeClosed(0, 6)
            .mapToObj(i -> LocalDate.now().minusDays(i))
            .toList();

        for (LocalDate date : lastWeekDates) {
            likeCountsByDay.putIfAbsent(date.toString(), 0);
        }

        return likeCountsByDay;
    }

    @Override
    public void addPoint(int point, long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        user.setPoint(user.getPoint() + point);
        userRepository.save(user);
    }

    @Override
    public List<AuthorResponseDTO> getAuthors() {
        List<AuthorProjection> authors = userRepository.findAllByRoleAuthor();
        if (authors.isEmpty()) {
            throw new NotFoundException("No author found..!!");
        }
        return authors.stream()
            .map(author -> {
                AuthorResponseDTO dto = new AuthorResponseDTO();
                dto.setId(author.getId());
                dto.setFullName(author.getFullName());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void followAuthor(String currentUsername, long authorId) {
        User currentUser = userRepository.findByUsername(currentUsername);
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + authorId));

        // Check if user is trying to follow themselves
        if (currentUser.getId() == authorId) {
            throw new RuntimeException("Users cannot follow themselves");
        }

        // Check if target user is an author
        if (!author.isAuthor()) {
            throw new RuntimeException("Target user is not an author");
        }

        // Check if already following
        if (currentUser.getFollowing().contains(author)) {
            throw new RuntimeException("Already following this author");
        }

        author.addFollower(currentUser);
        userRepository.save(author);
        userRepository.save(currentUser);
    }


    @Override
    public void unfollowAuthor(String currentUsername, long authorId) {
        User currentUser = userRepository.findByUsername(currentUsername);
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + authorId));

        if (!currentUser.getFollowing().contains(author)) {
            throw new RuntimeException("Not following this author");
        }

        author.removeFollower(currentUser);
        userRepository.save(author);
        userRepository.save(currentUser);
    }

    @Override
    public List<FollowResponseDTO> getAuthorFollowers(long authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + authorId));

        if (!author.isAuthor()) {
            throw new RuntimeException("User is not an author");
        }

        return author.getFollowers().stream()
            .map(this::convertToFollowDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<FollowResponseDTO> getUserFollowing(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found with username: " + username);
        }

        return user.getFollowing().stream()
            .map(this::convertToFollowDTO)
            .collect(Collectors.toList());
    }

    @Override
    public int getFollowerCount(long authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NotFoundException("Author not found with id: " + authorId));
        return author.getFollowerCount();
    }

    TopScoreResponseDTO convertToTopScoreDto(User user) {
        TopScoreResponseDTO dto = new TopScoreResponseDTO();
        Tier tier = user.getTier();
        dto.setUserId(user.getId());
        dto.setImagePath(user.getImagePath());
        dto.setFullName(user.getFullName());
        dto.setPoint(user.getPoint());
        if(tier == null) {
            dto.setTierName("No tier");
        }else
            dto.setTierName(tier.getName());
        return dto;
    }

    private FollowResponseDTO convertToFollowDTO(User user) {
        FollowResponseDTO dto = new FollowResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setImagePath(user.getImagePath());
        return dto;
    }

    TopReadResponseDTO convertToTopReadDto(User user) {
        TopReadResponseDTO dto = new TopReadResponseDTO();
        Tier tier = user.getTier();
        dto.setUserId(user.getId());
        dto.setImagePath(user.getImagePath());
        dto.setFullName(user.getFullName());
        dto.setChapterReadCount(user.getChapterReadCount());
        if(tier == null) {
            dto.setTierName("No tier");
        }else
            dto.setTierName(tier.getName());
        return dto;
    }

    UserResponseDTO convertToDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullName(user.getFullName());
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setChapterReadCount(user.getChapterReadCount());
        userResponseDTO.setPoint(user.getPoint());
        userResponseDTO.setAccountStatus(String.valueOf(user.getStatus()));
        if(user.getTier() == null){
            userResponseDTO.setTierName("No tier");
        }else {
            userResponseDTO.setTierName(user.getTier().getName());
        }
        userResponseDTO.setImagePath(user.getImagePath());
        userResponseDTO.setCreatedAt(user.getCreatedAt());
        userResponseDTO.setUpdatedAt(user.getUpdatedAt());
        if(user.getSelectedGenres().isEmpty()) {
            userResponseDTO.setSelectedGenreIds(null);
        }else{
            userResponseDTO.setSelectedGenreIds(user.getSelectedGenres()
                .stream()
                .map(Genre::getId)
                .toList()
            );
        }
        if(user.getHobbies().isEmpty()) {
            userResponseDTO.setHobbyNames(null);
        }else{
            userResponseDTO.setHobbyNames(user.getHobbies()
                .stream()
                .map(Hobby::getName)
                .toList()
            );
        }
        if(user.getRoles().stream().map(Role::getName).toList().contains("ROLE_AUTHOR")) {
            userResponseDTO.setFollowerCount(user.getFollowerCount());
            int totalNovel = userRepository.countAllByRoles(user.getUsername(), "ROLE_AUTHOR");
            userResponseDTO.setNovelOwnCount(totalNovel);
        }
        Subscription subscription = subscriptionRepository.findSubsByUserIdAndActive(user.getId(), true);
        if(subscription == null) {
            userResponseDTO.setDayLeft("Bạn chưa mua gói premium.");
            return userResponseDTO;
        }

        userResponseDTO.setDayLeft("Gói của bạn còn lại " + ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), subscription.getEndDate().toLocalDate()) + " ngày.");
        if(user.getWallet() == null) {
            userResponseDTO.setCoinWallet(0);
            userResponseDTO.setCoinSpent(0);
            return userResponseDTO;
        }

        userResponseDTO.setCoinWallet(user.getWallet().getCoinAmount());
        userResponseDTO.setCoinSpent(user.getWallet().getCoinSpent());
        return userResponseDTO;
    }

    @Override
    public Page<UserResponse> getAllUser(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> modelMapper.map(user, UserResponse.class));
    }

    @Override
    public PagedResultDTO<AuthorResponse> getAllAuthor(PaginationDTO paginationDTO) {
        Pageable pageable = createPageable(paginationDTO);
        Page<AuthorResponse> pageResult = userRepository.findAllAuthors(pageable);
        // Tạo kết quả phân trang và trả về
        return PagedResultDTO.<AuthorResponse>builder()
            .items(pageResult.getContent()) // Danh sách các AuthorResponse
            .currentPage(pageResult.getNumber()) // Trang hiện tại
            .totalPages(pageResult.getTotalPages()) // Tổng số trang
            .totalItems(pageResult.getTotalElements()) // Tổng số bản ghi
            .build();
    }

    // Chuyển PaginationDTO thành Pageable
    public Pageable createPageable(PaginationDTO paginationDTO) {
        return PageRequest.of(
            paginationDTO.getPageNum(),
            paginationDTO.getPageSize(),
            Sort.by("fullName").ascending() // Sắp xếp theo cột `fullName`, thay đổi nếu cần
        );
    }


}
