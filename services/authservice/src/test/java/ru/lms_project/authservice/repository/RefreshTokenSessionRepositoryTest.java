package ru.lms_project.authservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.lms_project.authservice.model.RefreshTokenSession;
import ru.lms_project.authservice.model.RefreshTokenStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class RefreshTokenSessionRepositoryTest {
    @Autowired
    RefreshTokenSessionRepository repository;

    @Test
    void markActiveAsAlreadyUsed_updatesOnce() {
        UUID sid = UUID.randomUUID();
        RefreshTokenSession s = new RefreshTokenSession();
        s.setId(sid);
        s.setUserId(UUID.randomUUID());
        s.setTokenHash("hash");
        s.setStatus(RefreshTokenStatus.ACTIVE);
        s.setExpires(Instant.now().plus(1, ChronoUnit.DAYS));
        repository.save(s);

        int first = repository.markActiveAsAlreadyUsed(sid);
        int second = repository.markActiveAsAlreadyUsed(sid);

        assertEquals(1, first);
        assertEquals(0, second);
        assertEquals(RefreshTokenStatus.ALREADY_USED, repository.findById(sid).orElseThrow().getStatus());
    }
}
