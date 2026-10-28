package com.sirji.homecare.repository;

import com.sirji.homecare.domain.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, UUID> {
    Optional<SiteSettings> findBySettingKey(String settingKey);
    List<SiteSettings> findAllByOrderBySettingKeyAsc();
}
