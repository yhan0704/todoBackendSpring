package backend.category.dto.response;

import backend.category.entity.Category;

public record CategoryResponse(Long id, String name, boolean mine) {
    public static CategoryResponse from(Category category, Long currentUserId) {
        boolean mine = category.getUser() != null && category.getUser().getId().equals(currentUserId);
        return new CategoryResponse(category.getId(), category.getName(), mine);
    }
}
