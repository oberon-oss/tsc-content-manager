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
@SuppressWarnings({"JavadocDeclaration", "JpaDataSourceORMInspection"})
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

    // @formatter:off
    /**
     * The setting key.
     * --- SETTER ---
     * Sets the setting key.
     *
     * @param settingKey the setting key to set
     * --- GETTER ---
     * Returns the setting key.
     *
     * @return the setting key
     * @since 1.0.0
     */
    // @formatter:on
    @Id
    @Column(name = "setting_key", length = 100, nullable = false)
    private String settingKey;

    // @formatter:off
    /**
     * The setting value.
     * --- SETTER ---
     * Sets the setting value.
     *
     * @param settingValue the setting value to set
     * --- GETTER ---
     * Returns the setting value.
     *
     * @return the setting value
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "setting_value", length = 1000)
    private String settingValue;

    // @formatter:off
    /**
     * The description of the setting.
     * --- SETTER ---
     * Sets the description of the setting.
     *
     * @param description the description to set
     * --- GETTER ---
     * Returns the description of the setting.
     *
     * @return the description of the setting
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "description")
    private String description;

    // @formatter:off
    /**
     * The timestamp when the setting was last updated.
     * --- SETTER ---
     * Sets the timestamp when the setting was last updated.
     *
     * @param updatedAt the timestamp when the setting was last updated to set
     * --- GETTER ---
     * Returns the timestamp when the setting was last updated.
     *
     * @return the timestamp when the setting was last updated
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "updated_at")
    private Instant updatedAt;
}
