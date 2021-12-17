package Models;

import Structure.BST23;
import Structure.IData;

import java.io.*;

public class Region implements IData<Region> {
    private static final int MAX_LENGTH_OF_STRING = 30;
    private static final String EMPTY = "";
    private static final int UNDEFINED = -1;

    //kraj
    private int regionId;
    private String name;
    private BST23<PCRKeyRegion, Address> treeOfTests;
    private boolean isValid;

    public Region(int regionId, String name) {
        this.regionId = regionId;
        this.name = name;
        treeOfTests = new BST23<PCRKeyRegion, Address>(
                "regionFiles/tests"+regionId,
                PCRKeyRegion.class,
                Address.class);
        isValid = true;
    }

    public Region(){
        regionId = UNDEFINED;
        name = EMPTY;
        isValid = false;
    }

    public boolean insertTest(PCRRegionData insertedTest){
        return treeOfTests.insert(insertedTest);
    }

    public boolean deletePCRTest(PCRRegionData deletedTest){
        return treeOfTests.delete(deletedTest);
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BST23<PCRKeyRegion, Address> getTreeOfTests() {
        return treeOfTests;
    }

    public void setTreeOfTests(BST23<PCRKeyRegion, Address> treeOfTests) {
        this.treeOfTests = treeOfTests;
    }

    @Override
    public Region createClass() {
        return new Region();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            int stringLength = name.length();
            //ukladanie poctu platnych znakov pre string
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(name);
            String unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_STRING){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);
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
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_STRING; i++){
                name += hlpInStream.readChar();
            }
            name = name.substring(0,validCharsInString);
            regionId = hlpInStream.readInt();
            isValid = hlpInStream.readBoolean();
            if (regionId != UNDEFINED){
                treeOfTests = new BST23<PCRKeyRegion, Address>(
                        "regionFiles/tests"+regionId,
                        PCRKeyRegion.class,
                        Address.class);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return ((MAX_LENGTH_OF_STRING*Character.BYTES) + 2*(Integer.BYTES) + 1);
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
