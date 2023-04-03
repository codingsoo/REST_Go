package pt.uc.dei.rest_api_robustness_tester;

import static pt.uc.dei.rest_api_robustness_tester.utils.Config.UNKNOWN_PORT;

public class Server
{
    public enum Scheme
    {
        HTTP("http", 80),
        HTTPS("https", 443),
        UNKNOWN("<scheme>", UNKNOWN_PORT);
        
        private final String value;
        private final int defaultPort;
        
        private Scheme(String value, int defaultPort)
        {
            this.value = value;
            this.defaultPort = defaultPort;
        }
        
        public String Value()
        {
            return this.value;
        }
        
        public int DefaultPort()
        {
            return this.defaultPort;
        }
        
        public static Scheme GetSchemeForValue(String value)
        {
            for(Scheme p : values())
                if(p.Value().equalsIgnoreCase(value))
                    return p;
            
            return UNKNOWN;
        }
    
        public static Scheme GetSchemeForPort(int port)
        {
            for(Scheme p : values())
                if(p.DefaultPort() == port)
                    return p;
        
            return UNKNOWN;
        }
    }
    
    public static final String SCHEME_SEP = "://";
    public static final String PORT_SEP = ":";
    public static final String PATH_SEP = "/";
    public static final String QUERY_SEP = "?";
    public static final String FRAGMENT_SEP = "#";
    
    private Scheme scheme;
    private final String host;
    private final int port;
    private final String remainder;
    
    public Server(String url)
    {
        this.scheme = ParseScheme(url);
        this.host = ParseHost(url, scheme);
        this.port = ParsePort(url, host, scheme);
        this.remainder = ParseRemainder(url, scheme, host, port);
        this.scheme = RecheckScheme(port);
    }
    
    private Scheme ParseScheme(String url)
    {
        for(Scheme p : Scheme.values())
            if(url.toLowerCase().startsWith(p.Value() + SCHEME_SEP))
                return p;
        
        return Scheme.UNKNOWN;
    }
    
    public Scheme GetScheme()
    {
        return this.scheme;
    }
    
    public boolean HasValidScheme()
    {
        return this.scheme != Scheme.UNKNOWN;
    }
    
    private String ParseHost(String url, Scheme scheme)
    {
        String withoutScheme = RemoveSchemeIfExists(url, scheme);
        
        if(withoutScheme.contains(PORT_SEP))
            return withoutScheme.substring(0, withoutScheme.indexOf(PORT_SEP));
        else if(withoutScheme.contains(PATH_SEP))
            return withoutScheme.substring(0, withoutScheme.indexOf(PATH_SEP));
        else if(withoutScheme.contains(QUERY_SEP))
            return withoutScheme.substring(0, withoutScheme.indexOf(QUERY_SEP));
        else if(withoutScheme.contains(FRAGMENT_SEP))
            return withoutScheme.substring(0, withoutScheme.indexOf(FRAGMENT_SEP));
        
        return withoutScheme;
    }
    
    public String GetHost()
    {
        return this.host;
    }
    
    private int ParsePort(String url, String host, Scheme scheme)
    {
        int portNumber = UNKNOWN_PORT;
        String withoutScheme = RemoveSchemeIfExists(url, scheme);
        String withoutHost = withoutScheme.replace(host, "");
        
        if(withoutHost.startsWith(PORT_SEP))
        {
            StringBuilder portPart = new StringBuilder();
            for(char c : withoutHost.substring(1).toCharArray())
            {
                if (Character.isDigit(c))
                    portPart.append(c);
                else
                    break;
            }
            
            try
            {
                portNumber = Integer.parseInt(portPart.toString());
            }
            catch(Exception ignored) {}
        }
        
        if(scheme != Scheme.UNKNOWN && portNumber == UNKNOWN_PORT)
            portNumber = scheme.DefaultPort();
        
        return portNumber;
    }
    
    public int GetPort()
    {
        return this.port;
    }
    
    public boolean HasValidPort()
    {
        return this.port != UNKNOWN_PORT;
    }
    
    private String ParseRemainder(String url, Scheme scheme, String host, int port)
    {
        String withoutScheme = RemoveSchemeIfExists(url, scheme);
        String withoutHost = withoutScheme.replace(host, "");
        String remainder;
        
        if(withoutHost.startsWith(PORT_SEP))
        {
            if(withoutHost.startsWith(PORT_SEP + port))
                remainder = withoutHost.substring((PORT_SEP + port).length());
            else
                throw new RuntimeException("Cannot identify port for parsing remainder: " + url);
        }
        else
            remainder = withoutHost;
        
        return remainder;
    }
    
    public String GetRemainder()
    {
        return this.remainder;
    }
    
    @Override
    public String toString()
    {
        return (scheme == Scheme.UNKNOWN? "" : scheme.Value() + SCHEME_SEP) +
                host +
                (port == UNKNOWN_PORT? "" : PORT_SEP + port) +
                remainder;
    }
    
    private String RemoveSchemeIfExists(String url, Scheme scheme)
    {
        String withoutScheme;
        if(scheme == Scheme.UNKNOWN)
            withoutScheme = url;
        else
            withoutScheme = url.substring(url.indexOf(SCHEME_SEP) + SCHEME_SEP.length());
        
        return withoutScheme;
    }
    
    private Scheme RecheckScheme(int port)
    {
        if(scheme == Scheme.UNKNOWN && port != UNKNOWN_PORT)
            return Scheme.GetSchemeForPort(port);
        
        return scheme;
    }
}
