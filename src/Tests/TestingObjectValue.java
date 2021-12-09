package Tests;

public class TestingObjectValue {
    private String name;
    private String testResult;

    public TestingObjectValue(String pName, String pTestResult){
        name = pName;
        testResult = pTestResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }
}
