package Models;

import Structure.BST23;
import Structure.IData;

import java.io.*;

public class District implements IData<District> {
    private static final int MAX_LENGTH_OF_STRING = 30;
    private static final String EMPTY = "";
    private static final int UNDEFINED = -1;

    private int districtId;
    private String name;
    private BST23<PCRKeyDistrict, Address> treeOfTests;
    private boolean isValid;

    public District(int districtId, String name) {
        this.districtId = districtId;
        this.name = name;
        treeOfTests = new BST23<PCRKeyDistrict, Address>(
                "districtFiles/tests"+districtId,
                PCRKeyDistrict.class,
                Address.class);
        isValid = true;
    }

    public District(){
        districtId = UNDEFINED;
        name = EMPTY;
        isValid = false;
    }

    public boolean insertTest(PCRDistrictPositiveData insertedTest){
        return treeOfTests.insert(insertedTest);
    }

    public boolean deletePCRTest(PCRDistrictPositiveData deletedTest){
        return treeOfTests.delete(deletedTest);
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BST23<PCRKeyDistrict, Address> getTreeOfTestedPeople() {
        return treeOfTests;
    }

    public void setTreeOfTestedPeople(BST23<PCRKeyDistrict, Address> treeOfTests) {
        this.treeOfTests = treeOfTests;
    }

    @Override
    public District createClass() {
        return new District();
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
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_STRING; i++){
                name += hlpInStream.readChar();
            }
            name = name.substring(0,validCharsInString);
            districtId = hlpInStream.readInt();
            isValid = hlpInStream.readBoolean();
            treeOfTests = new BST23<PCRKeyDistrict, Address>(
                    "districtFiles/tests"+districtId,
                    PCRKeyDistrict.class,
                    Address.class);
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
