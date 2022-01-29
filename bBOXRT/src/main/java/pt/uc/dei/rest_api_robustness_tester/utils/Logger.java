package pt.uc.dei.rest_api_robustness_tester.utils;

//TODO: implement Logger class with static methods for info, trace, debug, etc
//TODO: replace all sout and serr calls in this project by their corresponding logging equivalents
public interface Logger
{
    public enum Level{Trace, Debug, Info, Warn, Error, Fatal}
    
    void SetLevel(Level level);
    
    void Trace(String message);
    void Debug(String message);
    void Info(String message);
    void Warn(String message);
    void Error(String message);
    void Fatal(String message);
}
