package Models;

import Structure.IData;

import java.io.*;
import java.util.Date;

public class DistrictKey implements Comparable<DistrictKey>, IData<DistrictKey> {
    private static final int UNDEFINED = -1;

    private int districtId;
    private boolean isValid;

    public DistrictKey(int districtId) {
        this.districtId = districtId;
        isValid = true;
    }

    public DistrictKey(){
        districtId = UNDEFINED;
        isValid = false;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    @Override
    public int compareTo(DistrictKey o) {
        if (districtId < o.districtId){
            return 1;
        }else if (districtId > o.districtId ){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public DistrictKey createClass() {
        return new DistrictKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(districtId);
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
            districtId = hlpInStream.readInt();
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
