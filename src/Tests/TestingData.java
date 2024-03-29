package Tests;

import Structure.IData;

import java.io.*;

public class TestingData implements IData<TestingData> {
    private static final int MAX_LENGTH_OF_STRING = 15;
    private static final int UNDEFINED = -1;
    private static final String EMPTY = "";

    private int id;
    private int testInteger;
    private String testString;
    private boolean isValid;

    public TestingData(){
        id = -1;
        testInteger = UNDEFINED;
        testString = EMPTY;
        isValid = false;
    }

    public TestingData(int id, int testInteger, String testString) {
        //konstruktor je pre vvytvaranie dat ktore sa budu vkladat
        this.id = id;
        this.testInteger = testInteger;
        this.testString = testString;
        isValid = true;
    }

    @Override
    public TestingData createClass() {
        return new TestingData();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(id);
            hlpOutStream.writeInt(testInteger);
            int stringLength = testString.length();
            //ukladanie poctu platnych znakov pre string
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(testString);
            String unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_STRING){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);
            hlpOutStream.writeBoolean(isValid);
            return hlpByteArrayOutputStream.toByteArray();
        }catch (IOException e){
            throw new IllegalStateException("Error during conversion to byte array.");
        }
    }

    @Override
    public void FromByteArray(byte[] pArray) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(pArray);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);

        try {
            id = hlpInStream.readInt();
            testInteger = hlpInStream.readInt();
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_STRING; i++){
                testString += hlpInStream.readChar();
            }
            testString = testString.substring(0,validCharsInString);
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        //Integer: id, testovaci integer, velkost validnych charakterov v stringu
        //String(characters): jedna string testovacia hodnota
        //Boolean: ci je dana hodnota platna alebo vymazana(len 1 byte)
        return ((3*Integer.BYTES)+(MAX_LENGTH_OF_STRING*Character.BYTES)+1);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        isValid = pValid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTestInteger() {
        return testInteger;
    }

    public void setTestInteger(int testInteger) {
        this.testInteger = testInteger;
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }
}
