package xyz.migoo.framework.common.util.object;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ObjectUtils} 单元测试
 */
class ObjectUtilsTest {

    // ========== clone ==========

    @Test
    void clone_null() {
        assertThat(ObjectUtils.<String>clone(null)).isNull();
    }

    @Test
    void clone_deepCopy() {
        Person original = new Person("Alice", new ArrayList<>(List.of("t1", "t2")));
        Person copy = ObjectUtils.clone(original);

        // 拷贝结果相等但非同一实例
        assertThat(copy).isNotSameAs(original).isEqualTo(original);

        // 修改原对象后，拷贝不受影响（深拷贝）
        original.setName("Bob");
        original.getTags().add("t3");
        assertThat(copy.getName()).isEqualTo("Alice");
        assertThat(copy.getTags()).containsExactly("t1", "t2");
    }

    @Test
    void clone_withConsumer() {
        Person original = new Person("Alice", new ArrayList<>());
        Person copy = ObjectUtils.clone(original, person -> person.setName("changed"));
        assertThat(copy.getName()).isEqualTo("changed");
        assertThat(original.getName()).isEqualTo("Alice");

        // null 时不调用 consumer
        Person nullResult = ObjectUtils.clone(null, person -> person.setName("x"));
        assertThat(nullResult).isNull();
    }

    // ========== max ==========

    @Test
    void max_bothNull() {
        assertThat(ObjectUtils.<String>max(null, null)).isNull();
    }

    @Test
    void max_firstNull() {
        assertThat(ObjectUtils.max(null, "a")).isEqualTo("a");
        assertThat(ObjectUtils.max("a", null)).isEqualTo("a");
    }

    @Test
    void max_normal() {
        // 返回 compareTo 较大的一个
        assertThat(ObjectUtils.max(3, 5)).isEqualTo(5);
        assertThat(ObjectUtils.max(5, 3)).isEqualTo(5);
    }

    // ========== defaultIfNull ==========

    @Test
    void defaultIfNull() {
        assertThat(ObjectUtils.defaultIfNull(null, "x")).isEqualTo("x");
        assertThat(ObjectUtils.defaultIfNull(null, null, "y")).isEqualTo("y");
        // 返回第一个非 null 值
        assertThat(ObjectUtils.defaultIfNull(null, "x", "y")).isEqualTo("x");
    }

    @Test
    void defaultIfNull_allNull() {
        assertThat(ObjectUtils.<String>defaultIfNull(null, null)).isNull();
    }

    // ========== equalsAny ==========

    @Test
    void equalsAny_true() {
        assertThat(ObjectUtils.equalsAny("a", "b", "a")).isTrue();
        assertThat(ObjectUtils.equalsAny("a", "a")).isTrue();
    }

    @Test
    void equalsAny_false() {
        assertThat(ObjectUtils.equalsAny("a", "b", "c")).isFalse();
        assertThat(ObjectUtils.equalsAny("a")).isFalse();
    }

    /**
     * clone 测试用的可序列化实体
     */
    static class Person implements Serializable {

        private String name;
        private List<String> tags;

        Person() {
        }

        Person(String name, List<String> tags) {
            this.name = name;
            this.tags = tags;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        List<String> getTags() {
            return tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Person person)) {
                return false;
            }
            return Objects.equals(name, person.name) && Objects.equals(tags, person.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, tags);
        }
    }

}
