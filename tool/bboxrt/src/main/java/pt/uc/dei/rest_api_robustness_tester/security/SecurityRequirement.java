package pt.uc.dei.rest_api_robustness_tester.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecurityRequirement
{
    public Map<String, List<String>> requirement = new HashMap<>();
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityRequirement that = (SecurityRequirement) o;
        return requirement.equals(that.requirement);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(requirement);
    }
}
