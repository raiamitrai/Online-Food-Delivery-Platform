package com.quickbite.order.controller;

import com.quickbite.order.entity.PlatformSetting;
import com.quickbite.order.repository.PlatformSettingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/settings")
public class SettingController {

    private final PlatformSettingRepository settingRepository;

    public SettingController(PlatformSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(settingRepository.findById(key)
                .map(PlatformSetting::getSettingValue)
                .orElse("10")); // Default to 10 if not found
    }

    @PostMapping("/{key}")
    public ResponseEntity<Void> updateSetting(@PathVariable String key, @RequestParam String value) {
        PlatformSetting setting = PlatformSetting.builder()
                .settingKey(key)
                .settingValue(value)
                .build();
        settingRepository.save(setting);
        return ResponseEntity.ok().build();
    }
}
