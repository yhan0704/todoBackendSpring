package backend.user.repository;

import backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // email은 PK가 아니라 해당 값이 존재한다는 보장이 없어서 Optional로 감쌈.
    // null을 그대로 반환하면 호출부가 null 체크를 빼먹어도 컴파일이 되고 나중에
    // NPE로 터지므로, "없을 수도 있다"는 걸 타입으로 강제해서 호출부(AuthService 등)가
    // orElseThrow 등으로 반드시 처리하도록 만듦
    Optional<User> findByEmail(String email);
}