package Main_system;

public class PersonPCRResult {
    private ResponseType responseType;
    private String resultInfo;
    public PersonPCRResult(ResponseType pResponse, String pResult){
        responseType = pResponse;
        resultInfo = pResult;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public String getResultInfo() {
        return resultInfo;
    }
}
