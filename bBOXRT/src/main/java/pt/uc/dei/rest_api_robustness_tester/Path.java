package pt.uc.dei.rest_api_robustness_tester;

import java.net.URI;
import java.util.*;

public class Path
{
    public enum HttpMethod
    {
        GET, PUT, POST, DELETE;
        
        public static HttpMethod GetMethodForValue(String value)
        {
            for(HttpMethod m : values())
                if(m.name().equalsIgnoreCase(value))
                    return m;
    
            return null;
        }
    }
    
    public String uri = null;
    
    public Map<HttpMethod, Operation> operations = new LinkedHashMap<>();
    
    public List<Server> servers = new ArrayList<>();
    
    public Operation GetOperation(HttpMethod httpMethod)
    {
        return operations.get(httpMethod);
    }
    
    public float UriMatchScore(String uri)
    {
        String[] thisUriParts = SplitUri(this.uri);
        String[] uriParts = SplitUri(uri);
    
        float score = 0f;
        
        if(thisUriParts.length == uriParts.length)
            for(int i = 0; i < uriParts.length; i++)
                score += UriPartMatchScore(thisUriParts[i], uriParts[i]);
        
        return score;
    }
    
    public List<String> GetTemplateVariables()
    {
        String[] uriParts = SplitUri(uri);
        List<String> templateVariables = new ArrayList<>();
    
        for (String uriPart : uriParts)
            if (UriPartIsTemplate(uriPart))
                templateVariables.add(uriPart);
        
        return templateVariables;
    }
    
    private static String[] SplitUri(String uri)
    {
        return uri.split("/");
    }

    private static boolean UriPartIsTemplate(String uriPart)
    {
        return uriPart.contains("{") && uriPart.contains("}");
    }

    private static float UriPartMatchScore(String thisUriPart, String uriPart)
    {
        if(UriPartIsTemplate(thisUriPart))
        {
            String prefix = thisUriPart.substring(0, thisUriPart.indexOf("{")).replace("{", "");
            String suffix = thisUriPart.substring(thisUriPart.indexOf("}")).replace("}", "");

            boolean hasPrefix = !prefix.isEmpty();
            boolean hasSuffix = !suffix.isEmpty();

            if(hasPrefix && !hasSuffix)
                return uriPart.startsWith(prefix) ? 0.5f : 0f;
            else if(!hasPrefix && hasSuffix)
                return uriPart.endsWith(suffix) ? 0.5f : 0f;
            else if(hasPrefix && hasSuffix)
                return uriPart.startsWith(prefix) && uriPart.endsWith(suffix) ? 0.5f : 0f;
            else
                return 0.5f;
        }

        return thisUriPart.equals(uriPart) ? 1f : 0f;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(uri, path.uri) &&
                Objects.equals(operations, path.operations) &&
                Objects.equals(servers, path.servers);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(uri, operations, servers);
    }
}
