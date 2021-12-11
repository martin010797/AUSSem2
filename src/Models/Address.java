package Models;

import Structure.IData;

import java.io.*;

public class Address implements IData<Address> {
    private static final int UNDEFINED = -1;

    private int addressInUnsortedFile;
    private boolean isValid;

    public Address(int pAddress){
        addressInUnsortedFile = pAddress;
        isValid = true;
    }

    public Address(){
        addressInUnsortedFile = UNDEFINED;
        isValid = false;
    }

    @Override
    public Address createClass() {
        return new Address();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(addressInUnsortedFile);
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
            addressInUnsortedFile = hlpInStream.readInt();
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

    public int getAddressInUnsortedFile() {
        return addressInUnsortedFile;
    }

    public void setAddressInUnsortedFile(int addressInUnsortedFile) {
        this.addressInUnsortedFile = addressInUnsortedFile;
    }
}
