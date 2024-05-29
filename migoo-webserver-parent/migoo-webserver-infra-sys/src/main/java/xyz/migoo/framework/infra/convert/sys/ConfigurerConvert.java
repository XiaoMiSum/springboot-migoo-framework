package xyz.migoo.framework.infra.convert.sys;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.infra.controller.sys.configurer.vo.RequestBodyVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.ConfigurerDO;

import java.util.List;

@Mapper
public interface ConfigurerConvert {

    ConfigurerConvert INSTANCE = Mappers.getMapper(ConfigurerConvert.class);

    List<RequestBodyVO> convert(List<ConfigurerDO> beans);

    RequestBodyVO convert(ConfigurerDO bean);

    ConfigurerDO convert(RequestBodyVO bean);
}
