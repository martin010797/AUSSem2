package Tests;

import Structure.BST23;
import Structure.BST23Node;
import Structure.UnsortedFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Tester {
    public void testInsert(int pNumberOfValues){
        UnsortedFile<TestingData> testFile = new UnsortedFile<>("testName", TestingData.class);

        //vygenerovania hodnot ktore budeme vkladat
        ArrayList<TestingData> values = new ArrayList<TestingData>();
        HashMap<Integer,TestingData> addressesOfInsertedData = new HashMap<>();
        for (int i = 0; i < pNumberOfValues; i++){
            TestingData t = new TestingData(i+1,ThreadLocalRandom.current().nextInt(1, 500),"data "+i);
            values.add(t);
        }
        int numberOfBadInserts = 0;
        for (int i = 0; i < pNumberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            //spristupnenie vkladaneho prvku
            TestingData d = values.get(randomIndex);
            values.remove(randomIndex);
            //vkladanie do neutriedeneho suboru a ulozenie jeho adresy do hashmap
            int address = testFile.insert(d);
            if (address == -1){
                System.out.println("Nevlozene");
                numberOfBadInserts++;
            }else {
                addressesOfInsertedData.put(address,d);
            }
        }
        System.out.println("Pocet chybnych vlozeni: " + numberOfBadInserts);
        //prejdenie celeho hashmap a overenie ci su spravne hodnoty
        int wrongValuesForAddress = 0;
        Iterator hashIterator = addressesOfInsertedData.entrySet().iterator();
        while (hashIterator.hasNext()){
            Map.Entry hashItem = (Map.Entry) hashIterator.next();
            int key = (int) hashItem.getKey();
            TestingData data = (TestingData) hashItem.getValue();
            TestingData foundData = testFile.find(key);
            if (foundData.getId() != data.getId()){
                wrongValuesForAddress++;
            }else if(foundData.getTestInteger() != data.getTestInteger()){
                wrongValuesForAddress++;
            }else if (!foundData.getTestString().equals(data.getTestString())){
                wrongValuesForAddress++;
            }
        }
        System.out.println("Celkovy pocet testovanych hodnot = " + pNumberOfValues);
        System.out.println("Pocet nespravnych hodnot = " + wrongValuesForAddress);
        testFile.endWorkWithFile();
    }

    public void testDelete(int pNumberOfValues){
        UnsortedFile<TestingData> testFile = new UnsortedFile<>("testName", TestingData.class);

        //vygenerovania hodnot ktore budeme vkladat
        ArrayList<TestingData> values = new ArrayList<TestingData>();
        HashMap<Integer,TestingData> addressesOfInsertedData = new HashMap<>();
        for (int i = 0; i < pNumberOfValues; i++){
            TestingData t = new TestingData(i+1,ThreadLocalRandom.current().nextInt(1, 500),"data "+i);
            values.add(t);
        }
        int numberOfBadInserts = 0;
        for (int i = 0; i < pNumberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            //spristupnenie vkladaneho prvku
            TestingData d = values.get(randomIndex);
            values.remove(randomIndex);
            //vkladanie do neutriedeneho suboru a ulozenie jeho adresy do hashmap
            int address = testFile.insert(d);
            if (address == -1){
                System.out.println("Nevlozene");
                numberOfBadInserts++;
            }else {
                addressesOfInsertedData.put(address,d);
            }
        }

        //prejdenie celeho hashmap a mazanie prvkov zo suboru
        int notDeleted = 0;
        Iterator hashIterator = addressesOfInsertedData.entrySet().iterator();
        while (hashIterator.hasNext()){
            Map.Entry hashItem = (Map.Entry) hashIterator.next();
            int key = (int) hashItem.getKey();
            if (!testFile.delete(key)){
                notDeleted++;
            }
        }
        System.out.println("Celkovy pocet testovanych hodnot = " + pNumberOfValues);
        System.out.println("Pocet nezmazanych hodnot = " + notDeleted);
        testFile.endWorkWithFile();
    }

    public void testRandomOperation(int pNumberOfValues, double pProbabilityOfInsert){
        UnsortedFile<TestingData> testFile = new UnsortedFile<>("testName", TestingData.class);

        //vygenerovania hodnot ktore budeme vkladat
        ArrayList<TestingData> values = new ArrayList<TestingData>();
        ArrayList<Integer> usedAddresses = new ArrayList<>();
        HashMap<Integer,TestingData> addressesOfInsertedData = new HashMap<>();
        for (int i = 0; i < pNumberOfValues; i++){
            TestingData t = new TestingData(i+1,ThreadLocalRandom.current().nextInt(1, 500),"data "+i);
            values.add(t);
        }
        int numberOfBadInserts = 0;
        int notDeleted = 0;
        for (int i = 0; i < pNumberOfValues; i++){
            double typeOfOperation = Math.random();
            if (typeOfOperation < pProbabilityOfInsert){
                //insert
                int randomIndex;
                if (values.size() == 1){
                    randomIndex = 0;
                }else {
                    randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
                }
                //spristupnenie vkladaneho prvku
                TestingData d = values.get(randomIndex);
                values.remove(randomIndex);
                //vkladanie do neutriedeneho suboru a ulozenie jeho adresy do hashmap
                int address = testFile.insert(d);
                if (address == -1){
                    //System.out.println("Nevlozene");
                    numberOfBadInserts++;
                }else {
                    addressesOfInsertedData.put(address,d);
                    usedAddresses.add(address);
                }
            }else {
                //delete
                if (addressesOfInsertedData.isEmpty()){
                    System.out.println("Nie je co mazat");
                }else {
                    int randomIndex;
                    randomIndex = ThreadLocalRandom.current().nextInt(0, usedAddresses.size());
                    int key = usedAddresses.get(randomIndex);
                    addressesOfInsertedData.remove(key);
                    usedAddresses.remove(randomIndex);
                    if (!testFile.delete(key)){
                        notDeleted++;
                    }
                }
            }
        }
        //ak su nejake z vkladanych dat este nevymazane tak overit ci maju spravne hodnoty cez find
        int wrongValuesForAddress = 0;
        Iterator hashIterator = addressesOfInsertedData.entrySet().iterator();
        while (hashIterator.hasNext()){
            Map.Entry hashItem = (Map.Entry) hashIterator.next();
            int key = (int) hashItem.getKey();
            TestingData data = (TestingData) hashItem.getValue();
            TestingData foundData = testFile.find(key);
            if (foundData.getId() != data.getId()){
                wrongValuesForAddress++;
            }else if(foundData.getTestInteger() != data.getTestInteger()){
                wrongValuesForAddress++;
            }else if (!foundData.getTestString().equals(data.getTestString())){
                wrongValuesForAddress++;
            }
        }
        System.out.println("Celkovy pocet testovanych zaznamov = " + pNumberOfValues);
        System.out.println("Pocet nespravne vlozenych zaznamov = " + numberOfBadInserts);
        System.out.println("Pocet nezmazanych zaznamov = " + notDeleted);
        System.out.println("Pocet zaznamov so zlymi hodnotami = " + wrongValuesForAddress);

        testFile.endWorkWithFile();
    }

    public void test(){
        UnsortedFile<TestingData2> testFile = new UnsortedFile<>("test", TestingData2.class);

        for (int i = 0; i < 20; i++){
            if (i == 0){
                TestingData2 t = new TestingData2("Kostelej2", 1, 2);
                testFile.insert(t);
            }else {
                TestingData2 t = new TestingData2("string"+i, i, i);
                testFile.insert(t);
            }
        }
        testFile.endWorkWithFile();
    }

    public void testFindMe(){
        UnsortedFile<TestingData2> testFile = new UnsortedFile<>("test", TestingData2.class);

        TestingData2 t = testFile.find(8);
        System.out.println(t.getTestString());
        System.out.println(t.getTestInteger());
        System.out.println(t.getTestDouble());

        testFile.endWorkWithFile();
    }

    public void testInsertBTree(int numberOfValues){
        BST23<TestKey,TestingObjectValue> tree = new BST23<>("bTree",TestKey.class, TestingObjectValue.class);
        ArrayList<Integer> values = new ArrayList<Integer>();
        ArrayList<TestData> insertedData = new ArrayList<>();
        for (int i = 0; i < numberOfValues; i++){
            values.add(i+1);
        }
        for (int i = 0; i < numberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            TestKey d = new TestKey(values.get(randomIndex));
            TestingObjectValue v = new TestingObjectValue(randomIndex, randomIndex+1);
            TestData data = new TestData(d,v);
            TestKey d2 = new TestKey(values.get(randomIndex));
            TestingObjectValue v2 = new TestingObjectValue(randomIndex, randomIndex+1);
            TestData data2 = new TestData(d2,v2);
            //insertedData.add(data2);
            values.remove(randomIndex);
            if(!tree.insert(data)){
                System.out.println("nevlozene");
                //insertedData.remove(insertedData.size()-1);
            }else {
                System.out.println("Vlozene cislo " + data2.get_data1().getKey());
                insertedData.add(data2);
            }
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " neulozene spravne");
            }
        }

        for (int i = 0; i < insertedData.size(); i++){
            TestKey d = new TestKey(insertedData.get(i).get_data1().getKey());
            TestData data = new TestData(d);
            BST23Node foundData = tree.find(data);
            if (foundData == null){
                System.out.println(data.get_data1().getKey() + " nenajdene");
            }else {
                if (((TestKey) foundData.get_data1()).getKey() != insertedData.get(i).get_data1().getKey() &&
                        ((TestKey) foundData.get_data2()).getKey() != insertedData.get(i).get_data1().getKey()){
                    System.out.println("Hodnota kluca nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue1() != insertedData.get(i).get_value1().getValue1() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue1() != insertedData.get(i).get_value1().getValue1()){
                    System.out.println("Hodnota prveho integera nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue2() != insertedData.get(i).get_value1().getValue2() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue2() != insertedData.get(i).get_value1().getValue2()){
                    System.out.println("Hodnota druheho integera nespravna");
                }
            }
        }
        tree.endWorkWithFile();
    }

    public void testDeleteBTree(int numberOfValues){
        BST23<TestKey,TestingObjectValue> tree = new BST23<>("bTree",TestKey.class, TestingObjectValue.class);
        ArrayList<Integer> values = new ArrayList<Integer>();
        ArrayList<TestData> insertedData = new ArrayList<>();
        for (int i = 0; i < numberOfValues; i++){
            values.add(i+1);
        }
        //vkladanie
        for (int i = 0; i < numberOfValues; i++){
            int randomIndex;
            randomIndex = ThreadLocalRandom.current().nextInt(0, values.size());
            TestKey d = new TestKey(values.get(randomIndex));
            TestingObjectValue v = new TestingObjectValue(randomIndex, randomIndex+1);
            TestData data = new TestData(d,v);
            TestKey d2 = new TestKey(values.get(randomIndex));
            TestingObjectValue v2 = new TestingObjectValue(randomIndex, randomIndex+1);
            TestData data2 = new TestData(d2,v2);
            values.remove(randomIndex);
            if(!tree.insert(data)){
                System.out.println("nevlozene");
                //insertedData.remove(insertedData.size()-1);
            }else {
                System.out.println("Vlozene cislo " + data2.get_data1().getKey());
                insertedData.add(data2);
            }
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " neulozene spravne");
            }
        }
        //mazanie vsetkych vlozenych

        int notDeleted = 0;
        for (int i = 0; i < insertedData.size(); i++){
            TestKey d = new TestKey(insertedData.get(i).get_data1().getKey());
            TestData data = new TestData(d);
            BST23Node foundData = tree.find(data);
            if (foundData == null){
                System.out.println(data.get_data1().getKey() + " nenajdene");
            }else {
                //najprv sa skontroluje ci ma dobre hodnoty
                if (((TestKey) foundData.get_data1()).getKey() != insertedData.get(i).get_data1().getKey() &&
                        ((TestKey) foundData.get_data2()).getKey() != insertedData.get(i).get_data1().getKey()){
                    System.out.println("Hodnota kluca nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue1() != insertedData.get(i).get_value1().getValue1() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue1() != insertedData.get(i).get_value1().getValue1()){
                    System.out.println("Hodnota prveho integera nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue2() != insertedData.get(i).get_value1().getValue2() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue2() != insertedData.get(i).get_value1().getValue2()){
                    System.out.println("Hodnota druheho integera nespravna");
                }
                //potom sa data vymazu
                if (!tree.delete(data)){
                    System.out.println(data.get_data1().getKey() + " nevymazane");
                    notDeleted++;
                }
            }
        }
        System.out.println("Pocet nevymazanych udajov: " + notDeleted);
        tree.endWorkWithFile();
    }

    public void testRandomOperationBTree(int numberOfValues, double probabilityOfInsert){
        BST23<TestKey,TestingObjectValue> tree = new BST23<>("bTree",TestKey.class, TestingObjectValue.class);
        ArrayList<Integer> valuesForInsert = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            valuesForInsert.add(i+1);
        }
        ArrayList<TestData> insertedData = new ArrayList<>();
        //ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            double typeOfOperation = Math.random();
            //System.out.println(i + ". operacia: " + typeOfOperation);
            if (typeOfOperation < probabilityOfInsert){
                //insert
                int randomIndex;
                randomIndex = ThreadLocalRandom.current().nextInt(0, valuesForInsert.size());
                TestKey d = new TestKey(valuesForInsert.get(randomIndex));
                TestingObjectValue v = new TestingObjectValue(randomIndex, randomIndex+1);
                TestData data = new TestData(d,v);

                TestKey d2 = new TestKey(valuesForInsert.get(randomIndex));
                TestingObjectValue v2 = new TestingObjectValue(randomIndex, randomIndex+1);
                TestData data2 = new TestData(d2,v2);

                valuesForInsert.remove(randomIndex);
                if(!tree.insert(data)){
                    System.out.println("nevlozene");
                    //insertedData.remove(insertedData.size()-1);
                }else {
                    System.out.println("Vlozene cislo " + data2.get_data1().getKey());
                    insertedData.add(data2);
                }
                if (tree.find(data2) == null){
                    System.out.println(data2.get_data1().getKey() + " neulozene spravne");
                }
            }else {
                //delete
                if (tree.get_root() == null || insertedData.size() == 0){
                    System.out.println("Nie je co mazat");
                }else {
                    int randomIndex;
                    randomIndex = ThreadLocalRandom.current().nextInt(0, insertedData.size());
                    TestKey d = new TestKey(insertedData.get(randomIndex).get_data1().getKey());
                    TestData data = new TestData(d);
                    int keyOfData = data.get_data1().getKey();
                    System.out.println("pojde sa mazat " + keyOfData);
                    if (!tree.delete(data)){
                        System.out.println(keyOfData + " nevymazane");
                    }else {
                        System.out.println("Vymazane cislo " + keyOfData);
                        insertedData.remove(randomIndex);
                    }
                }
            }
        }
        int notFound = 0;
        for (TestData value: insertedData) {
            BST23Node foundData = tree.find(value);
            if (foundData == null){
                System.out.println(value.get_data1().getKey() + " nenajdene");
                notFound++;
            }else {
                if (((TestKey) foundData.get_data1()).getKey() != value.get_data1().getKey() &&
                        ((TestKey) foundData.get_data2()).getKey() != value.get_data1().getKey()){
                    System.out.println("Hodnota kluca nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue1() != value.get_value1().getValue1() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue1() != value.get_value1().getValue1()){
                    System.out.println("Hodnota prveho integera nespravna");
                }
                if (((TestingObjectValue) foundData.get_value1()).getValue2() != value.get_value1().getValue2() &&
                        ((TestingObjectValue) foundData.get_value2()).getValue2() != value.get_value1().getValue2()){
                    System.out.println("Hodnota druheho integera nespravna");
                }
            }
        }
        System.out.println("Pocet nenajdenych prvkov po operaciach: " + notFound);
        tree.endWorkWithFile();
    }

    public void testRandomOperationBTree2(int numberOfValues, double probabilityOfInsert){
        BST23<TestKey,TestingData2> tree = new BST23<>("bTree",TestKey.class, TestingData2.class);
        ArrayList<Integer> valuesForInsert = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            valuesForInsert.add(i+1);
        }
        ArrayList<TestData2> insertedData = new ArrayList<>();
        for (int i = 0; i < numberOfValues; i++){
            double typeOfOperation = Math.random();
            //System.out.println(i + ". operacia: " + typeOfOperation);
            if (typeOfOperation < probabilityOfInsert){
                //insert
                int randomIndex;
                randomIndex = ThreadLocalRandom.current().nextInt(0, valuesForInsert.size());
                TestKey d = new TestKey(valuesForInsert.get(randomIndex));
                TestingData2 v = new TestingData2("string"+i, i, i);
                TestData2 data = new TestData2(d,v);

                TestKey d2 = new TestKey(valuesForInsert.get(randomIndex));
                TestingData2 v2 = new TestingData2("string"+i, i, i);
                TestData2 data2 = new TestData2(d2,v2);

                valuesForInsert.remove(randomIndex);
                if(!tree.insert(data)){
                    System.out.println("nevlozene");
                    //insertedData.remove(insertedData.size()-1);
                }else {
                    System.out.println("Vlozene cislo " + data2.get_data1().getKey());
                    insertedData.add(data2);
                }
                if (tree.find(data2) == null){
                    System.out.println(data2.get_data1().getKey() + " neulozene spravne");
                }
            }else {
                //delete
                if (tree.get_root() == null || insertedData.size() == 0){
                    System.out.println("Nie je co mazat");
                }else {
                    int randomIndex;
                    randomIndex = ThreadLocalRandom.current().nextInt(0, insertedData.size());
                    TestKey d = new TestKey(insertedData.get(randomIndex).get_data1().getKey());
                    TestData2 data = new TestData2(d);
                    int keyOfData = data.get_data1().getKey();
                    System.out.println("pojde sa mazat " + keyOfData);
                    if (!tree.delete(data)){
                        System.out.println(keyOfData + " nevymazane");
                    }else {
                        System.out.println("Vymazane cislo " + keyOfData);
                        insertedData.remove(randomIndex);
                    }
                }
            }
        }
        int notFound = 0;
        for (TestData2 value: insertedData) {
            BST23Node foundData = tree.find(value);
            if (foundData == null){
                System.out.println(value.get_data1().getKey() + " nenajdene");
                notFound++;
            }else {
                if (((TestKey) foundData.get_data1()).getKey() != value.get_data1().getKey() &&
                        ((TestKey) foundData.get_data2()).getKey() != value.get_data1().getKey()){
                    System.out.println("Hodnota kluca nespravna");
                }
                if (((TestingData2) foundData.get_value1()).getTestInteger() != value.get_value1().getTestInteger() &&
                        ((TestingData2) foundData.get_value2()).getTestInteger() != value.get_value1().getTestInteger()){
                    System.out.println("Hodnota integera nespravna");
                }
                if ((((TestingData2) foundData.get_value1()).getTestString()).equals(value.get_value1().getTestString()) &&
                        (((TestingData2) foundData.get_value2()).getTestString()).equals(value.get_value1().getTestString())){
                    System.out.println("Hodnota stringu nespravna");
                }
            }
        }
        System.out.println("Pocet nenajdenych prvkov po operaciach: " + notFound);
        tree.endWorkWithFile();
    }

    public void listAllDataFromFile2(){
        BST23<TestKey,TestingData2> tree = new BST23<>("bTree",TestKey.class, TestingData2.class);
        ArrayList<BST23Node> listOfNodes = tree.getAllNodesFromFile();
        System.out.println("Vypis vsetkych nodov:");
        for (BST23Node value: listOfNodes) {
            System.out.println("------------------------------------------");
            System.out.println("Node validita: " + value.isValid() + ", Adresa: " + value.get_address());
            TestKey key = (TestKey) value.get_data1();
            TestingData2 data = (TestingData2) value.get_value1();
            //if (key.isValid()){
                System.out.println("Kluc: " + key.getKey() + ", Je validny: " + key.isValid());
                System.out.println("Hodnoty: " + data.getTestInteger() + ", " + data.getTestDouble() + ", " + data.getTestString());
            //}
            key = (TestKey) value.get_data2();
            data = (TestingData2) value.get_value2();
            //if (key.isValid()){
                System.out.println("Kluc: " + key.getKey() + ", Je validny: " + key.isValid());
                System.out.println("Hodnoty: " + data.getTestInteger() + ", " + data.getTestDouble() + ", " + data.getTestString());
            //}
            System.out.println("------------------------------------------");
        }
        tree.endWorkWithFile();
    }

    public void listAllDataFromFile(){
        BST23<TestKey,TestingObjectValue> tree = new BST23<>("bTree",TestKey.class, TestingObjectValue.class);
        ArrayList<BST23Node> listOfNodes = tree.getAllNodesFromFile();
        System.out.println("Vypis vsetkych nodov:");
        for (BST23Node value: listOfNodes) {
            TestKey key = (TestKey) value.get_data1();
            TestingObjectValue data = (TestingObjectValue) value.get_value1();
            if (key.isValid()){
                System.out.println("Kluc: " + key.getKey());
                System.out.println("Hodnoty: " + data.getValue1() + ", " + data.getValue2());
            }
            key = (TestKey) value.get_data2();
            data = (TestingObjectValue) value.get_value2();
            if (key.isValid()){
                System.out.println("Kluc: " + key.getKey());
                System.out.println("Hodnoty: " + data.getValue1() + ", " + data.getValue2());
            }
        }
        tree.endWorkWithFile();
    }
}
