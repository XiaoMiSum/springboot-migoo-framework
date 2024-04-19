package xyz.migoo.framework.security.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils implements BeanFactoryPostProcessor {

    private static PasswordEncoder passwordEncoder;

    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean verify(String ori, String hashed) {
        return passwordEncoder.matches(ori, hashed);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        passwordEncoder = beanFactory.getBean(PasswordEncoder.class);
    }
}
