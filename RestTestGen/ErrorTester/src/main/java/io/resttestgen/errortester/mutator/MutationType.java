package io.resttestgen.errortester.mutator;

/**
 * Used to define the mutation type
 */
public enum MutationType {
    REQUIRED_MISSING,    // Used to set a required parameter or field as not required
    WRONG_DATATYPE,      // Used to specify a wrong datatype (i.e., an integer is expected but a string is provided)
    VIOLATED_CONSTRAINT, // Used to violate a constraint (i.e., string length, minimum/maximum value, etc.);
    ALL
}
