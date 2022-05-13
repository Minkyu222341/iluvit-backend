package FIS.iLUVit.repository;

import FIS.iLUVit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByLoginId(String loginId);

}
