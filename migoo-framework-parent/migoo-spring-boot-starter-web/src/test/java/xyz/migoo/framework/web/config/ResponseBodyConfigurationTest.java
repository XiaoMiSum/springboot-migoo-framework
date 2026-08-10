package xyz.migoo.framework.web.config;

import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.handler.ResponseBodyI18nAdvice;
import xyz.migoo.framework.web.core.handler.ResponseBodyStorageAdvice;
import xyz.migoo.framework.web.i18n.I18NMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link ResponseBodyConfiguration} 单元测试：@Bean 方法可直接脱离容器调用。
 */
class ResponseBodyConfigurationTest {

    @Test
    void globalResponseBodyStorageAdviceIsCreatable() {
        ResponseBodyStorageAdvice advice = new ResponseBodyConfiguration().globalResponseBodyStorageAdvice();
        assertThat(advice).isInstanceOf(ResponseBodyStorageAdvice.class);
    }

    @Test
    void globalResponseBodyI18nAdviceWrapsI18nMessage() {
        I18NMessage i18n = mock(I18NMessage.class);
        ResponseBodyI18nAdvice advice = new ResponseBodyConfiguration().globalResponseBodyI18nAdvice(i18n);
        assertThat(advice).isInstanceOf(ResponseBodyI18nAdvice.class);
    }
}
