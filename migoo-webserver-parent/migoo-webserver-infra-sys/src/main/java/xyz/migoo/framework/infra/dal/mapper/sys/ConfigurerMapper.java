package xyz.migoo.framework.infra.dal.mapper.sys;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.infra.dal.dataobject.sys.ConfigurerDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;

@Mapper
public interface ConfigurerMapper extends BaseMapperX<ConfigurerDO> {

    default int update(ConfigurerDO data) {
        return update(data, new LambdaUpdateWrapper<ConfigurerDO>().eq(ConfigurerDO::getName, data.getName()));
    }
}
