package project.seatsence.src.user.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import project.seatsence.global.entity.BaseTimeAndStateEntity.State;
import project.seatsence.src.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmailAndState(String email, State state);

    Boolean existsByNicknameAndState(String nickname, State state);

    User save(User user);

    Optional<User> findByEmail(String email);
}