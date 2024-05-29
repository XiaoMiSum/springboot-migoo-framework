package xyz.migoo.framework.infra.service.sys.configurer;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.infra.dal.dataobject.sys.ConfigurerDO;
import xyz.migoo.framework.infra.dal.mapper.sys.ConfigurerMapper;

import java.util.List;

@Service
@Slf4j
public class ConfigurerServiceImpl implements ConfigurerService {

    @Resource
    private ConfigurerMapper mapper;

    @Override
    public void save(ConfigurerDO configurer) {
        if (mapper.update(configurer) < 1) {
            mapper.insert(configurer);
        }
    }

    @Override
    public List<ConfigurerDO> getList() {
        return mapper.selectList();
    }
}
