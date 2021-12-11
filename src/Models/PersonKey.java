package Models;

import Structure.IData;

import java.io.*;

public class PersonKey implements Comparable<PersonKey>, IData<PersonKey> {
    private static final int MAX_LENGTH_OF_STRING = 10;
    private static final String EMPTY = "";

    private String idNumber;
    private boolean isValid;

    public PersonKey(String idNumber) {
        this.idNumber = idNumber;
        isValid = true;
    }

    public PersonKey(){
        idNumber = EMPTY;
        isValid = false;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Override
    public int compareTo(PersonKey o) {
        //komparator opacne lebo aj v implementovani stromu som spravil omylom opacne
        if (idNumber.compareTo(o.idNumber) < 0){
            return 1;
        }else if (idNumber.compareTo(o.idNumber) > 0){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public PersonKey createClass() {
        return new PersonKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            int stringLength = idNumber.length();
            //ukladanie poctu platnych znakov pre string
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(idNumber);
            String unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_STRING){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);
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
                idNumber += hlpInStream.readChar();
            }
            idNumber = idNumber.substring(0,validCharsInString);
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return ((MAX_LENGTH_OF_STRING*Character.BYTES) + Integer.BYTES + 1);
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
