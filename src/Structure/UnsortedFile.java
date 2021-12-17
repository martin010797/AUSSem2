package Structure;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnsortedFile<T extends IData> {
    private static final int HEADER_SIZE = 8;

    private RandomAccessFile fileOfRecords;
    //mysli sa nie presne pocet zaznamov kolko je ale kde je posledny(lebo pri mazani vznikaju prazdne miesta)
    private int numberOfRecords;
    private int nextAddress;
    private Class<T> classType;
    //front s prazdnymi adresami
    private PriorityQueue<Integer> invalidRecordsAdrIncr;
    private PriorityQueue<Integer> invalidRecordsAdrDecr;
    private String nameOfFileOfRecords;

    public UnsortedFile(String pFileName, Class pClassType) {
        nameOfFileOfRecords = pFileName;
        classType = pClassType;
        if (!(new File(pFileName)).exists()){
            setInitialHeader(pFileName);
        }else {
            try {
                fileOfRecords = new RandomAccessFile(pFileName, "rw");
                loadHeader();
            }catch (FileNotFoundException exception){
                Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            }
        }
        invalidRecordsAdrIncr = new PriorityQueue<>();
        invalidRecordsAdrDecr = new PriorityQueue<>(Comparator.reverseOrder());
        loadPriorityQueuesFromFile();
    }

    //vracia adresu kam sa ulozil
    public int insert(T pData){
        //ziskanie pola bytov pre T zaznam
        byte[] arrayOfDataBytes = pData.ToByteArray();
        //naseekovanie na next address a vlozenie na dane miesto
        try {
            if (!invalidRecordsAdrIncr.isEmpty()){
                int savedOnAddress = invalidRecordsAdrIncr.peek();
                fileOfRecords.seek(savedOnAddress);
                fileOfRecords.write(arrayOfDataBytes);
                invalidRecordsAdrDecr.remove(invalidRecordsAdrIncr.poll());
                //savePriorityQueuestoFile();
                return savedOnAddress;
            }else {
                fileOfRecords.seek(nextAddress);
                fileOfRecords.write(arrayOfDataBytes);
                //zvysenie poctu len ked sa pridavalo na koniec
                //tak isto aj pre dalsiu adresu
                numberOfRecords++;
                nextAddress += pData.getSize();
                //ulozit upravene hlavickove subory
                //saveHeader();
                return nextAddress- pData.getSize();
            }
        }catch (IOException exception){
            return -1;
        }
    }

    //metoda ktora uklada do suboru na urcene miesto(vyuzivane na update)
    public boolean updateOnAddress(T item, int pAddress){
        //naseekovanie na zadanu adresu a vlozenie na dane miesto
        try {
            fileOfRecords.seek(pAddress);

            //ziskanie pola bytov pre T zaznam
            byte[] arrayOfDataBytes = item.ToByteArray();

            fileOfRecords.write(arrayOfDataBytes);

            return true;
        }catch (IOException exception){
            return false;
        }
    }

    public T find(int pAddressOfRecord){
        //vytvorenie instancie triedy T
        T data;
        try {
            data = (T) classType.newInstance().createClass();
        }catch (InstantiationException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return null;
        }catch (IllegalAccessException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return null;
        }

        //nacitanie pola bytov zo suboru
        byte[] arrayOfDataBytes = new byte[data.getSize()];
        try {
            fileOfRecords.seek(pAddressOfRecord);
            fileOfRecords.read(arrayOfDataBytes);
        }catch (IOException exception){
            return null;
        }
        //nacitanie hodnot pre T data z pola bytov
        data.FromByteArray(arrayOfDataBytes);

        //kontrola platnosti zaznamu
        if (data.isValid()){
            return data;
        }else {
            return null;
        }
    }

    public boolean delete(int pAddressOfDeletedData) {
        //vytvorenie instancie triedy T
        T data;
        try {
            data = (T) classType.newInstance().createClass();
        }catch (InstantiationException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }catch (IllegalAccessException exception){
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }

        //nacitanie pola bytov zo suboru
        byte[] arrayOfDataBytes = new byte[data.getSize()];
        long lengthOfFile = 0;
        try {
            fileOfRecords.seek(pAddressOfDeletedData);
            fileOfRecords.read(arrayOfDataBytes);
            lengthOfFile = fileOfRecords.length();
        }catch (IOException exception){
            return false;
        }
        //spristupnenie dat
        data.FromByteArray(arrayOfDataBytes);
        //nastavenie isValid na false
        data.setValid(false);

        //ak bol zaznam na konci suboru tak skratit subor o pocet volnych miest
        if(pAddressOfDeletedData == (lengthOfFile - data.getSize())){
            int numberOfEmptySpaces = 1;
            Integer unusedAddress = invalidRecordsAdrDecr.peek();
            while (unusedAddress != null && unusedAddress == (pAddressOfDeletedData - data.getSize() * numberOfEmptySpaces)){
                invalidRecordsAdrIncr.remove(unusedAddress);
                invalidRecordsAdrDecr.poll();
                numberOfEmptySpaces++;
                unusedAddress = invalidRecordsAdrDecr.peek();
            }
            try {
                fileOfRecords.setLength(lengthOfFile - (numberOfEmptySpaces * data.getSize()));
            }catch (IOException exception){
                return false;
            }
            numberOfRecords -= numberOfEmptySpaces;
            nextAddress = numberOfRecords * data.getSize() + HEADER_SIZE;
            //saveHeader();
        }else {
            //pridat do prioritneho frontu nepouzitu adresu
            invalidRecordsAdrIncr.add(pAddressOfDeletedData);
            invalidRecordsAdrDecr.add(pAddressOfDeletedData);
            //ulozenie upravenych dat(upravena validita zaznamu)
            byte[] arrayOfDeletedBytes = data.ToByteArray();
            try {
                fileOfRecords.seek(pAddressOfDeletedData);
                fileOfRecords.write(arrayOfDeletedBytes);
            }catch (IOException exception){
                return false;
            }
        }
        //ukladanie udajov pre fronty
        //savePriorityQueuestoFile();
        return true;
    }

    private boolean savePriorityQueuestoFile(){
        FileWriter csvWriter = null;
        Iterator incrIterator = invalidRecordsAdrIncr.iterator();
        Iterator decrIterator = invalidRecordsAdrDecr.iterator();
        try {
            csvWriter = new FileWriter(nameOfFileOfRecords + "incr.csv");

            while (incrIterator.hasNext()){
                csvWriter.append(""+ incrIterator.next());
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();

            csvWriter = new FileWriter(nameOfFileOfRecords + "decr.csv");
            while (decrIterator.hasNext()){
                csvWriter.append(""+ decrIterator.next());
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    private boolean loadPriorityQueuesFromFile(){
        try {
            BufferedReader incrReader = new BufferedReader(new FileReader(nameOfFileOfRecords + "incr.csv"));
            String row;
            while ((row = incrReader.readLine()) != null) {
                String[] data = row.split(",");
                if (data[0] != null){
                    invalidRecordsAdrIncr.add(Integer.parseInt(data[0]));
                }
            }
            incrReader.close();

            BufferedReader decrReader = new BufferedReader(new FileReader(nameOfFileOfRecords + "decr.csv"));
            while ((row = decrReader.readLine()) != null) {
                String[] data = row.split(",");
                if (data[0] != null){
                    invalidRecordsAdrDecr.add(Integer.parseInt(data[0]));
                }
            }
            decrReader.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    private boolean loadHeader(){
        //nacitanie udajov pre hlavicku zo suboru do pola bytov
        byte[] arrayOfHeaderBytes = new byte[2*Integer.BYTES];
        try {
            fileOfRecords.seek(0);
            fileOfRecords.read(arrayOfHeaderBytes);
        }catch (IOException exception){
            return false;
        }
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(arrayOfHeaderBytes);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);

        //priradenie hodnot hlavickovych dat
        try {
            numberOfRecords = hlpInStream.readInt();
            nextAddress = hlpInStream.readInt();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveHeader(){
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        //vytvorenie pola bytov pre hlavicku
        try{
            hlpOutStream.writeInt(numberOfRecords);
            hlpOutStream.writeInt(nextAddress);
        }catch (IOException e){
            return false;
        }

        //vytvorenie suboru a zapisanie udajov pre hlavicku
        try {
            //fileOfRecords = new RandomAccessFile(nameOfFileOfRecords, "rw");
            fileOfRecords.seek(0);
            fileOfRecords.write(hlpByteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
        return true;
    }

    private boolean setInitialHeader(String pFileName){
        ByteArrayOutputStream hlpByteArrayOutputStream= new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        //vytvorenie pola bytov pre hlavicku
        try{
            //v hlavicke pocet zaznamov v subore(do poctu rata aj prazdne miesta)
            // a dalsia adresa ak sa nevklada do prazdnych miest
            hlpOutStream.writeInt(0);
            hlpOutStream.writeInt(2*Integer.BYTES);
        }catch (IOException e){
            return false;
        }

        //vytvorenie suboru a zapisanie inicializacnych udajov pre hlavicku
        try {
            fileOfRecords = new RandomAccessFile(pFileName, "rw");
            fileOfRecords.seek(0);
            fileOfRecords.write(hlpByteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
        numberOfRecords = 0;
        nextAddress = HEADER_SIZE;
        return true;
    }

    public boolean endWorkWithFile(){
        saveHeader();
        savePriorityQueuestoFile();
        try {
            fileOfRecords.close();
        }catch (IOException exception){
            return false;
        }
        return true;
    }

    public ArrayList<RecordWithAddress<T>> getAllRecordsFromFile(){
        ArrayList<RecordWithAddress<T>> listOfRecords = new ArrayList<>();
        if (nextAddress == HEADER_SIZE){
            return listOfRecords;
        }else {
            int next = HEADER_SIZE;
            //nacitanie celeho suboru
            byte[] arrayOfDataBytes;
            try {
                arrayOfDataBytes = new byte[nextAddress];
                fileOfRecords.seek(0);
                fileOfRecords.read(arrayOfDataBytes);
            }catch (IOException exception){
                return null;
            }
            while (next < nextAddress){
                T data;
                try {
                    data = (T) classType.newInstance().createClass();
                }catch (InstantiationException exception){
                    Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
                    return null;
                }catch (IllegalAccessException exception){
                    Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
                    return null;
                }

                //kopirovanie casti nacitaneho suboru
                byte[] partOfByteArray = Arrays.copyOfRange(
                        arrayOfDataBytes,
                        next,
                        next + data.getSize());
                data.FromByteArray(partOfByteArray);
                //pridanie zaznamu
                listOfRecords.add(new RecordWithAddress<T>(data,next));
                next += data.getSize();
            }
            //stary sposob citania
            /*int next = HEADER_SIZE;
            while (next < nextAddress){
                T data;
                try {
                    data = (T) classType.newInstance().createClass();
                }catch (InstantiationException exception){
                    Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
                    return null;
                }catch (IllegalAccessException exception){
                    Logger.getLogger(UnsortedFile.class.getName()).log(Level.SEVERE, null, exception);
                    return null;
                }
                //nacitanie pola bytov zo suboru
                byte[] arrayOfDataBytes = new byte[data.getSize()];
                try {
                    fileOfRecords.seek(next);
                    fileOfRecords.read(arrayOfDataBytes);
                }catch (IOException exception){
                    return null;
                }

                //nacitanie hodnot pre node z pola bytov
                data.FromByteArray(arrayOfDataBytes);
                listOfRecords.add(new RecordWithAddress<T>(data,next));
                next += data.getSize();
            }*/
            return listOfRecords;
        }
    }
}
