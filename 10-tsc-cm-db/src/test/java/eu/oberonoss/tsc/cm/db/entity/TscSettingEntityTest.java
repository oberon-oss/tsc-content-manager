package eu.oberonoss.tsc.cm.db.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TscSettingEntityTest {

    @Test
    void testEntityPropertiesAndBuilder() {
        Instant now = Instant.now();
        TscSettingEntity entity = TscSettingEntity.builder()
                .settingKey("app.theme")
                .settingValue("dark")
                .description("Application UI Theme")
                .updatedAt(now)
                .build();

        assertThat(entity.getSettingKey()).isEqualTo("app.theme");
        assertThat(entity.getSettingValue()).isEqualTo("dark");
        assertThat(entity.getDescription()).isEqualTo("Application UI Theme");
        assertThat(entity.getUpdatedAt()).isEqualTo(now);

        entity.setSettingValue("light");
        assertThat(entity.getSettingValue()).isEqualTo("light");

        TscSettingEntity copy = entity.toBuilder().settingValue("contrast").build();
        assertThat(copy.getSettingKey()).isEqualTo("app.theme");
        assertThat(copy.getSettingValue()).isEqualTo("contrast");
    }

    @Test
    void testEqualsAndHashCode() {
        TscSettingEntity entity1 = TscSettingEntity.builder().settingKey("k1").settingValue("v1").build();
        TscSettingEntity entity2 = TscSettingEntity.builder().settingKey("k1").settingValue("v2").build();
        TscSettingEntity entity3 = TscSettingEntity.builder().settingKey("k2").settingValue("v1").build();

        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).hasSameHashCodeAs(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1.toString()).contains("k1");
    }

    @Test
    void testNoArgsConstructor() {
        TscSettingEntity entity = new TscSettingEntity();
        entity.setSettingKey("test.key");
        assertThat(entity.getSettingKey()).isEqualTo("test.key");
    }
}
