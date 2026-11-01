package eu.oberonoss.tsc.cm.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing an application setting in the database.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Entity
@Table(name = "tsc_setting")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "settingKey")
public class TscSettingEntity {

    @Id
    @Column(name = "setting_key", length = 100, nullable = false)
    private String settingKey;

    @Column(name = "setting_value", length = 1000)
    private String settingValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
