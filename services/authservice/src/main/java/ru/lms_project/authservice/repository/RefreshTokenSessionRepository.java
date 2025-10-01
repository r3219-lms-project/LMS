package ru.lms_project.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.lms_project.authservice.model.RefreshTokenSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    List<RefreshTokenSession> findAllByUserId(UUID userId);

    void deleteByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update RefreshTokenSession r
           set r.status = ru.lms_project.authservice.model.RefreshTokenStatus.REVOKED
         where r.userId = :userId
           and r.status = ru.lms_project.authservice.model.RefreshTokenStatus.ACTIVE
    """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update RefreshTokenSession s
           set s.status = ru.lms_project.authservice.model.RefreshTokenStatus.ALREADY_USED
         where s.id = :sid
           and s.status = ru.lms_project.authservice.model.RefreshTokenStatus.ACTIVE
    """)
    int markActiveAsAlreadyUsed(@Param("sid") UUID sid);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update RefreshTokenSession s
           set s.status = ru.lms_project.authservice.model.RefreshTokenStatus.EXPIRED
         where s.id = :sid
           and s.status = ru.lms_project.authservice.model.RefreshTokenStatus.ACTIVE
    """)
    int expireIfActive(@Param("sid") UUID sid);
}
