package Models;

import Structure.IData;

import java.io.*;

public class RegionKey implements Comparable<RegionKey>, IData<RegionKey> {
    private static final int UNDEFINED = -1;

    private int regionId;
    private boolean isValid;

    public RegionKey(int regionId) {
        this.regionId = regionId;
        isValid = true;
    }
    public RegionKey(){
        regionId = UNDEFINED;
        isValid = false;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    @Override
    public int compareTo(RegionKey o) {
        if (regionId < o.regionId){
            return 1;
        }else if (regionId > o.regionId ){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public RegionKey createClass() {
        return new RegionKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(regionId);
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
            regionId = hlpInStream.readInt();
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
