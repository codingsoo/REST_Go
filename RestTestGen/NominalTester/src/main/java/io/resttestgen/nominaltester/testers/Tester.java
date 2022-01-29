package io.resttestgen.nominaltester.testers;

import io.resttestgen.nominaltester.helper.ReflectionHelper;
import io.resttestgen.nominaltester.helper.SessionClassLoader;
import io.resttestgen.nominaltester.helper.exceptions.ClassLoaderNotInitializedException;
import io.resttestgen.nominaltester.models.Authentication;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.coverage.Coverage;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Tester class contains methods and fields required to test operations
 * E.g. It has a response dictionary which is used during the parameter generation and
 * the method testOperation to execute the operation, getting the coverage
 */
public abstract class Tester {

    protected static String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0";
    protected static int STARTING_SLEEP_TIME = 5000; // starting sleep time if service replies with 423 status code

    static final Logger logger = LogManager.getLogger(Tester.class);

    protected Authentication authentication;
    protected Class<?> resetHookClass;
    protected Class<?> authHookClass;

    protected OpenAPI openAPI;
    protected Map<String, List<OperationInfo>> operationsPerApiClass;

    protected int currentSleepTime;

    public Tester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) {
        this.openAPI = openAPI;
        this.authentication = new Authentication();
        this.operationsPerApiClass = operationsPerApiClass;
        setUserAgent(DEFAULT_USER_AGENT);
        setSleepTime(STARTING_SLEEP_TIME);
    }

    /**
     * Change user-agent for every requests
     * @param userAgent string representing new user-agent
     */
    public void setUserAgent(String userAgent) {
        try {
            SessionClassLoader sessionClassLoader = SessionClassLoader.getInstance();
            Class<?> configurationClass = sessionClassLoader.loadClass("io.swagger.client.Configuration");
            Method getDefaultApiClient = configurationClass.getMethod("getDefaultApiClient");
            Class<?> apiClientClass = sessionClassLoader.loadClass("io.swagger.client.ApiClient");
            Object defaultApiClient = getDefaultApiClient.invoke(null);
            Method setUserAgent = apiClientClass.getMethod("setUserAgent", String.class);
            setUserAgent.invoke(defaultApiClient, userAgent);
        } catch (ClassLoaderNotInitializedException | ClassNotFoundException
                | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Cannot set user-agent on default ApiClient", e);
        }
    }

    /**
     * Set ResetHook class
     * @param resetHookClass reset hook class
     */
    public void setResetHookClass(Class<?> resetHookClass) {
        this.resetHookClass = resetHookClass;
        Method resetMethod = ReflectionHelper.getMethodByName(resetHookClass, "reset");
        if (resetMethod == null) {
            logger.error("No method: public static reset()");
            this.resetHookClass = null;
        }
    }

    /**
     * Set AuthHook class
     * @param authHookClass auth hook class
     */
    public void setAuthHookClass(Class<?> authHookClass) {
        this.authHookClass = authHookClass;
        Method authMethod = ReflectionHelper.getMethodByName(this.authHookClass, "authenticate");
        if (authMethod == null) {
            logger.error("No method: public static authenticate()");
            this.authHookClass = null;
        }
    }

    /**
     * Calls the 'reset' method defined in resetHookClass
     * @return result of 'reset' method in hook class
     */
    protected boolean reset() {
        if (this.resetHookClass == null) return false;
        Method resetMethod = ReflectionHelper.getMethodByName(this.resetHookClass, "reset");
        try {
            Object resetResult = resetMethod.invoke(null);
            return (boolean)resetResult;
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Error while invoking method 'reset' on ", e);
        }
        return false;
    }

    /**
     * Call the method authenticate() of the class authentication class
     * @return true method authenticate is called correctly, false otherwise
     */
    protected boolean authenticate() {
        boolean authSuccess = false;

        if (this.authHookClass != null) {
            Method authenticate = ReflectionHelper.getMethodByName(this.authHookClass, "authenticate");
            try {
                Object authenticationResult = authenticate.invoke(null);
                authSuccess = (boolean)authenticationResult;
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Error while invoking method authenticate on ", e);
            }
        }

        authentication.setClientAuthenticated(authSuccess);
        authentication.setAuthenticationClass(this.authHookClass);

        return authSuccess;
    }

    public void setSleepTime(int startingSleepTime) {
        this.currentSleepTime = startingSleepTime;
    }

    public void resetCurrentSpleepTime() {
        setSleepTime(STARTING_SLEEP_TIME);
    }

    public void pauseTestingAccordingToCurrentSleepTime() {
        try {
            logger.info("Sleep time " + currentSleepTime);
            Thread.sleep(currentSleepTime);
            currentSleepTime = currentSleepTime * 2;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the service following a specific strategy
     * @return Coverage
     */
    public abstract Coverage run();
}
