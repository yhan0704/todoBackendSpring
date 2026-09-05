package backend.todo.repository;

import backend.todo.entity.Todo;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

// Todo.user에 걸린 @ManyToOne(fetch = FetchType.LAZY)가 실제로 지연 로딩되는지 확인하는 테스트.
// Hibernate.isInitialized(...)로 "이 연관관계가 진짜 DB에서 로딩됐는지"를 직접 물어볼 수 있음 —
// 콘솔 SQL 로그를 눈으로 세는 것보다 훨씬 정확한 검증 방법.
//
// 실험해보고 싶으면: Todo.java의 fetch를 LAZY -> EAGER로 바꾸고 이 테스트를 다시 돌려보기.
// EAGER로 바꾸면 findById 시점에 user까지 이미 로딩되어 있어서 아래 첫 번째 assertThat(...isFalse())가
// 깨짐 (isInitialized가 true로 나옴) — 그게 바로 LAZY와 EAGER의 실질적 차이.
@Testcontainers
@SpringBootTest
@Transactional
class TodoUserFetchTypeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("LAZY로 설정된 Todo.user는 findById 직후엔 초기화되지 않고, getUser()를 호출해야 그때 로딩된다")
    void todoUser_isNotLoaded_untilAccessed() {
        // given: user와 그 user를 참조하는 todo를 저장
        User user = User.builder()
                .name("테스트유저")
                .email("fetchtype-test@example.com")
                .password("dummy")
                .build();
        userRepository.save(user);

        Todo todo = Todo.builder()
                .tasks("fetch 실험용 todo")
                .done(false)
                .user(user)
                .build();
        todoRepository.save(todo);

        // 영속성 컨텍스트를 비워야 함 — 안 비우면 방금 저장한 user가 1차 캐시에 그대로 남아있어서
        // findById가 DB를 다시 안 타고 캐시된(이미 초기화된) 객체를 그대로 돌려줘 실험이 무의미해짐
        entityManager.flush();
        entityManager.clear();

        // when: todo만 다시 조회 (아직 user는 안 건드림)
        Todo found = todoRepository.findById(todo.getId()).orElseThrow();

        // then: LAZY라서 user는 아직 프록시 상태 — 실제 데이터가 안 채워져 있음
        assertThat(Hibernate.isInitialized(found.getUser())).isFalse();

        // when: user를 실제로 건드림 (이 시점에 users 테이블 SELECT가 추가로 나감)
        String name = found.getUser().getName();

        // then: 건드리고 나면 초기화됨
        assertThat(Hibernate.isInitialized(found.getUser())).isTrue();
        assertThat(name).isEqualTo("테스트유저");
    }
}
