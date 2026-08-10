package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.mybatis.core.dataobject.BaseUuidDO;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFieldHandlerTest {

    private final DefaultFieldHandler handler = new DefaultFieldHandler();

    static class TestBaseDO extends BaseDO {
    }

    static class TestUuidDO extends BaseUuidDO<TestUuidDO> {
    }

    private MetaObject metaObject(Object target) {
        return MetaObject.forObject(target, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
    }

    @Test
    void insertFillSetsAuditFields() {
        TestBaseDO target = new TestBaseDO();

        handler.insertFill(metaObject(target));

        assertThat(target.getCreatedAt()).isNotNull();
        assertThat(target.getUpdatedAt()).isNotNull();
        assertThat(target.getIsDeleted()).isFalse();
    }

    @Test
    void updateFillSetsOnlyUpdatedAt() {
        TestBaseDO target = new TestBaseDO();
        target.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

        handler.updateFill(metaObject(target));

        assertThat(target.getUpdatedAt()).isNotNull();
        assertThat(target.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void insertFillGeneratesUuidWhenMissing() {
        TestUuidDO target = new TestUuidDO();

        handler.insertFill(metaObject(target));

        assertThat(target.getId()).isNotNull();
    }

    @Test
    void insertFillKeepsExistingUuid() {
        TestUuidDO target = new TestUuidDO();
        UUID existing = UUID.randomUUID();
        target.setId(existing);

        handler.insertFill(metaObject(target));

        assertThat(target.getId()).isEqualTo(existing);
    }

    @Test
    void insertFillIgnoresNonBaseDo() {
        handler.insertFill(metaObject(new Object()));
        // 不抛异常即可
    }
}
