package vn.teca.digitization.migrate.config.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import vn.teca.digitization.migrate.model.Auditor;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<Auditor> auditorProvider() {
        return new AuditorProvider();
    }
}
