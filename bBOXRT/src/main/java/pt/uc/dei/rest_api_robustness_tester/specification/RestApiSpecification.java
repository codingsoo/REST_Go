package pt.uc.dei.rest_api_robustness_tester.specification;

import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityScheme;

import java.util.*;

public class RestApiSpecification
{
    public String specNameVersion = null;
    public String apiNameVersion = null;
    
    public List<Server> servers = new ArrayList<>();
    
    public List<Path> paths = new ArrayList<>();
    
    public List<SecurityRequirement> securityRequirements = new ArrayList<>();
    
    public Map<String, SecurityScheme> securitySchemes = new HashMap<>();
    
    public Path GetPath(String uri, Path.HttpMethod method)
    {
        TreeMap<Float, List<Path>> pathScores = new TreeMap<>();
        for(Path path : paths)
        {
            if(path.GetOperation(method) != null)
            {
                if(path.uri.equals(uri))
                    return path;
                
                float score = path.UriMatchScore(uri);
                if (!pathScores.containsKey(score))
                    pathScores.put(score, new ArrayList<>());
                pathScores.get(score).add(path);
            }
        }
        
        List<Path> bestMatchPaths = pathScores.lastEntry().getValue();
        
        if(bestMatchPaths.size() == 1)
            return bestMatchPaths.get(0);
        else
            System.err.println("Error: more than one URI in the API Spec matches the Request URI " + uri);
        
        return null;
    }
    
    public void Validate()
    {
        DiscardAmbiguousPaths();
    }
    
    private void DiscardAmbiguousPaths()
    {
        Map<String, List<Path>> pathsByUri = new HashMap<>();
        for(Path path : paths)
        {
            List<String> templateVariables = path.GetTemplateVariables();
            String cleanUri = path.uri;
            for(String s : templateVariables)
                cleanUri = cleanUri.replace(s, "<?>");
            if(!pathsByUri.containsKey(cleanUri))
                pathsByUri.put(cleanUri, new ArrayList<>());
            pathsByUri.get(cleanUri).add(path);
        }
        
        for(String uri : pathsByUri.keySet())
        {
            if(pathsByUri.get(uri).size() > 1)
            {
                System.out.println("The following paths must be removed because they are ambiguous:");
                for(Path path : pathsByUri.get(uri))
                {
                    paths.remove(path);
                    System.out.println("\t" + path.uri);
                }
                System.out.println("These paths all evaluate to the URI " + uri);
                System.out.println("(See \"Path templating\" in the OpenAPI specification)");
            }
        }
    }
}
