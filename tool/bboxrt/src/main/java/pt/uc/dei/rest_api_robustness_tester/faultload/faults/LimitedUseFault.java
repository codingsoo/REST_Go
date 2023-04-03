package pt.uc.dei.rest_api_robustness_tester.faultload.faults;

import pt.uc.dei.rest_api_robustness_tester.utils.LimitedUseObject;

public class LimitedUseFault extends LimitedUseObject<Fault>
{
    public LimitedUseFault(Fault object, int maxUses)
    {
        super(object, maxUses);
    }
}
