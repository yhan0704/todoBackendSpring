package backend.user.repository;

import backend.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // email은 PK가 아니라 해당 값이 존재한다는 보장이 없어서 Optional로 감쌈.
    // null을 그대로 반환하면 호출부가 null 체크를 빼먹어도 컴파일이 되고 나중에
    // NPE로 터지므로, "없을 수도 있다"는 걸 타입으로 강제해서 호출부(AuthService 등)가
    // orElseThrow 등으로 반드시 처리하도록 만듦
    Optional<User> findByEmail(String email);

    // 로그인 응답에 todos를 함께 실어야 할 때만 사용. findByEmail에 EntityGraph를 붙이면
    // refresh/logout 등 todos가 필요 없는 호출부까지 매번 todos를 fetch join하게 되므로 분리함.
    // 메서드 이름을 findBy...로 자동 파생시키면 "Email" 뒤의 "WithTodos"를 email(String) 위의
    // 중첩 프로퍼티로 오인해 파싱 에러가 나서, @Query로 조건절을 직접 명시함
    @EntityGraph(attributePaths = "todos")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithTodos(@Param("email") String email);
}