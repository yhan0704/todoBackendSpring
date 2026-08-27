package backend.user.service;

import backend.user.dto.request.UpdateEmailRequest;
import backend.user.dto.response.UserResponse;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Builder
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateEmail(Long id, UpdateEmailRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.updateEmail(request.email());

        return UserResponse.from(user);
    }
}