package knu.fit.kbzi.sydorenko.antibotdefender.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BlacklistRepository {

    private final JdbcTemplate jdbcTemplate;

    public BlacklistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void banIp(String ipAddress) {
        String sql = "INSERT INTO blacklist (ip_address) VALUES (?) ON DUPLICATE KEY UPDATE ip_address = ip_address";
        jdbcTemplate.update(sql, ipAddress);
    }

}
