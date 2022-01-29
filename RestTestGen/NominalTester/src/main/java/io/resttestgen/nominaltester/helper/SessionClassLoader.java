package io.resttestgen.nominaltester.helper;

import io.resttestgen.nominaltester.helper.exceptions.ClassLoaderNotInitializedException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class SessionClassLoader {
    private static SessionClassLoader sessionClassLoader = null;
    private final URLClassLoader urlClassLoader;

    private SessionClassLoader(String codegenJarPath) throws MalformedURLException {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL codegenJarURL = new File(codegenJarPath).toURI().toURL();
        URL[] urls = {codegenJarURL};
        urlClassLoader = URLClassLoader.newInstance(urls, cl);
    }

    public static SessionClassLoader createInstance(String codegenJarPath) throws MalformedURLException {
        if (sessionClassLoader == null) {
            sessionClassLoader = new SessionClassLoader(codegenJarPath);
        }
        return sessionClassLoader;
    }

    public static SessionClassLoader getInstance() throws ClassLoaderNotInitializedException {
        if (sessionClassLoader == null)
            throw new ClassLoaderNotInitializedException("Attempt to call getInstance without being initialized");
        return sessionClassLoader;
    }

    public URLClassLoader getUrlClassLoader() {
        return urlClassLoader;
    }

    public Class<?> loadClass(String classPath) throws ClassNotFoundException {
        return urlClassLoader.loadClass(classPath);
    }
}
