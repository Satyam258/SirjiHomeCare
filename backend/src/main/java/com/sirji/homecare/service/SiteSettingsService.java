package com.sirji.homecare.service;

import com.sirji.homecare.domain.SiteSettings;
import com.sirji.homecare.repository.SiteSettingsRepository;
import com.sirji.homecare.web.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class SiteSettingsService {
    private static final String BULK_THRESHOLD_KEY = "bulk.quantity.threshold";
    private final SiteSettingsRepository settingsRepository;

    public SiteSettingsService(SiteSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> list() {
        return settingsRepository.findAllByOrderBySettingKeyAsc().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(SiteSettings::getSettingKey, SiteSettings::getSettingValue));
    }

    @Transactional
    public Map.Entry<String, String> update(String key, String value) {
        if (!BULK_THRESHOLD_KEY.equals(key)) {
            throw new ResourceNotFoundException("Setting not found.");
        }
        int threshold;
        try {
            threshold = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Bulk quantity threshold must be a whole number.");
        }
        if (threshold < 1 || threshold > 100000) {
            throw new IllegalArgumentException("Bulk quantity threshold must be between 1 and 100000.");
        }
        SiteSettings setting = settingsRepository.findBySettingKey(key).orElseGet(() -> {
            SiteSettings created = new SiteSettings();
            created.setSettingKey(key);
            return created;
        });
        setting.setSettingValue(Integer.toString(threshold));
        SiteSettings saved = settingsRepository.save(setting);
        return Map.entry(saved.getSettingKey(), saved.getSettingValue());
    }
}
