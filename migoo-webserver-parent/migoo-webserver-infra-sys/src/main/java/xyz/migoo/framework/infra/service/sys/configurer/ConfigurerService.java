package xyz.migoo.framework.infra.service.sys.configurer;

import xyz.migoo.framework.infra.dal.dataobject.sys.ConfigurerDO;

import java.util.List;

public interface ConfigurerService {

    void save(ConfigurerDO configurerDO);

    List<ConfigurerDO> getList();
}
