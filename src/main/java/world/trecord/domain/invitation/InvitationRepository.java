package world.trecord.domain.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE InvitationEntity ie " +
            "SET ie.status = :status, " +
            "ie.deletedDateTime = NOW() " +
            "WHERE ie.userToEntity.id = :userToId AND ie.feedEntity.id = :feedId")
    void updateStatusAndDeleteByUserEntityIdAndFeedEntityId(@Param("userToId") Long userToId,
                                                            @Param("feedId") Long feedId,
                                                            @Param("status") InvitationStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE InvitationEntity ie " +
            "SET ie.deletedDateTime = NOW() " +
            "where ie.feedEntity.id = :feedId")
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);

    void deleteByUserToEntityIdAndFeedEntityId(Long userToId, Long feedId);
}
