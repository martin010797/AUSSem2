package Tests;

import Structure.IData;

import java.io.*;

public class TestKey implements Comparable<TestKey>, IData<TestKey> {
    private static final int MAX_LENGTH_OF_STRING = 15;
    private static final int UNDEFINED = -1;
    private static final String EMPTY = "";

    private int key;
    private boolean isValid;

    public TestKey(int pKey){
        key = pKey;
        isValid = true;
    }

    public TestKey(){
        key = UNDEFINED;
        isValid = false;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public int compareTo(TestKey o) {
        if (key < o.key){
            return 1;
        }else if (key > o.key){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public TestKey createClass() {
        return new TestKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(key);
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
            key = hlpInStream.readInt();
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return (Integer.BYTES+1);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        isValid = pValid;
    }
}
