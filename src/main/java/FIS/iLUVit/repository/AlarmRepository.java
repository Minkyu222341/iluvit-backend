package FIS.iLUVit.repository;

import FIS.iLUVit.domain.User;
import FIS.iLUVit.domain.alarms.Alarm;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    /*
        유저 id를 파라미터로 정하여 유저 id에 맞는 활동을 조회한다.
     */
    @Query("select alarm from Alarm alarm " +
            "where alarm.user.id =:userId and " +
            "alarm.dtype <> 'PresentationFullAlarm' and alarm.dtype <> 'ConvertedToParticipateAlarm' and alarm.dtype <> 'PresentationCreatedAlarm' and alarm.dtype <> 'PresentationPeriodClosedAlarm' ")
    Slice<Alarm> findActiveByUser(@Param("userId") Long userId, Pageable pageable);

    /*
        유저 id를 파라미터로 정하여 유저 id에 맞는 설명회를 조회한다.
     */
    @Query("select alarm from Alarm alarm " +
            "where alarm.user.id =:userId and " +
            "(alarm.dtype = 'PresentationFullAlarm' or alarm.dtype = 'ConvertedToParticipateAlarm' or alarm.dtype = 'PresentationCreatedAlarm' or alarm.dtype = 'PresentationPeriodClosedAlarm')")
    Slice<Alarm> findPresentationByUser(@Param("userId") Long userId, Pageable pageable);

    /*
        유저 id와 여러개의 알람 id들을 파라미터 값으로 받아 알람을 삭제한다.
     */
    @Modifying
    @Query("delete from Alarm alarm where alarm.id in :alarmIds and alarm.user.id = :userId")
    Integer deleteByIds(@Param("userId") Long userId, @Param("alarmIds") List<Long> alarmIds);

    /*
        게시글 id를 파라미터로 받아서 게시글은 null이 아니다를 세팅한다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update PostAlarm pa set pa.postId = null where pa.postId = :postId")
    Integer setPostIsNull(@Param("postId") Long postId);

    /*
        유저의 모든 알람을 삭제한다.
    */
    void deleteAllByUser(User user);
}
