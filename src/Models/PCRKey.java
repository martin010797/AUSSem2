package Models;

import Main_system.PersonPCRResult;
import Main_system.ResponseType;
import Structure.IData;

import java.io.*;
import java.util.UUID;

public class PCRKey implements Comparable<PCRKey>, IData<PCRKey> {
    private static final int MAX_LENGTH_OF_STRING = 40;
    private static final String EMPTY = "";

    private UUID PCRId;
    private boolean isValid;

    public PCRKey(UUID PCRId) {
        this.PCRId = PCRId;
        isValid = true;
    }

    public PCRKey(){
        PCRId = null;
        isValid = false;
    }

    public UUID getPCRId() {
        return PCRId;
    }

    public void setPCRId(UUID PCRId) {
        this.PCRId = PCRId;
    }

    @Override
    public int compareTo(PCRKey o) {
        //komparator opacne lebo aj v implementovani stromu som spravil omylom opacne
        if (PCRId.compareTo(o.PCRId) < 0){
            return 1;
        }else if (PCRId.compareTo(o.PCRId) > 0){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public PCRKey createClass() {
        return new PCRKey();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            String pcrIdString = EMPTY;
            if (PCRId != null){
                pcrIdString = PCRId.toString();
            }
            int stringLength = pcrIdString.length();
            //ukladanie poctu platnych znakov pre string
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(pcrIdString);
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

        String pcrIdString = EMPTY;
        try {
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_STRING; i++){
                pcrIdString += hlpInStream.readChar();
            }
            pcrIdString = pcrIdString.substring(0,validCharsInString);
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }

        try{
            PCRId = UUID.fromString(pcrIdString);
        }catch (Exception exception){
            PCRId = null;
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
