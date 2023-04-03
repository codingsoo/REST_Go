package pt.uc.dei.rest_api_robustness_tester.security;

public class ApiKey extends SecurityScheme
{
    public enum Location
    {
        Query(),
        Header(),
        Cookie();
        
        private final String value;
        
        private Location()
        {
            this.value = this.name().toLowerCase();
        }
        
        public String Value()
        {
            return this.value;
        }
        
        public static Location GetLocationForValue(String value)
        {
            for(Location loc : Location.values())
                if(value.equalsIgnoreCase(loc.Value()))
                    return loc;
            return null;
        }
    }
    
    public String name = null;
    public Location location = null;
    
    public ApiKey()
    {
        super(Type.ApiKey);
    }
}
