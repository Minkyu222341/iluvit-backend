package FIS.iLUVit.repository;

import FIS.iLUVit.domain.reports.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /*
        targetId로 Report를 불러옵니다.
     */
    Optional<Report> findByTargetId(Long targetId);

    /*
        postId에 해당하는 게시물 ID를 가진 Report 객체들의 targetId 속성을 null로, status 속성을 "DELETE"로 업데이트합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Report r " +
            "set r.targetId = null, r.status = 'DELETE' "+
            "where r.targetId =:postId ")
    void setTargetIsNullAndStatusIsDelete(@Param("postId") Long postId);

    /*
        commentIds에 해당하는 댓글 ID를 가진 Report 객체들의 targetId 속성을 null로, status 속성을 "DELETE"로 업데이트합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Report r " +
            "set r.targetId = null, r.status = 'DELETE' "+
            "where r.targetId in :commentIds")
    void setTargetIsNullAndStatusIsDelete(@Param("commentIds") List<Long> commentIds);
}