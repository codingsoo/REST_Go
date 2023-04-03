package pt.uc.dei.rest_api_robustness_tester.faultload;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.any.ReplaceWithEmptyValue;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.any.ReplaceWithNull;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.array.*;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.bool.*;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.date.*;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.datetime.*;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.numerical.*;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.string.*;

public class FaultloadGenerator
{
    //TODO: Add support for external classes containing implementations of the Fault interface
    public Faultload Generate()
    {
        Faultload faultload = new Faultload();
        
        //Any
        faultload.RegisterFault(new ReplaceWithEmptyValue());
        faultload.RegisterFault(new ReplaceWithNull());

        //FIXME: temporary
        //Numerical
        faultload.RegisterFault(new Add1Unit());
        faultload.RegisterFault(new Subtract1Unit());
        faultload.RegisterFault(new ReplaceWith0());
        faultload.RegisterFault(new ReplaceWith1());
        faultload.RegisterFault(new ReplaceWithMinus1());
        faultload.RegisterFault(new ReplaceWithDataTypeMaximum());
        faultload.RegisterFault(new ReplaceWithDataTypeMinimum());
        faultload.RegisterFault(new ReplaceWithDataTypeMaximumPlus1());
        faultload.RegisterFault(new ReplaceWithDataTypeMinimumMinus1());
        faultload.RegisterFault(new ReplaceWithDomainMaximum());
        faultload.RegisterFault(new ReplaceWithDomainMinimum());
        faultload.RegisterFault(new ReplaceWithDomainMaximumPlus1());
        faultload.RegisterFault(new ReplaceWithDomainMinimumMinus1());

        //Boolean
        faultload.RegisterFault(new NegateBooleanValue());
        faultload.RegisterFault(new OverflowBooleanValue());

        //String
        faultload.RegisterFault(new AddRandomCharactersToOverflowMaximumLength());
        faultload.RegisterFault(new AddRandomNonPrintableCharactersToEnd());
        faultload.RegisterFault(new InsertRandomNonPrintableCharactersAtRandomPositions());
        faultload.RegisterFault(new ReplaceWithRandomAlphanumericString());
        faultload.RegisterFault(new ReplaceWithRandomNonPrintableCharacterString());
        faultload.RegisterFault(new ReplaceWithRandomPrintableCharacterString());
        faultload.RegisterFault(new SwapRandomPairsInString());
        faultload.RegisterFault(new DuplicateRandomElementsToOverflowMaximumLength());
        //faultload.RegisterFault(new AddRandomCharactersToVeryLargeStringLength());
        faultload.RegisterFault(new ReplaceWithMaliciousString1());
        faultload.RegisterFault(new ReplaceWithMaliciousString2());
        faultload.RegisterFault(new ReplaceWithMaliciousString3());
        faultload.RegisterFault(new ReplaceWithMaliciousString4());
        faultload.RegisterFault(new ReplaceWithMaliciousString5());
        faultload.RegisterFault(new ReplaceWithMaliciousString6());
        faultload.RegisterFault(new ReplaceWithMaliciousString7());
        faultload.RegisterFault(new ReplaceWithMaliciousString8());
        faultload.RegisterFault(new ReplaceWithMaliciousString9());
        faultload.RegisterFault(new ReplaceWithMaliciousString10());
        faultload.RegisterFault(new ReplaceWithMaliciousString11());

        //Array
        faultload.RegisterFault(new RemoveRandomElementFromArray());
        faultload.RegisterFault(new DuplicateRandomElements());
        faultload.RegisterFault(new RemoveAllElements());
        faultload.RegisterFault(new RemoveAllElementsExceptFirst());

        //Date
        faultload.RegisterFault(new Add100Years());
        faultload.RegisterFault(new Subtract100Years());
        faultload.RegisterFault(new ReplaceWithInvalidDate1());
        faultload.RegisterFault(new ReplaceWithInvalidDate2());
        faultload.RegisterFault(new ReplaceWithInvalidDate3());
        faultload.RegisterFault(new ReplaceWithInvalidDate4());
        faultload.RegisterFault(new ReplaceWithInvalidDate5());
        faultload.RegisterFault(new ReplaceWithInvalidDate6());
        faultload.RegisterFault(new ReplaceWithLastDayOfPreviousMillennium());
        faultload.RegisterFault(new ReplaceWithFirstDayOfCurrentMillennium());

        //Date time
        faultload.RegisterFault(new Add24Hours());
        faultload.RegisterFault(new Subtract24Hours());
        faultload.RegisterFault(new ReplaceWithInvalidTime1());
        faultload.RegisterFault(new ReplaceWithInvalidTime2());
        faultload.RegisterFault(new ReplaceWithInvalidTime3());
        faultload.RegisterFault(new ReplaceWithInvalidTime4());
        faultload.RegisterFault(new ReplaceWithInvalidTime5());
        faultload.RegisterFault(new ReplaceWithInvalidTime6());

        return faultload;
    }
}
