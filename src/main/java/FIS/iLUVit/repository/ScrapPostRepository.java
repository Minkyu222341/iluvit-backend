package FIS.iLUVit.repository;

import FIS.iLUVit.domain.ScrapPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScrapPostRepository extends JpaRepository<ScrapPost, Long> {

    @Query("select sp " +
            "from ScrapPost sp " +
            "join fetch sp.post p " +
            "join fetch p.user u " +
            "join fetch p.board " +
            "join fetch sp.scrap s " +
            "where s.id =:scrapId " +
            "and s.user.id =:userId")
    Slice<ScrapPost> findByScrapWithPost(@Param("userId") Long userId, @Param("scrapId") Long scrapId, Pageable pageable);

    @Query("select sp " +
            "from ScrapPost sp " +
            "join sp.scrap s " +
            "where sp.id =:scrapPostId " +
            "and s.user.id =:userId")
    Optional<ScrapPost> findByScrapAndPost(@Param("userId") Long userId, @Param("scrapPostId") Long scrapPostId);
}