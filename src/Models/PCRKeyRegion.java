package Models;

import Structure.IData;

import java.io.*;
import java.util.Date;

public class PCRKeyRegion  implements Comparable<PCRKeyRegion>, IData<PCRKeyRegion> {
    private static final int UNDEFINED = -1;

    private boolean positivity;
    private Date date;
    private boolean isValid;

    public PCRKeyRegion(boolean positivity, Date date) {
        this.positivity = positivity;
        this.date = date;
        isValid = true;
    }

    public PCRKeyRegion(){
        positivity = false;
        date = null;
        isValid = false;
    }

    public boolean isPositivity() {
        return positivity;
    }

    public void setPositivity(boolean positivity) {
        this.positivity = positivity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(PCRKeyRegion o) {
        //pozitivne testy budu nalavo a negativne napravo stromu
        if (positivity && !o.positivity){
            return 1;
        }else if (!positivity && o.positivity){
            return -1;
        }else if (date.compareTo(o.date) < 0){
            return 1;
        }else if (date.compareTo(o.date) > 0){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public PCRKeyRegion createClass() {
        return new PCRKeyRegion();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            hlpOutStream.writeBoolean(positivity);
            //den, mesiac, rok, hodina, minuta, sekunda
            if (date != null) {
                hlpOutStream.writeInt(date.getDate());
                hlpOutStream.writeInt(date.getMonth());
                hlpOutStream.writeInt(date.getYear());
                hlpOutStream.writeInt(date.getHours());
                hlpOutStream.writeInt(date.getMinutes());
                hlpOutStream.writeInt(date.getSeconds());
            }else {
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
            }
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
            positivity = hlpInStream.readBoolean();
            int day = hlpInStream.readInt();
            int month = hlpInStream.readInt();
            int year = hlpInStream.readInt();
            int hour = hlpInStream.readInt();
            int minute = hlpInStream.readInt();
            int seconds = hlpInStream.readInt();
            if (day == UNDEFINED &&
                    month == UNDEFINED &&
                    year == UNDEFINED &&
                    hour == UNDEFINED &&
                    minute == UNDEFINED &&
                    seconds == UNDEFINED){
                date = null;
            }else {
                date = new Date(year,month, day, hour, minute, seconds);
            }
            isValid = hlpInStream.readBoolean();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        return ((6*Integer.BYTES)+2);
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
