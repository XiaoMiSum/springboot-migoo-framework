package xyz.migoo.framework.common.id;

/**
 * 分布式 ID 生成器 SPI（零 Spring 依赖，可直接 new，或注册为 Bean 由容器注入）
 *
 * <p>实现约定：</p>
 * <ul>
 *     <li>同一实现内生成的 ID <b>时间有序</b>（{@link UuidV7IdGenerator} 按字符串排序即时间序，
 *         {@link SnowflakeIdGenerator} 按 long 排序即时间序）；</li>
 *     <li>不同实现之间的 ID <b>格式不同、不可比较</b>；</li>
 *     <li>实现须保证多线程下的唯一性。</li>
 * </ul>
 *
 * @author xiaomi
 */
public interface IdGenerator {

    /**
     * 生成下一个分布式 ID
     *
     * <p>返回字符串形态，便于作为 VARCHAR 主键 / 请求 ID / 对外编号；
     * 纯数字（BIGINT）场景请使用具体实现的专用方法，如
     * {@link SnowflakeIdGenerator#nextLong()}。</p>
     *
     * @return 下一个 ID
     */
    String nextId();
}
