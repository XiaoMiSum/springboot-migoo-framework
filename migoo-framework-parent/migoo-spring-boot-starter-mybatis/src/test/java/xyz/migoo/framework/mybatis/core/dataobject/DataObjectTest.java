package xyz.migoo.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataObjectTest {

    static class TestAutoIncDO extends BaseAutoIncDO<Long, TestAutoIncDO> {
    }

    static class TestUuidDO extends BaseUuidDO<TestUuidDO> {
    }

    @Test
    void baseDoHasAuditFieldsWithFillAnnotations() throws Exception {
        Field createdAt = BaseDO.class.getDeclaredField("createdAt");
        TableField createdAtTableField = createdAt.getAnnotation(TableField.class);
        assertThat(createdAtTableField).isNotNull();
        assertThat(createdAtTableField.fill()).isEqualTo(FieldFill.INSERT);

        Field updatedAt = BaseDO.class.getDeclaredField("updatedAt");
        TableField updatedAtTableField = updatedAt.getAnnotation(TableField.class);
        assertThat(updatedAtTableField).isNotNull();
        assertThat(updatedAtTableField.fill()).isEqualTo(FieldFill.INSERT_UPDATE);

        Field isDeleted = BaseDO.class.getDeclaredField("isDeleted");
        assertThat(isDeleted.getAnnotation(TableLogic.class)).isNotNull();
        TableField isDeletedTableField = isDeleted.getAnnotation(TableField.class);
        assertThat(isDeletedTableField).isNotNull();
        assertThat(isDeletedTableField.fill()).isEqualTo(FieldFill.INSERT);
    }

    @Test
    void baseUuidDoHasUuidTableId() throws Exception {
        Field id = BaseUuidDO.class.getDeclaredField("id");
        assertThat(id.getType()).isEqualTo(UUID.class);
        assertThat(id.getAnnotation(TableId.class)).isNotNull();
    }

    @Test
    void baseAutoIncDoHasAutoTableId() throws Exception {
        Field id = BaseAutoIncDO.class.getDeclaredField("id");
        TableId tableId = id.getAnnotation(TableId.class);
        assertThat(tableId).isNotNull();
        assertThat(tableId.type()).isEqualTo(IdType.AUTO);
    }

    @Test
    void baseUuidDoFluentSetIdReturnsSelf() {
        TestUuidDO target = new TestUuidDO();
        UUID id = UUID.randomUUID();

        TestUuidDO result = target.setId(id);

        assertThat(result).isSameAs(target);
        assertThat(target.getId()).isEqualTo(id);
    }

    @Test
    void baseAutoIncDoFluentSetIdReturnsSelf() {
        TestAutoIncDO target = new TestAutoIncDO();

        TestAutoIncDO result = target.setId(100L);

        assertThat(result).isSameAs(target);
        assertThat(target.getId()).isEqualTo(100L);
    }

    @Test
    void baseDoIsSerializable() {
        assertThat(java.io.Serializable.class.isAssignableFrom(BaseDO.class)).isTrue();
    }
}
