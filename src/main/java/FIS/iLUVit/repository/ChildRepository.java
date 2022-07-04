package FIS.iLUVit.repository;

import FIS.iLUVit.domain.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {
    @Query("select c " +
            "from Child c " +
            "join c.parent p " +
            "join c.center ct " +
            "where p.id = :userId " +
            "and ct.id = :centerId")
    Child findByParentAndCenter(@Param("userId") Long userId, @Param("centerId") Long centerId);

    @Query("select c " +
            "from Child c " +
            "join fetch c.center " +
            "where c.id =:childId " +
            "and c.parent.id =:userId")
    Optional<Child> findByIdWithParentAndCenter(@Param("userId") Long userId, @Param("childId") Long childId);

    @Query("select c " +
            "from Child c " +
            "join fetch c.center " +
            "where c.parent.id =:userId")
    List<Child> findByUserWithCenter(@Param("userId") Long userId);

    @Modifying
    @Query("update Child c " +
            "set c.approval = 'ACCEPT' " +
            "where c.id =:childId " +
            "and c.center.id =:centerId")
    void acceptChild(@Param("childId") Long childId,@Param("centerId") Long centerId);

    @Modifying(clearAutomatically = true)
    @Query("update Child c " +
            "set c.approval = 'REJECT' " +
            "where c.id =:childId")
    void fireChild(@Param("childId") Long childId);
}