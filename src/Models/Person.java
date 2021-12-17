package Models;

import Structure.BST23;
import Structure.BST23Node;
import Structure.IData;

import java.io.*;
import java.util.Date;
import java.util.GregorianCalendar;

public class Person implements IData<Person> {
    private static final int MAX_LENGTH_OF_NAME = 15;
    private static final int MAX_LENGTH_OF_SURNAME = 20;
    private static final int MAX_LENGTH_OF_ID_NUMBER = 10;
    private static final String EMPTY = "";
    private static final int UNDEFINED = -1;

    private String name;
    private String surname;
    private Date dateOfBirth;
    private String idNumber;
    private BST23<PCRKey, Address> treeOfTests;
    private BST23<PCRKeyDate, Address> treeOfTestsByDate;
    private boolean isValid;

    public Person(String pName, String pSurname, int pYear, int pMonth, int pDay, String pIdNumber){
        name = pName;
        surname = pSurname;
        dateOfBirth = new Date(pYear,pMonth-1,pDay);
        idNumber = pIdNumber;
        treeOfTests = new BST23<PCRKey, Address>(
                "personFiles/tests"+idNumber,
                PCRKey.class,
                Address.class);
        treeOfTestsByDate = new BST23<PCRKeyDate, Address>(
                "personFiles/testsDate"+idNumber,
                PCRKeyDate.class,
                Address.class);
        isValid = true;
    }

    public Person(){
        name = EMPTY;
        surname = EMPTY;
        dateOfBirth = null;
        idNumber = EMPTY;
        isValid = false;
    }

    public boolean insertPCRForPerson(PCRData PCRTest){
        return treeOfTests.insert(PCRTest);
    }

    public boolean insertPCRByDateForPerson(PCRWorkplaceData PCRTest){
        return treeOfTestsByDate.insert(PCRTest);
    }

    public boolean deletePCRTest(PCRData deletedTest){
        return treeOfTests.delete(deletedTest);
    }

    public boolean deletePCRTestByDate(PCRWorkplaceData deletedTest){
        return treeOfTestsByDate.delete(deletedTest);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public BST23<PCRKey, Address> getTreeOfTests() {
        return treeOfTests;
    }

    public void setTreeOfTests(BST23<PCRKey, Address> treeOfTests) {
        this.treeOfTests = treeOfTests;
    }

    public BST23<PCRKeyDate, Address> getTreeOfTestsByDate() {
        return treeOfTestsByDate;
    }

    @Override
    public Person createClass() {
        return new Person();
    }

    @Override
    public byte[] ToByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try{
            int stringLength = name.length();
            //ukladanie poctu platnych znakov pre meno
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(name);
            String unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_NAME){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            stringLength = surname.length();
            //ukladanie poctu platnych znakov pre priezvisko
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(surname);
            unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_SURNAME){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            stringLength = idNumber.length();
            //ukladanie poctu platnych znakov pre rodne cislo
            hlpOutStream.writeInt(stringLength);
            hlpOutStream.writeChars(idNumber);
            unusedChars = "";
            while (stringLength < MAX_LENGTH_OF_ID_NUMBER){
                unusedChars += "-";
                stringLength++;
            }
            hlpOutStream.writeChars(unusedChars);

            if (dateOfBirth != null) {
                hlpOutStream.writeInt(dateOfBirth.getDate());
                hlpOutStream.writeInt(dateOfBirth.getMonth());
                hlpOutStream.writeInt(dateOfBirth.getYear());
                hlpOutStream.writeInt(dateOfBirth.getHours());
                hlpOutStream.writeInt(dateOfBirth.getMinutes());
                hlpOutStream.writeInt(dateOfBirth.getSeconds());
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
            //nacitanie mena
            int validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_NAME; i++){
                name += hlpInStream.readChar();
            }
            name = name.substring(0,validCharsInString);

            //nacitanie priezviska
            validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_SURNAME; i++){
                surname += hlpInStream.readChar();
            }
            surname = surname.substring(0,validCharsInString);

            //nacitanie rodne cislo
            validCharsInString = hlpInStream.readInt();
            for (int i = 0; i < MAX_LENGTH_OF_ID_NUMBER; i++){
                idNumber += hlpInStream.readChar();
            }
            idNumber = idNumber.substring(0,validCharsInString);

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
                dateOfBirth = null;
            }else {
                dateOfBirth = new Date(year,month, day, hour, minute, seconds);
            }
            isValid = hlpInStream.readBoolean();
            if (!idNumber.isEmpty()){
                treeOfTests = new BST23<PCRKey, Address>(
                        "personFiles/tests"+idNumber,
                        PCRKey.class,
                        Address.class);
                treeOfTestsByDate = new BST23<PCRKeyDate, Address>(
                        "personFiles/testsDate"+idNumber,
                        PCRKeyDate.class,
                        Address.class);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int getSize() {
        //6*integer pre datum narodenia
        //string pre meno,priezvisko,rodne cislo
        //pre kazdy string znacenie pouzitych znakov(3*integer)
        //1 validita
        return ((MAX_LENGTH_OF_NAME*Character.BYTES)+
                (MAX_LENGTH_OF_SURNAME*Character.BYTES)+
                (MAX_LENGTH_OF_ID_NUMBER*Character.BYTES)+
                (9*Integer.BYTES)+1);
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
