package Models;

import Structure.IData;

import java.io.*;

public class RegionSickCountKey implements  Comparable<RegionSickCountKey>, IData<RegionSickCountKey> {
    private static final int UNDEFINED = -1;

    private int numberOfSickPeople;
    private int regionId;
    private boolean isValid;

    public RegionSickCountKey(int numberOfSickPeople, int regionId) {
        this.numberOfSickPeople = numberOfSickPeople;
        this.regionId = regionId;
        isValid = true;
    }

    public RegionSickCountKey(){
        numberOfSickPeople = UNDEFINED;
        regionId = UNDEFINED;
        isValid = false;
    }

    public int getNumberOfSickPeople() {
        return numberOfSickPeople;
    }

    public int getRegionId() {
        return regionId;
    }

    @Override
    public int compareTo(RegionSickCountKey o) {
        //vyssi pocet chorych znamena mensi kluc(aby nalavo daval najvyssie pocty chorych)
        if (numberOfSickPeople < o.numberOfSickPeople){
            return -1;
        }else if (numberOfSickPeople > o.numberOfSickPeople ){
            return 1;
        }else {
            if (regionId < o.regionId){
                return 1;
            }else if (regionId > o.regionId ){
                return -1;
            }else {
                return 0;
            }
        }
    }

    @Override
    public RegionSickCountKey createClass() {
        return new RegionSickCountKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(numberOfSickPeople);
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
            numberOfSickPeople = hlpInStream.readInt();
            regionId = hlpInStream.readInt();
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
}
