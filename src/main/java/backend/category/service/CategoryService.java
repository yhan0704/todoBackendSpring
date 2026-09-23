package backend.category.service;

import backend.category.dto.CategoryRequest;
import backend.category.dto.response.CategoryResponse;
import backend.category.entity.Category;
import backend.category.entity.HiddenCategory;
import backend.category.repository.CategoryRepository;
import backend.category.repository.HiddenCategoryRepository;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final HiddenCategoryRepository hiddenCategoryRepository;
    private final UserRepository userRepository;

    // 추천 카테고리(user_id NULL, 내가 숨긴 것 제외) + 로그인한 유저 본인 카테고리를 같이 반환.
    public List<CategoryResponse> getMyCategories() {
        User currentUser = currentUser();
        Set<Long> hiddenIds = Set.copyOf(hiddenCategoryRepository.findCategoryIdsByUser(currentUser));
        return categoryRepository.findByUserIsNullOrUser(currentUser).stream()
                .filter(category -> !hiddenIds.contains(category.getId()))
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

    // 추천 카테고리는 원본을 지우면 다른 유저에게도 영향을 주므로, 그 유저에게만 안 보이도록
    // hidden_categories에 기록한다. 본인 카테고리는 원래대로 실제 삭제.
    @Transactional
    public Long deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        User currentUser = currentUser();

        if (category.getUser() == null) {
            hideCategory(category, currentUser);
        } else if (category.getUser().getId().equals(currentUser.getId())) {
            categoryRepository.deleteById(id);
        } else {
            throw new AccessDeniedException("본인이 만든 카테고리만 삭제할 수 있습니다");
        }
        return id;
    }

    private void hideCategory(Category category, User currentUser) {
        if (hiddenCategoryRepository.existsByUserAndCategory(currentUser, category)) return;
        hiddenCategoryRepository.save(
                HiddenCategory.builder().user(currentUser).category(category).build()
        );
    }

    // 추천 카테고리(user null)는 아무도 수정 못 하고, 본인이 만든 카테고리만 가능.
    private void requireOwnCategory(Category category) {
        User currentUser = currentUser();
        if (category.getUser() == null || !category.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인이 만든 카테고리만 수정할 수 있습니다");
        }
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
