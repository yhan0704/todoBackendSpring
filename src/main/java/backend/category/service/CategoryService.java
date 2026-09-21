package backend.category.service;

import backend.category.dto.response.CategoryResponse;
import backend.category.repository.CategoryRepository;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 추천 카테고리(user_id NULL) + 로그인한 유저 본인 카테고리를 같이 반환.
    public List<CategoryResponse> getMyCategories() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return categoryRepository.findByUserIsNullOrUser(user).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
