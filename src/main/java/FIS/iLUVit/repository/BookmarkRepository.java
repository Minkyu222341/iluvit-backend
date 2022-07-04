package FIS.iLUVit.repository;

import FIS.iLUVit.domain.Bookmark;
import FIS.iLUVit.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @Query("select b from Bookmark b join fetch b.user u join fetch b.board bd left join fetch bd.center c " +
            "where u.id = :userId")
    List<Bookmark> findWithUserAndBoard(@Param("userId") Long userId);

    @Query("select b from Bookmark b join fetch b.user u join fetch b.board bd left outer join bd.center c " +
            "where u.id = :userId and c.id is null ")
    List<Bookmark> findWithUserAndBoardCenterIsNull(@Param("userId") Long userId);

    @Query("select p from Post p join fetch p.board b where p.id in " +
            "(select max(p.id) from Post p where p.board.id in " +
            "(select b.board.id from Bookmark b where b.user.id = :userId) " +
            "group by p.board.id) and p.board.id = b.id")
    List<Post> findPostByBoard(@Param("userId") Long userId);

    @Query("select bm from Bookmark bm join fetch bm.board b where bm.user.id = :userId and b.center.id is null ")
    List<Bookmark> findBoardByUser(@Param("userId") Long userId);

    @Query("select bm from Bookmark bm join fetch bm.board b where bm.user.id = :userId and b.center.id = :centerId ")
    List<Bookmark> findBoardByUserAndCenter(@Param("userId") Long userId, @Param("centerId") Long centerId);

    @Modifying
    @Query("delete " +
            "from Bookmark b " +
            "where b.board.id in :boardIds " +
            "and b.user.id =:userId")
    void deleteAllByBoardAndUser(Long userId, List<Long> boardIds);

}