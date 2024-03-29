package Models;

import Structure.IData;

import java.io.*;

public class WorkplaceKey implements Comparable<WorkplaceKey>, IData<WorkplaceKey> {
    private static final int UNDEFINED = -1;

    private int workplaceId;
    private boolean isValid;

    public WorkplaceKey(int workplaceId) {
        this.workplaceId = workplaceId;
        isValid = true;
    }

    public WorkplaceKey(){
        workplaceId = UNDEFINED;
        isValid = false;
    }

    public int getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    @Override
    public int compareTo(WorkplaceKey o) {
        if (workplaceId < o.workplaceId){
            return 1;
        }else if (workplaceId > o.workplaceId ){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public WorkplaceKey createClass() {
        return new WorkplaceKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeInt(workplaceId);
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
            workplaceId = hlpInStream.readInt();
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
