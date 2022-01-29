package pt.uc.dei.rest_api_robustness_tester.response;

import java.util.Objects;

public class StatusCode
{
    public static final StatusCode STATUS_CODE_DEFAULT = new StatusCode(0);
    public static final StatusCode STATUS_CODE_1XX = new StatusCode(1);
    public static final StatusCode STATUS_CODE_2XX = new StatusCode(2);
    public static final StatusCode STATUS_CODE_3XX = new StatusCode(3);
    public static final StatusCode STATUS_CODE_4XX = new StatusCode(4);
    public static final StatusCode STATUS_CODE_5XX = new StatusCode(5);
    
    private final int statusCode;
    
    private StatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }
    
    public int ToInt()
    {
        return this.statusCode;
    }
    
    public static StatusCode FromInt(int value)
    {
        return new StatusCode(value);
    }
    
    public static StatusCode FromString(String value)
    {
        String statusCodeClean = value.trim().toLowerCase();
        switch(statusCodeClean)
        {
            case "default":
                return STATUS_CODE_DEFAULT;
            case "1xx":
                return STATUS_CODE_1XX;
            case "2xx":
                return STATUS_CODE_2XX;
            case "3xx":
                return STATUS_CODE_3XX;
            case "4xx":
                return STATUS_CODE_4XX;
            case "5xx":
                return STATUS_CODE_5XX;
        }
        
        return FromInt(Integer.parseInt(statusCodeClean));
    }
    
    public boolean Is1xx()
    {
        return Is1xx(this.statusCode);
    }
    public static boolean Is1xx(StatusCode statusCode)
    {
        return Is1xx(statusCode.statusCode);
    }
    public static boolean Is1xx(int statusCode)
    {
        return IsBetween(statusCode, 100, 199) || statusCode == STATUS_CODE_1XX.statusCode;
    }
    
    public boolean Is2xx()
    {
        return Is2xx(this.statusCode);
    }
    public static boolean Is2xx(StatusCode statusCode)
    {
        return Is2xx(statusCode.statusCode);
    }
    public static boolean Is2xx(int statusCode)
    {
        return IsBetween(statusCode, 200, 299) || statusCode == STATUS_CODE_2XX.statusCode;
    }
    
    public boolean Is3xx()
    {
        return Is3xx(this.statusCode);
    }
    public static boolean Is3xx(StatusCode statusCode)
    {
        return Is3xx(statusCode.statusCode);
    }
    public static boolean Is3xx(int statusCode)
    {
        return IsBetween(statusCode, 300, 399) || statusCode == STATUS_CODE_3XX.statusCode;
    }
    
    public boolean Is4xx()
    {
        return Is4xx(this.statusCode);
    }
    public static boolean Is4xx(StatusCode statusCode)
    {
        return Is4xx(statusCode.statusCode);
    }
    public static boolean Is4xx(int statusCode)
    {
        return IsBetween(statusCode, 400, 499) || statusCode == STATUS_CODE_4XX.statusCode;
    }
    
    public boolean Is5xx()
    {
        return Is5xx(this.statusCode);
    }
    public static boolean Is5xx(StatusCode statusCode)
    {
        return Is5xx(statusCode.statusCode);
    }
    public static boolean Is5xx(int statusCode)
    {
        return IsBetween(statusCode, 500, 599) || statusCode == STATUS_CODE_5XX.statusCode;
    }
    
    private static boolean IsBetween(int statusCode, int min, int max)
    {
        return statusCode >= min && statusCode <= max;
    }
    
    @Override
    public String toString()
    {
        return "" + this.statusCode;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof StatusCode))
            return false;
        return ((StatusCode)obj).statusCode == this.statusCode;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(statusCode);
    }
}
