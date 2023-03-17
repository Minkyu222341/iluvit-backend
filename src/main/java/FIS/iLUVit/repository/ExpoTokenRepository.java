package FIS.iLUVit.repository;

import FIS.iLUVit.domain.ExpoToken;
import FIS.iLUVit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExpoTokenRepository extends JpaRepository<ExpoToken, Long> {

    List<ExpoToken> findByUser(User user);

    Optional<ExpoToken> findByToken(String token);

    void deleteByTokenAndUser(String token, User user);

    Optional<ExpoToken> findByTokenAndUser(String token, User user);

    void deleteByTokenIn(Collection<String> tokens);

    void deleteAllByUser(User user);
}
