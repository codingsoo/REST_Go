package pt.uc.dei.rest_api_robustness_tester.utils;

public class LimitedUseObject<T>
{
    private final T object;
    private final int maxUses;
    private int usesLeft;
    
    public LimitedUseObject(T object, int maxUses)
    {
        this.object = object;
        this.maxUses = maxUses > 0 ? maxUses : 1;
        this.usesLeft = this.maxUses;
    }
    
    public int MaxUses()
    {
        return maxUses;
    }
    
    public int UsesLeft()
    {
        return usesLeft;
    }
    
    public void Reset()
    {
        usesLeft = maxUses;
    }
    
    public T Use()
    {
        if(usesLeft > 0)
        {
            usesLeft--;
            return object;
        }
        
        return null;
    }
}
