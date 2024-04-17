package xyz.migoo.framework.security.core;

public interface BaseUser<T> {

    T getId();

    String getUsername();

    String getPassword();

    String getName();

    String getAvatar();

    Integer getStatus();

    String getSecretKey();

    Integer getBindAuthenticator();

    Integer getRequiredVerifyAuthenticator();
}
