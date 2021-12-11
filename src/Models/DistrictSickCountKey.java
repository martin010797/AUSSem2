package Models;

import Structure.IData;

import java.io.*;

public class DistrictSickCountKey implements Comparable<DistrictSickCountKey>, IData<DistrictSickCountKey> {
    private static final int UNDEFINED = -1;

    private int numberOfSickPeople;
    private int districtId;
    private boolean isValid;

    public DistrictSickCountKey(int numberOfSickPeople, int districtId) {
        this.numberOfSickPeople = numberOfSickPeople;
        this.districtId = districtId;
        isValid = true;
    }

    public DistrictSickCountKey(){
        numberOfSickPeople = UNDEFINED;
        districtId = UNDEFINED;
        isValid = false;
    }

    public int getNumberOfSickPeople() {
        return numberOfSickPeople;
    }

    public int getDistrictId() {
        return districtId;
    }

    @Override
    public int compareTo(DistrictSickCountKey o) {
        //vyssi pocet chorych znamena mensi kluc(aby nalavo daval najvyssie pocty chorych)
        if (numberOfSickPeople < o.numberOfSickPeople){
            return -1;
        }else if (numberOfSickPeople > o.numberOfSickPeople ){
            return 1;
        }else {
            if (districtId < o.districtId){
                return 1;
            }else if (districtId > o.districtId ){
                return -1;
            }else {
                return 0;
            }
        }
    }

    @Override
    public DistrictSickCountKey createClass() {
        return new DistrictSickCountKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(numberOfSickPeople);
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
            numberOfSickPeople = hlpInStream.readInt();
            districtId = hlpInStream.readInt();
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
