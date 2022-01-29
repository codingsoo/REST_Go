package io.resttestgen.nominaltester.models;

import java.util.ArrayList;
import java.util.List;

/**
 * TestCase is a wrapping class for a list of test steps
 */
public class TestCase {

    private List<TestStep> testSteps;
    private Authentication authentication;


    public TestCase() {
        this.testSteps = new ArrayList<>();
    }

    public TestCase(List<TestStep> executions) {
        this.testSteps = executions;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    /**
     * Set a list of new test steps
     * @param testSteps
     */
    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = testSteps;
    }

    /**
     * Add a new step to the test case
     * @param testStep
     */
    public void addTestStep(TestStep testStep) {
        this.testSteps.add(testStep);
    }

    /**
     * Get the main test step (last step of the test case)
     * @return TestStep
     */
    public TestStep getMainTestStep() {
        return testSteps.get(testSteps.size() - 1);
    }

    @Override
    public String toString() {
        return "Test case with " + testSteps.size() + " test steps";
    }


    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Returns the dependencies to successfully execute the main test step (i.e., all test steps having status code 200)
     * @return a list of test steps representing the dependencies of the main test step
     */
    public List<TestStep> getMainTestStepDependencies() {
        List<TestStep> dependencies = new ArrayList<>();

        for (TestStep testStep : this.testSteps) {
            if (testStep.getExecutionResult().getStatusCode() == 200 && !testStep.equals(this.getMainTestStep())) {
                dependencies.add(testStep);
            }
        }

        return dependencies;
    }
}
