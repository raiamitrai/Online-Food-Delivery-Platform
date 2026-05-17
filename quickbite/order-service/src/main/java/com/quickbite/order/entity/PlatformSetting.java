package com.quickbite.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSetting {
    @Id
    private String settingKey;
    private String settingValue;
}
