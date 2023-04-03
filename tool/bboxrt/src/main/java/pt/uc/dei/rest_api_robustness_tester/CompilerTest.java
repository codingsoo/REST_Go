package pt.uc.dei.rest_api_robustness_tester;

import pt.uc.dei.rest_api_robustness_tester.specification.RestApiSetup;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class CompilerTest {

    private  String apiPath = null;
    private  String className = null;

    public CompilerTest(String apiPath  , String className){
        this.apiPath = apiPath;
        this.className = className;
    }

    public RestApiSetup compileClass(){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Compiling the code
        compiler.run(null, null, null, this.apiPath);

        String classDirGenerate = this.apiPath.replace(this.className + ".java", "");
        // Giving the path of the class directory where class file is generated..
        File classesDir = new File(classDirGenerate);
        // Load and instantiate compiled class.
        URLClassLoader classLoader;
        try {
            // Loading the class
            classLoader = URLClassLoader.newInstance(new URL[] { classesDir.toURI().toURL() });
            Class<?> cls;
            cls = Class.forName(className, true, classLoader);
            RestApiSetup instance = (RestApiSetup)cls.newInstance();
            return instance;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }
}
