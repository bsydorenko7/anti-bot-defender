package knu.fit.kbzi.sydorenko.antibotdefender.repository;

import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedIpRepository extends JpaRepository<BlacklistedIp, Long> {

    boolean existsByIpAddress(String ipAddress);
}
