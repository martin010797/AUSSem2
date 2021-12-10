package Tests;

import Structure.IData;

import java.io.*;

public class TestingData2 implements IData<TestingData2> {

    private static final int MAX_LENGTH_OF_STRING = 15;
    private static final int UNDEFINED = -1;
    private static final String EMPTY = "";

    private String testString;
    private int testInteger;
    private double testDouble;
    private boolean isValid;

    public TestingData2(){
        testInteger = UNDEFINED;
        testDouble = UNDEFINED;
        testString = EMPTY;
        isValid = false;
    }

    public TestingData2(String testString, int testInteger, double testDouble) {
        this.testString = testString;
        this.testInteger = testInteger;
        this.testDouble = testDouble;
        isValid = true;
    }

    @Override
    public TestingData2 createClass() {
        return new TestingData2();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
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
            hlpOutStream.writeInt(testInteger);
            hlpOutStream.writeDouble(testDouble);
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
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_STRING; i++){
                testString += hlpInStream.readChar();
            }
            testString = testString.substring(0,validCharsInString);
            testInteger = hlpInStream.readInt();
            testDouble = hlpInStream.readDouble();
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return ((Double.BYTES)+(2*Integer.BYTES)+(MAX_LENGTH_OF_STRING*Character.BYTES)+1);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        isValid = pValid;
    }

    public String getTestString() {
        return testString;
    }

    public int getTestInteger() {
        return testInteger;
    }

    public double getTestDouble() {
        return testDouble;
    }
}
