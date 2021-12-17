package Models;

import Structure.BST23;
import Structure.IData;
import Tests.TestKey;
import Tests.TestingData2;

import java.io.*;

public class Workplace implements IData<Workplace> {
    private static final int UNDEFINED = -1;

    private int workplaceId;
    private boolean isValid;
    private BST23<PCRKeyDate, Address> treeOfTests;

    public Workplace(int workplaceId) {
        this.workplaceId = workplaceId;
        isValid = true;
        treeOfTests = new BST23<PCRKeyDate, Address>(
                "workplaceFiles/tests"+workplaceId,
                PCRKeyDate.class,
                Address.class);
    }

    public Workplace(){
        workplaceId = UNDEFINED;
        isValid = false;
    }

    public boolean insertTest(PCRWorkplaceData insertedTest){
        return treeOfTests.insert(insertedTest);
    }

    public boolean deletePCRTest(PCRWorkplaceData deletedTest){
        return treeOfTests.delete(deletedTest);
    }

    public int getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    public BST23<PCRKeyDate, Address> getTreeOfTests() {
        return treeOfTests;
    }

    public void setTreeOfTests(BST23<PCRKeyDate, Address> treeOfTests) {
        this.treeOfTests = treeOfTests;
    }

    @Override
    public Workplace createClass() {
        return new Workplace();
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
            if (workplaceId != UNDEFINED){
                treeOfTests = new BST23<PCRKeyDate, Address>(
                        "workplaceFiles/tests"+workplaceId,
                        PCRKeyDate.class,
                        Address.class);
            }
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
