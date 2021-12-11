package Models;

import Main_system.ResponseType;
import Structure.BST23Node;
import Structure.IData;

import java.io.*;
import java.util.Date;
import java.util.UUID;

public class PCR implements IData<PCR> {
    private static final int MAX_LENGTH_OF_PCR_ID = 40;
    private static final int MAX_LENGTH_OF_ID_NUMBER = 10;
    private static final int MAX_LENGTH_OF_DESCRIPTION = 20;
    private static final String EMPTY = "";
    private static final int UNDEFINED = -1;

    private Date dateAndTimeOfTest;
    private String patientId;
    private UUID PCRId;
    private int workplaceId;
    private int districtId;
    private int regionId;
    private boolean result;
    private String description;

    //referencie budu nahradene adresami
    /*private Person testedPerson;
    private Workplace workplace;
    private Region region;
    private District district;*/

    private int testedPerson;
    private int workplace;
    private int region;
    private int district;

    private boolean isValid;

    public PCR(int year,
               int month,
               int day,
               int hour,
               int minute,
               int second,
               String pPatienId,
               int pWorkplaceId,
               int pDistrictId,
               int pRegionId,
               boolean pResult,
               String pDescription,
               int pPerson,
               String pTestID){
        dateAndTimeOfTest = new Date(year,month-1,day,hour,minute,second);
        patientId = pPatienId;
        if(pTestID == null){
            PCRId = UUID.randomUUID();
        }else {
            try{
                PCRId = UUID.fromString(pTestID);
            }catch (Exception exception){
                PCRId = UUID.randomUUID();
            }
        }
        workplaceId = pWorkplaceId;
        districtId = pDistrictId;
        regionId = pRegionId;
        result = pResult;
        description = pDescription;
        testedPerson = pPerson;
        isValid = true;
    }

    public PCR(){
        dateAndTimeOfTest = null;
        patientId = EMPTY;
        PCRId = null;
        workplaceId = UNDEFINED;
        districtId = UNDEFINED;
        regionId = UNDEFINED;
        result = false;
        description = EMPTY;
        isValid = false;
        testedPerson = UNDEFINED;
        workplace = UNDEFINED;
        region = UNDEFINED;
        district = UNDEFINED;
    }

    @Override
    public PCR createClass() {
        return new PCR();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            //ukladanie id pre pcr
            String pcrIdString = EMPTY;
            if (PCRId != null){
                pcrIdString = PCRId.toString();
            }
            int stringLength = pcrIdString.length();
            //ukladanie poctu platnych znakov pre id pcr
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(pcrIdString);
            String unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_PCR_ID){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            //ukladanie rodneho cisla
            stringLength = patientId.length();
            //ukladanie poctu platnych znakov pre rodne cislo
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(patientId);
            unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_ID_NUMBER){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            //ukladanie popisu
            stringLength = description.length();
            //ukladanie poctu platnych znakov pre popis
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(description);
            unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_DESCRIPTION){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            //ukladanie datumu testu
            if (dateAndTimeOfTest != null) {
                hlpOutStream.writeInt(dateAndTimeOfTest.getDate());
                hlpOutStream.writeInt(dateAndTimeOfTest.getMonth());
                hlpOutStream.writeInt(dateAndTimeOfTest.getYear());
                hlpOutStream.writeInt(dateAndTimeOfTest.getHours());
                hlpOutStream.writeInt(dateAndTimeOfTest.getMinutes());
                hlpOutStream.writeInt(dateAndTimeOfTest.getSeconds());
            }else {
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
                hlpOutStream.writeInt(UNDEFINED);
            }

            //ukladanie id
            hlpOutStream.writeInt(workplaceId);
            hlpOutStream.writeInt(districtId);
            hlpOutStream.writeInt(regionId);

            //ukladanie adries
            hlpOutStream.writeInt(testedPerson);
            hlpOutStream.writeInt(workplace);
            hlpOutStream.writeInt(district);
            hlpOutStream.writeInt(region);

            hlpOutStream.writeBoolean(result);
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
            //nacitavanie pcr id
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_PCR_ID; i++){
                pcrIdString += hlpInStream.readChar();
            }
            pcrIdString = pcrIdString.substring(0,validCharsInString);

            //nacitavanie rodneho cisla
            validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_ID_NUMBER; i++){
                patientId += hlpInStream.readChar();
            }
            patientId = patientId.substring(0,validCharsInString);

            //nacitavanie popisu
            validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_DESCRIPTION; i++){
                description += hlpInStream.readChar();
            }
            description = description.substring(0,validCharsInString);

            //nacitanie datumu testu
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
                dateAndTimeOfTest = null;
            }else {
                dateAndTimeOfTest = new Date(year,month, day, hour, minute, seconds);
            }

            //nacitanie id
            workplaceId = hlpInStream.readInt();
            districtId = hlpInStream.readInt();
            regionId = hlpInStream.readInt();

            //ukladanie adries
            testedPerson = hlpInStream.readInt();
            workplace = hlpInStream.readInt();
            district = hlpInStream.readInt();
            region = hlpInStream.readInt();

            result = hlpInStream.readBoolean();
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
        //3* string(+3*integer)
        //datum(6*integer)
        //result 1
        //id pre workplace,region a district(3* integer)
        //validita 1
        //adresy pre person,workplace,region a district(4*integer)
        return ((MAX_LENGTH_OF_ID_NUMBER*Character.BYTES)+
                (MAX_LENGTH_OF_DESCRIPTION*Character.BYTES)+
                (MAX_LENGTH_OF_PCR_ID*Character.BYTES)+
                (16*Integer.BYTES)+2);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean pValid) {
        isValid = pValid;
    }

    public Date getDateAndTimeOfTest() {
        return dateAndTimeOfTest;
    }

    public void setDateAndTimeOfTest(Date dateAndTimeOfTest) {
        this.dateAndTimeOfTest = dateAndTimeOfTest;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public UUID getPCRId() {
        return PCRId;
    }

    public void setPCRId(UUID PCRId) {
        this.PCRId = PCRId;
    }

    public int getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPerson() {
        return testedPerson;
    }

    public void setPerson(int person) {
        this.testedPerson = person;
    }

    public int getWorkplace() {
        return workplace;
    }

    public void setWorkplace(int workplace) {
        this.workplace = workplace;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }
}
