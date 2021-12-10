package Tests;

import Structure.IData;

import java.io.*;

public class TestingObjectValue implements IData<TestingObjectValue> {
    private static final int MAX_LENGTH_OF_STRING = 15;
    private static final int UNDEFINED = -1;
    private static final String EMPTY = "";

    private int value1;
    private int value2;
    private boolean isValid;

    public TestingObjectValue(int pValue1, int pValue2){
        value1 = pValue1;
        value2 = pValue2;
        isValid = true;
    }

    public TestingObjectValue(){
        value1 = UNDEFINED;
        value2 = UNDEFINED;
        isValid = false;
    }

    @Override
    public TestingObjectValue createClass() {
        return new TestingObjectValue();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(value1);
            hlpOutStream.writeInt(value2);
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
            value1 = hlpInStream.readInt();
            value2 = hlpInStream.readInt();
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return ((2*Integer.BYTES)+1);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        isValid = pValid;
    }

    public int getValue1() {
        return value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }
}
