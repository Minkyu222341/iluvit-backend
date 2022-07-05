package FIS.iLUVit.repository;

import FIS.iLUVit.domain.ChatRoom;
import FIS.iLUVit.domain.Post;
import FIS.iLUVit.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByReceiverAndSenderAndPost(User receiver, User sender, Post post);

    @Query("select cr from ChatRoom cr " +
            "join fetch cr.post p " +
            "join fetch p.board b " +
            "left join fetch b.center c " +
            "where cr.receiver.id = :userId order by cr.createdDate desc ")
    Slice<ChatRoom> findByUser(@Param("userId") Long userId, @Param("userId") Pageable pageable);
}