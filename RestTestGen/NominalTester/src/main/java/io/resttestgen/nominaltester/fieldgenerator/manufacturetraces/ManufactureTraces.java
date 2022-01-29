package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a List of Manufacture Trace, necessary to build a http's call parameter
 * during its creation.
 */
public class ManufactureTraces {
    List<ManufactureTrace> manufactureTraces;
    public ManufactureTraces() {
        this.manufactureTraces = new ArrayList<>();
    }

    public void addTrace(ManufactureTrace manufactureTrace) {
        this.manufactureTraces.add(manufactureTrace);
    }

    public List<ManufactureTrace> getManufactureTraces() {
        return manufactureTraces;
    }
}
