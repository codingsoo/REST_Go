package io.resttestgen.nominaltester.models;

public class Authentication {
    private Class<?> authenticationClass;

    public Class<?> getAuthenticationClass() {
        return authenticationClass;
    }

    public void setAuthenticationClass(Class<?> authenticationClass) {
        this.authenticationClass = authenticationClass;
    }

    public boolean isClientAuthenticated() {
        return isClientAuthenticated;
    }

    public void setClientAuthenticated(boolean clientAuthenticated) {
        isClientAuthenticated = clientAuthenticated;
    }

    private boolean isClientAuthenticated;


}
