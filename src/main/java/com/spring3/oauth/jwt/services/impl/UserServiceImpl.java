package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.*;
import com.spring3.oauth.jwt.entity.Package;
import com.spring3.oauth.jwt.entity.enums.UserStatusEnum;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.FollowResponseDTO;
import com.spring3.oauth.jwt.models.dtos.TopReadResponseDTO;
import com.spring3.oauth.jwt.models.dtos.TopScoreResponseDTO;
import com.spring3.oauth.jwt.models.dtos.UserResponseDTO;
import com.spring3.oauth.jwt.models.request.ForgotPassRequest;
import com.spring3.oauth.jwt.models.request.GenresSelectedRequest;
import com.spring3.oauth.jwt.models.request.UpdateUserRequest;
import com.spring3.oauth.jwt.models.request.UserRequest;
import com.spring3.oauth.jwt.models.response.UserResponse;
import com.spring3.oauth.jwt.repositories.*;
import com.spring3.oauth.jwt.services.EmailService;
import com.spring3.oauth.jwt.services.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PackageRepository packageRepository;

    @Autowired
    private CoinWalletRepository walletRepository;

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
        user.setStatus(UserStatusEnum.INACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDob(null);
        user.setChapterReadCount(0);
        user.setPoint(1);
        user.setSelectedGenres(null);
        user.setHobbies(null);
        CoinWallet wallet = new CoinWallet();
        wallet.setId("wallet_" + user.getId());
        wallet.setCoinAmount(0);
        wallet.setCoinSpent(0);
        wallet.setCreatedDate(LocalDate.now());
        walletRepository.save(wallet);
        user.setWallet(wallet);
        if(userRequest.getId() != null){
            User oldUser = userRepository.findFirstById(userRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setEmail(user.getEmail());
                oldUser.setRoles(roles);
                oldUser.setStatus(UserStatusEnum.INACTIVE);
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
    public UserResponseDTO updateProfile(UpdateUserRequest request, String username) {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username: " + username + " is not found..!!");
        }
        List<Hobby> hobbies = hobbyRepository.findAllById(request.getHobbyIds());
        if(hobbies.isEmpty()) {
            throw new NullPointerException("Hobby ids is null!");
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setUpdatedAt(LocalDateTime.now());
        user.setHobbies(hobbies);
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
        List<User> users = (List<User>) userRepository.findAll();
        Map<String, Integer> scoreRanges = new LinkedHashMap<>();

        List<int[]> ranges = List.of(
            new int[]{0, 999},
            new int[]{1000, 4999},
            new int[]{5000, 9999},
            new int[]{10000, 49999},
            new int[]{50000, 100000}
        );

        for (int[] range : ranges) {
            String key = (range[0] == 50000) ? "50000+" : range[0] + "-" + range[1];
            scoreRanges.put(key, 0);
        }

        for (User user : users) {
            int score = user.getPoint();
            for (int[] range : ranges) {
                if (score >= range[0] && score <= range[1]) {
                    String key = range[0] + "-" + range[1];
                    scoreRanges.put(key, scoreRanges.get(key) + 1);
                    break;
                }
            }
        }

        return scoreRanges;
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
    public List<UserResponse> getAllUser() {
        List<User> users = (List<User>) userRepository.findAll();
        Type setOfDTOsType = new TypeToken<List<UserResponse>>(){}.getType();
        List<UserResponse> userResponses = modelMapper.map(users, setOfDTOsType);
        return userResponses;
    }

}
