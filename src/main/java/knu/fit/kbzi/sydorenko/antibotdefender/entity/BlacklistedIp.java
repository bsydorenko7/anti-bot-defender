package knu.fit.kbzi.sydorenko.antibotdefender.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_ips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    private String reason;

    @Column(name = "blocked_at", nullable = false, updatable = false)
    private LocalDateTime blockedAt = LocalDateTime.now();
}

