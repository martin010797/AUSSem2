package Main_system;

public class ResultWIthNumberOfResults {
    private ResponseType responseType;
    private String resultInfo;
    private int numberOfResults;
    public ResultWIthNumberOfResults(ResponseType pResponse, String pResult, int pNumberOfResults){
        responseType = pResponse;
        resultInfo = pResult;
        numberOfResults = pNumberOfResults;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public String getResultInfo() {
        return resultInfo;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }
}
