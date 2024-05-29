package xyz.migoo.framework.infra.controller.sys.configurer.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestBodyVO {

    private String name;

    private String label;

    private String value;

    private List<Map<String, String>> options;
}
