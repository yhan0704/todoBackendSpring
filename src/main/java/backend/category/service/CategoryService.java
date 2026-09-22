package backend.category.service;

import backend.category.dto.CategoryRequest;
import backend.category.dto.response.CategoryResponse;
import backend.category.entity.Category;
import backend.category.repository.CategoryRepository;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        User currentUser = currentUser();
        return categoryRepository.findByUserIsNullOrUser(currentUser).stream()
                .map(category -> CategoryResponse.from(category, currentUser.getId()))
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User currentUser = currentUser();
        Category category = Category.builder()
                .name(request.name())
                .user(currentUser)
                .build();
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved, currentUser.getId());
    }

    @Transactional
    public CategoryResponse editCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        requireOwnCategory(category);
        category.update(request.name());
        return CategoryResponse.from(category, currentUser().getId());
    }

    @Transactional
    public Long deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        requireOwnCategory(category);
        categoryRepository.deleteById(id);
        return id;
    }

    // 추천 카테고리(user null)는 아무도 수정/삭제 못 하고, 본인이 만든 카테고리만 가능.
    private void requireOwnCategory(Category category) {
        User currentUser = currentUser();
        if (category.getUser() == null || !category.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인이 만든 카테고리만 수정/삭제할 수 있습니다");
        }
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
