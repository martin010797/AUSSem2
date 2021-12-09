package Main_system;

import Models.*;
import Structure.BST23;
import Structure.BST23Node;
import Structure.NodeWithKey;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PCRSystem {
    private BST23<PersonKey, Person> treeOfPeople = new BST23<>();
    private BST23<DistrictKey, District> treeOfDistricts = new BST23<>();
    private BST23<RegionKey, Region> treeOfRegions = new BST23<>();
    private BST23<WorkplaceKey, Workplace> treeOfWorkplace = new BST23<>();

    public PCRSystem() {
    }

    public PCRSystem(int pNumberOfRegions,
                     int pNumberOfDistricts,
                     int pNumberOfWorkplaces,
                     int pNumberOfPeople,
                     int pNumberOfTests){
        generateDistrictsRegionsAndWorkplaces(
                pNumberOfRegions,
                pNumberOfDistricts,
                pNumberOfWorkplaces,
                pNumberOfPeople,
                pNumberOfTests);
    }

    public ResponseAndPCRTestId insertPCRTest(String personIdNumber,
                                              int yearOfTest,
                                              int monthOfTest,
                                              int dayOfTest,
                                              int hourOfTest,
                                              int minuteOfTest,
                                              int secondOfTest,
                                              int workplaceId,
                                              int districtId,
                                              int regionId,
                                              boolean result,
                                              String description,
                                              String pTestId){
        //overuje sa ci osoba pre dany system existuje. ak nie tak hod chybu
        PersonKey pKey = new PersonKey(personIdNumber);
        PersonData pData = new PersonData(pKey,null);
        BST23Node testedPersonNode = treeOfPeople.find(pData);
        if (testedPersonNode == null){
            //osoba sa v systeme nenachadza
            return new ResponseAndPCRTestId(ResponseType.PERSON_DOESNT_EXIST,null);
        }else {
            //vytvorenie testu
            Person person;
            if (((PersonKey) testedPersonNode.get_data1()).getIdNumber().equals(personIdNumber)){
                person = ((Person) testedPersonNode.get_value1());
            }else{
                person = ((Person) testedPersonNode.get_value2());
            }
            PCR testValue = new PCR(
                    yearOfTest,
                    monthOfTest,
                    dayOfTest,
                    hourOfTest,
                    minuteOfTest,
                    secondOfTest,
                    personIdNumber,
                    workplaceId,
                    districtId,
                    regionId,
                    result,
                    description,
                    person,
                    pTestId);
            PCRKey testKey = new PCRKey(testValue.getPCRId());
            PCRData personTestData = new PCRData(testKey, testValue);
            //vlozenie testu do stromu testov v osobe
            if(!person.insertPCRForPerson(personTestData)){
                return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
            }
            //vlozenie testu do stromov v osobe podla datumu
            PCRKeyDate pKeyDate = new PCRKeyDate(testValue.getDateAndTimeOfTest());
            PCRWorkplaceData pDateData = new PCRWorkplaceData(pKeyDate,testValue);
            if (!person.insertPCRByDateForPerson(pDateData)){
                //nepodarilo sa vlozit tak vymaz z testov pre osobu podla id testu
                PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                person.deletePCRTest(deletedPersonTestData);
                return new ResponseAndPCRTestId(ResponseType.PCR_EXISTS_FOR_THAT_TIME, testValue.getPCRId().toString());
            }
            //pre dany okres vlozi test
            DistrictKey dKey = new DistrictKey(districtId);
            DistrictData dData = new DistrictData(dKey,null);
            BST23Node testedDistrictNode = treeOfDistricts.find(dData);
            if(testedDistrictNode == null){
                //vymaze sa test z osoby lebo sa nemoze vkladat do systemu pokial neexistuje okres
                PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                person.deletePCRTest(deletedPersonTestData);
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                return new ResponseAndPCRTestId(ResponseType.DISTRICT_DOESNT_EXIST,testValue.getPCRId().toString());
            }else {
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRDistrictPositiveData districtTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId){
                    testValue.setDistrict(((District) testedDistrictNode.get_value1()));
                    if(!((District) testedDistrictNode.get_value1()).insertTest(districtTestData)){
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
                    }
                }else {
                    testValue.setDistrict(((District) testedDistrictNode.get_value2()));
                    if (!((District) testedDistrictNode.get_value2()).insertTest(districtTestData)){
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
                    }
                }
            }
            //pre dany kraj vlozi test
            RegionKey rKey = new RegionKey(regionId);
            RegionData rData = new RegionData(rKey,null);
            BST23Node testedRegionNode = treeOfRegions.find(rData);
            if(testedRegionNode == null){
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                    ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                }else {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                    ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                }
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                person.deletePCRTest(deletedPersonTestData);
                return new ResponseAndPCRTestId(ResponseType.REGION_DOESNT_EXIST, testValue.getPCRId().toString());
            }else {
                PCRKeyRegion pcrKeyRegion = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRRegionData regionTestData = new PCRRegionData(pcrKeyRegion,testValue);
                if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                    testValue.setRegion(((Region) testedRegionNode.get_value1()));
                    if (!((Region) testedRegionNode.get_value1()).insertTest(regionTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_WITH_ID_EXISTS,
                                testValue.getPCRId().toString());
                    }
                }else {
                    testValue.setRegion(((Region) testedRegionNode.get_value2()));
                    if (!((Region) testedRegionNode.get_value2()).insertTest(regionTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_WITH_ID_EXISTS,
                                testValue.getPCRId().toString());
                    }
                }
            }
            //pre dane pracovisko vlozi test
            WorkplaceKey wKey = new WorkplaceKey(workplaceId);
            WorkplaceData wData = new WorkplaceData(wKey,null);
            BST23Node workplaceNode = treeOfWorkplace.find(wData);
            if(workplaceNode == null){
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                    ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                }else {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                    ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                }
                if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                    PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                    ((Region) testedRegionNode.get_value1()).deletePCRTest(deletedRegionTestData);
                }else {
                    PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                    ((Region) testedRegionNode.get_value2()).deletePCRTest(deletedRegionTestData);
                }
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                person.deletePCRTest(deletedPersonTestData);
                return new ResponseAndPCRTestId(
                        ResponseType.WORKPLACE_DOESNT_EXIST,
                        testValue.getPCRId().toString());
            }else {
                PCRKeyDate testWorkplaceKey = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData workplaceTestData = new PCRWorkplaceData(testWorkplaceKey, testValue);
                if (((WorkplaceKey) workplaceNode.get_data1()).getWorkplaceId() == workplaceId){
                    testValue.setWorkplace(((Workplace) workplaceNode.get_value1()));
                    if (!((Workplace) workplaceNode.get_value1()).insertTest(workplaceTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                        }
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                            ((Region) testedRegionNode.get_value1()).deletePCRTest(deletedRegionTestData);
                        }else {
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                            ((Region) testedRegionNode.get_value2()).deletePCRTest(deletedRegionTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_EXISTS_FOR_THAT_TIME,
                                testValue.getPCRId().toString());
                    }
                }else {
                    testValue.setWorkplace(((Workplace) workplaceNode.get_value2()));
                    if (!((Workplace) workplaceNode.get_value2()).insertTest(workplaceTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value1()).deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
                            ((District) testedDistrictNode.get_value2()).deletePCRTest(deletedDistrictTestData);
                        }
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                            ((Region) testedRegionNode.get_value1()).deletePCRTest(deletedRegionTestData);
                        }else {
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
                            ((Region) testedRegionNode.get_value2()).deletePCRTest(deletedRegionTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
                        person.deletePCRTest(deletedPersonTestData);
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_EXISTS_FOR_THAT_TIME,
                                testValue.getPCRId().toString());
                    }
                }
            }
            return new ResponseAndPCRTestId(ResponseType.SUCCESS,testValue.getPCRId().toString());
        }
    }

    public boolean insertPerson(String name, String surname, int year, int month, int day, String idNumber){
        PersonKey pKey = new PersonKey(idNumber);
        Person pValue = new Person(name,surname,year,month,day,idNumber);
        PersonData pData = new PersonData(pKey,pValue);
        return treeOfPeople.insert(pData);
    }

    public ResponseType deletePerson(String personId){
        //najdenie osoby
        PersonKey personKey = new PersonKey(personId);
        PersonData personData = new PersonData(personKey, null);
        BST23Node personNode = treeOfPeople.find(personData);
        if (personNode == null){
            return ResponseType.PERSON_DOESNT_EXIST;
        }
        Person person;
        if(((PersonKey) personNode.get_data1()).getIdNumber().equals(personId)){
            person = ((Person) personNode.get_value1());
        }else {
            person = ((Person) personNode.get_value2());
        }

        //prechadzanie vsetkych testov a ich mazanie
        ArrayList<NodeWithKey> listOfTests = new ArrayList<NodeWithKey>();
        NodeWithKey nextTestNode = person.getTreeOfTests().getFirst();
        if (nextTestNode != null){
            //pridavanie testov do docasneho array listu
            PCRKey tempPCRKey = new PCRKey(((PCRKey) nextTestNode.getKey()).getPCRId());
            PCR tempPCRValue;
            if (((PCRKey) nextTestNode.getNode().get_data1()).compareTo(((PCRKey) nextTestNode.getKey())) == 0){
                tempPCRValue = ((PCR) nextTestNode.getNode().get_value1());
            }else {
                tempPCRValue = ((PCR) nextTestNode.getNode().get_value2());
            }
            BST23Node tempNode = new PCRData(tempPCRKey,tempPCRValue);
            listOfTests.add(new NodeWithKey(tempNode,tempPCRKey));
            nextTestNode = person.getTreeOfTests().getNext(nextTestNode.getNode(), ((PCRKey) nextTestNode.getKey()));
            while (nextTestNode != null){
                tempPCRKey = new PCRKey(((PCRKey) nextTestNode.getKey()).getPCRId());
                if (((PCRKey) nextTestNode.getNode().get_data1()).compareTo(((PCRKey) nextTestNode.getKey())) == 0){
                    tempPCRValue = ((PCR) nextTestNode.getNode().get_value1());
                }else {
                    tempPCRValue = ((PCR) nextTestNode.getNode().get_value2());
                }
                tempNode = new PCRData(tempPCRKey,tempPCRValue);
                listOfTests.add(new NodeWithKey(tempNode,tempPCRKey));
                nextTestNode = person.getTreeOfTests().getNext(nextTestNode.getNode(), ((PCRKey) nextTestNode.getKey()));
            }
            //mazanie testov
            for (int i = 0; i < listOfTests.size(); i++){
                NodeWithKey personNodeWithKey = new NodeWithKey(personNode,personKey);
                if (!deleteTestInAllTrees(personNodeWithKey, listOfTests.get(i).getNode(), (PCRKey) listOfTests.get(i).getKey())){
                    return ResponseType.PROBLEM_WITH_DELETING;
                }
            }
        }
        //vymazanie osoby
        if (!treeOfPeople.delete(personData)){
            return ResponseType.PROBLEM_WITH_DELETING;
        }
        return ResponseType.SUCCESS;
    }

    public ResponseType deletePCRTest(String PCRId){
        //vytvorenie UUID kluca a vyskladanie data objektu pre test ktory budeme chciet mazat
        PCRKey tKey;
        try{
            tKey = new PCRKey(UUID.fromString(PCRId));
        }catch (Exception exception){
            return ResponseType.INCORRECT_PCR_FORMAT;
        }
        PCRData tData = new PCRData(tKey, null);
        //najdenie testu
        NodeWithKey firstNode = treeOfPeople.getFirst();
        BST23Node testResult;
        if (firstNode == null){
            return ResponseType.PERSON_DOESNT_EXIST;
        }else {
            testResult = ((Person) firstNode.getNode().get_value1()).getTreeOfTests().find(tData);
        }
        NodeWithKey nextNode = null;
        if (testResult == null){
            //postupne prehladava osoby
            nextNode = treeOfPeople.getNext(firstNode.getNode(), ((PersonKey) firstNode.getKey()));
            while (nextNode != null){
                if (((PersonKey) nextNode.getKey()).getIdNumber().equals(((PersonKey) nextNode.getNode().get_data1()).getIdNumber())){
                    testResult = ((Person) nextNode.getNode().get_value1()).getTreeOfTests().find(tData);
                }else {
                    testResult = ((Person) nextNode.getNode().get_value2()).getTreeOfTests().find(tData);
                }
                if (testResult != null){
                    break;
                }
                nextNode = treeOfPeople.getNext(nextNode.getNode(), ((PersonKey) nextNode.getKey()));
                if (nextNode == null){
                    return ResponseType.PCR_DOESNT_EXIST;
                }
            }
        }else{
            nextNode = firstNode;
        }
        //aktualne je v nextNode osoba a v testResult test ktory chceme mazat
        if(deleteTestInAllTrees(nextNode, testResult, tKey)){
            return ResponseType.SUCCESS;
        }else {
            return ResponseType.PROBLEM_WITH_DELETING;
        }
    }

    private boolean deleteTestInAllTrees(NodeWithKey personNode, BST23Node testResult, PCRKey testKey){
        PCR testValue;
        Person person;
        if (((PCRKey) testResult.get_data1()).compareTo(testKey) == 0){
            testValue = ((PCR) testResult.get_value1());
        }else {
            testValue = ((PCR) testResult.get_value2());
        }
        if (((PersonKey) personNode.getKey()).getIdNumber().equals(((PersonKey) personNode.getNode().get_data1()).getIdNumber())){
            person = ((Person) personNode.getNode().get_value1());
        }else {
            person = ((Person) personNode.getNode().get_value2());
        }

        //mazanie pre okres
        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
        PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, testValue);
        if(!testValue.getDistrict().deletePCRTest(deletedDistrictTestData)){
            return false;
        }
        //mazanie pre kraj
        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
        PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, testValue);
        if (!testValue.getRegion().deletePCRTest(deletedRegionTestData)){
            return false;
        }
        //mazanie pre pracovisko
        PCRKeyDate workplacePCRKey = new PCRKeyDate(testValue.getDateAndTimeOfTest());
        PCRWorkplaceData deletedWorkplaceData = new PCRWorkplaceData(workplacePCRKey, testValue);
        if (!testValue.getWorkplace().deletePCRTest(deletedWorkplaceData)){
            return false;
        }
        //mazanie pre osobu
        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,testValue);
        if (!person.deletePCRTestByDate(deletedPersonDateTestData)){
            return false;
        }
        PCRData deletedPersonTestData = new PCRData(testKey, testValue);
        if (!person.deletePCRTest(deletedPersonTestData)){
            return false;
        }
        return true;
    }

    private void generateDistrictsRegionsAndWorkplaces(
            int pNumberOfRegions,
            int pNumberOfDistricts,
            int pNumberOfWorkplaces,
            int pNumberOfPeople,
            int pNumberOfTests){
        for (int i = 0; i < pNumberOfRegions; i++){
            RegionKey rKey = new RegionKey(i);
            Region rValue = new Region(i,"Kraj "+i);
            RegionData rData = new RegionData(rKey,rValue);
            treeOfRegions.insert(rData);
        }
        for (int i = 0; i < pNumberOfDistricts; i++){
            DistrictKey dKey = new DistrictKey(i);
            District dValue = new District(i, "Okres "+i);
            DistrictData dData = new DistrictData(dKey,dValue);
            treeOfDistricts.insert(dData);
        }
        for (int i = 0; i < pNumberOfWorkplaces; i++){
            WorkplaceKey wKey = new WorkplaceKey(i);
            Workplace wValue = new Workplace(i);
            WorkplaceData wData = new WorkplaceData(wKey,wValue);
            treeOfWorkplace.insert(wData);
        }
        for (int i = 0; i < pNumberOfPeople; i++){
            PersonKey pKey = new PersonKey(Integer.toString(i+1));
            Person pValue = new Person(
                    "Meno"+(i+1),
                    "Priezvisko"+(i+1),
                    1998,
                    2,
                    3,
                    Integer.toString(i+1));
            PersonData pData = new PersonData(pKey,pValue);
            treeOfPeople.insert(pData);
        }

        /*try {
            File myObj = new File("rod_cislo_test.txt");
            myObj.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        for (int i = 0; i < pNumberOfTests; i++){
            double positivity = Math.random();
            boolean boolPos;
            if (positivity < 0.5){
                boolPos = true;
            }else {
                boolPos = false;
            }
            int randIdPerson = ThreadLocalRandom.current().nextInt(1, pNumberOfPeople+1);
            int randYear = ThreadLocalRandom.current().nextInt(2019, 2022);
            int randMonth = ThreadLocalRandom.current().nextInt(1, 13);
            int randDay = ThreadLocalRandom.current().nextInt(1, 29);
            int randHour = ThreadLocalRandom.current().nextInt(0, 24);
            int randMinute = ThreadLocalRandom.current().nextInt(0, 60);
            int randSecond = ThreadLocalRandom.current().nextInt(0, 60);
            int randWorkplace = ThreadLocalRandom.current().nextInt(0, pNumberOfWorkplaces);
            int randDistrict = ThreadLocalRandom.current().nextInt(0, pNumberOfDistricts);
            int randRegion = ThreadLocalRandom.current().nextInt(0, pNumberOfRegions);
            ResponseAndPCRTestId response = insertPCRTest(
                    Integer.toString(randIdPerson),
                    randYear,
                    randMonth,
                    randDay,
                    randHour,
                    randMinute,
                    randSecond,
                    randWorkplace,
                    randDistrict,
                    randRegion,
                    boolPos,
                    "nejaky popis",
                    null
            );
            /* {
                FileWriter myWriter;
                if (i == 0){
                    myWriter = new FileWriter("rod_cislo_test.txt");
                    myWriter.write("");
                }else{
                    myWriter = new FileWriter("rod_cislo_test.txt", true);
                }
                myWriter.write(randIdPerson + " " + response.getPCRTestId() + "\n");
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public class ResponseAndPCRTestId{
        private ResponseType response;
        private String PCRTestId;

        public ResponseAndPCRTestId(ResponseType response, String PCRTestId) {
            this.response = response;
            this.PCRTestId = PCRTestId;
        }

        public ResponseType getResponse() {
            return response;
        }

        public String getPCRTestId() {
            return PCRTestId;
        }
    }

    public PersonPCRResult findTestResultForPerson(String personId, String pcrId){
        //najdi osobu s danym rodnym cislom
        PersonKey pKey = new PersonKey(personId);
        PersonData pData = new PersonData(pKey,null);
        BST23Node testedPersonNode = treeOfPeople.find(pData);
        if (testedPersonNode == null){
            //osoba sa v systeme nenachadza
            return new PersonPCRResult(ResponseType.PERSON_DOESNT_EXIST,null);
        }else {
            Person person;
            if (((PersonKey) testedPersonNode.get_data1()).getIdNumber().equals(personId)) {
                person = ((Person) testedPersonNode.get_value1());
            } else {
                person = ((Person) testedPersonNode.get_value2());
            }
            PCRKey tKey;
            try{
                tKey = new PCRKey(UUID.fromString(pcrId));
            }catch (Exception exception){
                return new PersonPCRResult(
                        ResponseType.PCR_DOESNT_EXIST,
                        person.getName() + " " + person.getSurname());
            }
            PCRData tData = new PCRData(tKey, null);
            BST23Node testNode = person.getTreeOfTests().find(tData);
            if (testNode == null){
                return new PersonPCRResult(
                        ResponseType.PCR_DOESNT_EXIST,
                        person.getName() + " " + person.getSurname());
            }else {
                String resultString = person.getName() + " " + person.getSurname() + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "." + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear();
                String res;
                if (((PCRKey) testNode.get_data1()).getPCRId().toString().equals(pcrId)) {
                    if (((PCR) testNode.get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += "\nKod testu: " + ((PCR) testNode.get_value1()).getPCRId() + "\nDatum a cas testu: "
                            + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) testNode.get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                            + ((PCR) testNode.get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) testNode.get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) testNode.get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: " + ((PCR) testNode.get_value1()).getDescription();
                    return new PersonPCRResult(
                            ResponseType.SUCCESS, resultString);
                } else {
                    if (((PCR) testNode.get_value2()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += "\nKod testu: " + ((PCR) testNode.get_value2()).getPCRId() + "\n Datum a cas testu: "
                            + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) testNode.get_value2()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                            + ((PCR) testNode.get_value2()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) testNode.get_value2()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) testNode.get_value2()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: " + ((PCR) testNode.get_value2()).getDescription();
                    return new PersonPCRResult(
                            ResponseType.SUCCESS, resultString);
                }
            }
        }
    }

    public PersonPCRResult findTestResultForPerson(Person person, String pcrId){
        PCRKey tKey;
        try{
            tKey = new PCRKey(UUID.fromString(pcrId));
        }catch (Exception exception){
            return new PersonPCRResult(
                    ResponseType.INCORRECT_PCR_FORMAT,
                    person.getName() + " " + person.getSurname());
        }
        PCRData tData = new PCRData(tKey, null);
        BST23Node testNode = person.getTreeOfTests().find(tData);
        if (testNode == null){
            return new PersonPCRResult(
                    ResponseType.PCR_DOESNT_EXIST,
                    person.getName() + " " + person.getSurname());
        }else {
            String resultString = person.getName() + " " + person.getSurname() + "\n" + person.getIdNumber() +
                    "\nNarodeny: " + person.getDateOfBirth().getDate() + "." + (person.getDateOfBirth().getMonth()+1)
                    + "." + person.getDateOfBirth().getYear();
            String res;
            if (((PCRKey) testNode.get_data1()).getPCRId().toString().equals(pcrId)) {
                if (((PCR) testNode.get_value1()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                resultString += "\nKod testu: " + ((PCR) testNode.get_value1()).getPCRId() + "\nDatum a cas testu: "
                        + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) testNode.get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) testNode.get_value1()).getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                        + ((PCR) testNode.get_value1()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) testNode.get_value1()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) testNode.get_value1()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: " + ((PCR) testNode.get_value1()).getDescription();
                return new PersonPCRResult(
                        ResponseType.SUCCESS, resultString);
            } else {
                if (((PCR) testNode.get_value2()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                resultString += "\nKod testu: " + ((PCR) testNode.get_value2()).getPCRId() + "\n Datum a cas testu: "
                        + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) testNode.get_value2()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) testNode.get_value2()).getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                        + ((PCR) testNode.get_value2()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) testNode.get_value2()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) testNode.get_value2()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: " + ((PCR) testNode.get_value2()).getDescription();
                return new PersonPCRResult(
                        ResponseType.SUCCESS, resultString);
            }
        }
    }

    public PersonPCRResult searchForTestsInWorkplace(int workplaceId, Date dateFrom, Date dateTo){
        String resultString = "";
        WorkplaceKey wKey = new WorkplaceKey(workplaceId);
        WorkplaceData wData = new WorkplaceData(wKey,null);
        BST23Node workplaceNode = treeOfWorkplace.find(wData);
        if (workplaceNode == null){
            return new PersonPCRResult(ResponseType.WORKPLACE_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyDate pKeyFrom = new PCRKeyDate(dateFrom);
            PCRWorkplaceData pDataFrom = new PCRWorkplaceData(pKeyFrom,null);
            PCRKeyDate pKeyTo = new PCRKeyDate(dateTo);
            PCRWorkplaceData pDataTo = new PCRWorkplaceData(pKeyTo,null);
            if (((WorkplaceKey) workplaceNode.get_data1()).getWorkplaceId() == workplaceId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Workplace) workplaceNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre pracovisko v zadanych datumoch.";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Workplace) workplaceNode.get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre pracovisko v zadanych datumoch.";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public PersonPCRResult searchSickPeopleInRegion(int regionId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        RegionKey rKey = new RegionKey(regionId);
        RegionData rData = new RegionData(rKey,null);
        BST23Node regionNode = treeOfRegions.find(rData);
        if (regionNode == null){
            return new PersonPCRResult(ResponseType.REGION_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
            PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,null);
            PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
            PCRRegionData pDataTo = new PCRRegionData(pKeyTo,null);
            if (((RegionKey) regionNode.get_data1()).getRegionId() == regionId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public PersonPCRResult getSortedRegionsBySickPeople(Date dateFrom, Date dateTo){
        if (dateFrom.compareTo(dateTo) > 0){
            return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
        }
        String resultString = "";
        int numberOfSickPeople = 0;
        BST23<RegionSickCountKey, Region> regionsSortedByNumberOfSickPeople = new BST23<>();
        NodeWithKey firstNode = treeOfRegions.getFirst();
        if (firstNode == null){
            //ziadne kraje tak vrati prazdne
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            numberOfSickPeople = getNumberOfSickInRegion(
                    ((Region) firstNode.getNode().get_value1()),
                    dateFrom,
                    dateTo);
            //vlozenie kraju do stromu kde sa usporiadava podla poctu chorych
            RegionSickCountKey key = new RegionSickCountKey(
                    numberOfSickPeople, ((RegionKey) firstNode.getKey()).getRegionId());
            Region value = ((Region) firstNode.getNode().get_value1());
            RegionSickCountData data = new RegionSickCountData(key,value);
            regionsSortedByNumberOfSickPeople.insert(data);
        }
        NodeWithKey nextNode = treeOfRegions.getNext(firstNode.getNode(), (RegionKey) firstNode.getKey());
        while (nextNode != null){
            if (((RegionKey) nextNode.getKey()).getRegionId() == ((RegionKey) nextNode.getNode().get_data1()).getRegionId()){
                numberOfSickPeople = getNumberOfSickInRegion(
                        ((Region) nextNode.getNode().get_value1()),
                        dateFrom,
                        dateTo);
                //vlozenie kraju do stromu kde sa usporiadava podla poctu chorych
                RegionSickCountKey key = new RegionSickCountKey(
                        numberOfSickPeople, ((RegionKey) nextNode.getKey()).getRegionId());
                Region value = ((Region) nextNode.getNode().get_value1());
                RegionSickCountData data = new RegionSickCountData(key,value);
                regionsSortedByNumberOfSickPeople.insert(data);
            }else {
                numberOfSickPeople = getNumberOfSickInRegion(
                        ((Region) nextNode.getNode().get_value2()),
                        dateFrom,
                        dateTo);
                //vlozenie kraju do stromu kde sa usporiadava podla poctu chorych
                RegionSickCountKey key = new RegionSickCountKey(
                        numberOfSickPeople, ((RegionKey) nextNode.getKey()).getRegionId());
                Region value = ((Region) nextNode.getNode().get_value2());
                RegionSickCountData data = new RegionSickCountData(key,value);
                regionsSortedByNumberOfSickPeople.insert(data);
            }
            nextNode = treeOfRegions.getNext(nextNode.getNode(), ((RegionKey) nextNode.getKey()));
        }
        //prejdenie stromu s krajmi zoradenymi podla poctu chorych
        NodeWithKey firstRegion = regionsSortedByNumberOfSickPeople.getFirst();
        int order = 0;
        if (firstRegion == null){
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            order++;
            resultString += getStringOfRegionsBySickCount(firstRegion, order);
        }
        NodeWithKey nextRegion = regionsSortedByNumberOfSickPeople.getNext(
                firstRegion.getNode(), ((RegionSickCountKey) firstRegion.getKey()));
        while (nextRegion != null){
            order++;
            resultString += getStringOfRegionsBySickCount(nextRegion, order);
            nextRegion = regionsSortedByNumberOfSickPeople.getNext(
                    nextRegion.getNode(), ((RegionSickCountKey) nextRegion.getKey()));
        }
        return new PersonPCRResult(ResponseType.SUCCESS, resultString);
    }

    private String getStringOfRegionsBySickCount(NodeWithKey pNodeWithKey, int nextValue){
        String resultString = "";
        if (((RegionSickCountKey) pNodeWithKey.getNode().get_data1()).equals(((RegionSickCountKey) pNodeWithKey.getKey()))){
            resultString += nextValue + ". " + ((Region) pNodeWithKey.getNode().get_value1()).getName() + "\n" +
                    "Pocet chorych = " +
                    ((RegionSickCountKey) pNodeWithKey.getNode().get_data1()).getNumberOfSickPeople() + "\n" +
                    "---------------------------------\n";
        }else {
            resultString += nextValue + ". " + ((Region) pNodeWithKey.getNode().get_value2()).getName() + "\n" +
                    "Pocet chorych = " +
                    ((RegionSickCountKey) pNodeWithKey.getNode().get_data2()).getNumberOfSickPeople() + "\n" +
                    "---------------------------------\n";
        }
        return resultString;
    }

    private int getNumberOfSickInRegion(Region region, Date dateFrom, Date dateTo){
        PCRKeyRegion pKeyFrom = new PCRKeyRegion(true,dateFrom);
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,null);
        PCRKeyRegion pKeyTo = new PCRKeyRegion(true,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,null);
        ArrayList<BST23Node> listOfFoundNodes = region.getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
        return listOfFoundNodes.size();
    }

    public PersonPCRResult getSortedDistrictsBySickPeople(Date dateFrom, Date dateTo){
        if (dateFrom.compareTo(dateTo) > 0){
            return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
        }
        String resultString = "";
        int numberOfSickPeople = 0;
        BST23<DistrictSickCountKey, District> districtSortedByNumberOfSickPeople = new BST23<>();
        NodeWithKey firstNode = treeOfDistricts.getFirst();
        if (firstNode == null){
            //ziadne okresy tak vrati prazdne
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            numberOfSickPeople = getNumberOfSickInDistrict(
                    ((District) firstNode.getNode().get_value1()),
                    dateFrom,
                    dateTo);
            //vlozenie okresu do stromu kde sa usporiadava podla poctu chorych
            DistrictSickCountKey key = new DistrictSickCountKey(
                    numberOfSickPeople, ((DistrictKey) firstNode.getKey()).getDistrictId());
            District value = ((District) firstNode.getNode().get_value1());
            DistrictSickCountData data = new DistrictSickCountData(key,value);
            districtSortedByNumberOfSickPeople.insert(data);
        }
        NodeWithKey nextNode = treeOfDistricts.getNext(firstNode.getNode(), (DistrictKey) firstNode.getKey());
        while (nextNode != null){
            if (((DistrictKey) nextNode.getKey()).getDistrictId() == ((DistrictKey) nextNode.getNode().get_data1()).getDistrictId()){
                numberOfSickPeople = getNumberOfSickInDistrict(
                        ((District) nextNode.getNode().get_value1()),
                        dateFrom,
                        dateTo);
                //vlozenie okresu do stromu kde sa usporiadava podla poctu chorych
                DistrictSickCountKey key = new DistrictSickCountKey(
                        numberOfSickPeople, ((DistrictKey) nextNode.getKey()).getDistrictId());
                District value = ((District) nextNode.getNode().get_value1());
                DistrictSickCountData data = new DistrictSickCountData(key,value);
                districtSortedByNumberOfSickPeople.insert(data);
            }else {
                numberOfSickPeople = getNumberOfSickInDistrict(
                        ((District) nextNode.getNode().get_value2()),
                        dateFrom,
                        dateTo);
                //vlozenie okresu do stromu kde sa usporiadava podla poctu chorych
                DistrictSickCountKey key = new DistrictSickCountKey(
                        numberOfSickPeople, ((DistrictKey) nextNode.getKey()).getDistrictId());
                District value = ((District) nextNode.getNode().get_value2());
                DistrictSickCountData data = new DistrictSickCountData(key,value);
                districtSortedByNumberOfSickPeople.insert(data);
            }
            nextNode = treeOfDistricts.getNext(nextNode.getNode(), ((DistrictKey) nextNode.getKey()));
        }
        //prejdenie stromu s okresmi zoradenymi podla poctu chorych
        NodeWithKey firstDistrict = districtSortedByNumberOfSickPeople.getFirst();
        int order = 0;
        if (firstDistrict == null){
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            order++;
            resultString += getStringOfDistrictsBySickCount(firstDistrict, order);
        }
        NodeWithKey nextDistrict = districtSortedByNumberOfSickPeople.getNext(
                firstDistrict.getNode(), ((DistrictSickCountKey) firstDistrict.getKey()));
        while (nextDistrict != null){
            order++;
            resultString += getStringOfDistrictsBySickCount(nextDistrict, order);
            nextDistrict = districtSortedByNumberOfSickPeople.getNext(
                    nextDistrict.getNode(), ((DistrictSickCountKey) nextDistrict.getKey()));
        }
        return new PersonPCRResult(ResponseType.SUCCESS, resultString);
    }

    private String getStringOfDistrictsBySickCount(NodeWithKey pNodeWithKey, int nextValue){
        String resultString = "";
        if (((DistrictSickCountKey) pNodeWithKey.getNode().get_data1()).equals(((DistrictSickCountKey) pNodeWithKey.getKey()))){
            resultString += nextValue + ". " + ((District) pNodeWithKey.getNode().get_value1()).getName() + "\n" +
                    "Pocet chorych = " +
                    ((DistrictSickCountKey) pNodeWithKey.getNode().get_data1()).getNumberOfSickPeople() + "\n" +
                    "---------------------------------\n";
        }else {
            resultString += nextValue + ". " + ((District) pNodeWithKey.getNode().get_value2()).getName() + "\n" +
                    "Pocet chorych = " +
                    ((DistrictSickCountKey) pNodeWithKey.getNode().get_data2()).getNumberOfSickPeople() + "\n" +
                    "---------------------------------\n";
        }
        return resultString;
    }

    private int getNumberOfSickInDistrict(District district, Date dateFrom, Date dateTo){
        PCRKeyDistrict pKeyFrom = new PCRKeyDistrict(true,dateFrom);
        PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,null);
        PCRKeyDistrict pKeyTo = new PCRKeyDistrict(true,dateTo);
        PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,null);
        ArrayList<BST23Node> listOfFoundNodes = district.getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
        return listOfFoundNodes.size();
    }

    public PersonPCRResult searchSickPeopleInDistrict(int districtId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        DistrictKey dKey = new DistrictKey(districtId);
        DistrictData dData = new DistrictData(dKey,null);
        BST23Node districtNode = treeOfDistricts.find(dData);
        if (districtNode == null){
            return new PersonPCRResult(ResponseType.DISTRICT_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyDistrict pKeyFrom = new PCRKeyDistrict(positivity,dateFrom);
            PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,null);
            PCRKeyDistrict pKeyTo = new PCRKeyDistrict(positivity,dateTo);
            PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,null);
            if (((DistrictKey) districtNode.get_data1()).getDistrictId() == districtId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value1()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value2()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public ResultWIthNumberOfResults searchTestsInDistrict(int districtId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        DistrictKey dKey = new DistrictKey(districtId);
        DistrictData dData = new DistrictData(dKey,null);
        BST23Node districtNode = treeOfDistricts.find(dData);
        if (districtNode == null){
            return new ResultWIthNumberOfResults(ResponseType.DISTRICT_DOESNT_EXIST,null, 0);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null, 0);
            }
            PCRKeyDistrict pKeyFrom = new PCRKeyDistrict(positivity,dateFrom);
            PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,null);
            PCRKeyDistrict pKeyTo = new PCRKeyDistrict(positivity,dateTo);
            PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,null);
            if (((DistrictKey) districtNode.get_data1()).getDistrictId() == districtId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value1()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += "" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy v zadanych datumoch.\n";
                }
                return new ResultWIthNumberOfResults(ResponseType.SUCCESS,resultString, listOfFoundNodes.size());
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value2()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += "" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy v zadanych datumoch.\n";
                }
                return new ResultWIthNumberOfResults(ResponseType.SUCCESS,resultString, listOfFoundNodes.size());
            }
        }
    }

    public ResultWIthNumberOfResults searchTestsInRegion(int regionId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        RegionKey rKey = new RegionKey(regionId);
        RegionData rData = new RegionData(rKey,null);
        BST23Node regionNode = treeOfRegions.find(rData);
        if (regionNode == null){
            return new ResultWIthNumberOfResults(ResponseType.REGION_DOESNT_EXIST,null,0);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null,0);
            }
            PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
            PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,null);
            PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
            PCRRegionData pDataTo = new PCRRegionData(pKeyTo,null);
            if (((RegionKey) regionNode.get_data1()).getRegionId() == regionId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy v zadanych datumoch.\n";
                }
                return new ResultWIthNumberOfResults(ResponseType.SUCCESS,resultString, listOfFoundNodes.size());
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy v zadanych datumoch.\n";
                }
                return new ResultWIthNumberOfResults(ResponseType.SUCCESS,resultString, listOfFoundNodes.size());
            }
        }
    }

    public ResultWIthNumberOfResults searchTestsInAllRegions(Date dateFrom, Date dateTo, boolean positivity){
        if (dateFrom.compareTo(dateTo) > 0){
            return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null, 0);
        }
        int numberOfResults = 0;
        String resultString = "";
        NodeWithKey firstNode = treeOfRegions.getFirst();
        if (firstNode == null){
            return new ResultWIthNumberOfResults(ResponseType.SUCCESS, resultString,0);
        }else {
            ResultWIthNumberOfResults result = getTestsStringForRegion(firstNode, dateFrom, dateTo, positivity);
            resultString += result.getResultInfo();
            numberOfResults += result.getNumberOfResults();
        }
        NodeWithKey nextNode = treeOfRegions.getNext(firstNode.getNode(), (RegionKey) firstNode.getKey());
        while (nextNode != null){
            ResultWIthNumberOfResults result = getTestsStringForRegion(nextNode, dateFrom, dateTo, positivity);
            resultString += result.getResultInfo();
            numberOfResults += result.getNumberOfResults();
            nextNode = treeOfRegions.getNext(nextNode.getNode(), (RegionKey) nextNode.getKey());
        }
        return new ResultWIthNumberOfResults(ResponseType.SUCCESS, resultString, numberOfResults);
    }

    public PersonPCRResult findPCRTestById(String PCRId){
        NodeWithKey firstNode = treeOfPeople.getFirst();
        PersonPCRResult result;
        if (firstNode == null){
            return new PersonPCRResult(ResponseType.SUCCESS, null);
        }else {
            result = findTestResultForPerson(((Person) firstNode.getNode().get_value1()), PCRId);
            if (result.getResponseType() == ResponseType.SUCCESS){
                return result;
            }
            if (result.getResponseType() == ResponseType.INCORRECT_PCR_FORMAT){
                return new PersonPCRResult(ResponseType.INCORRECT_PCR_FORMAT, null);
            }
        }
        NodeWithKey nextNode = treeOfPeople.getNext(firstNode.getNode(), ((PersonKey) firstNode.getKey()));
        while (nextNode != null){
            if (((PersonKey) nextNode.getKey()).getIdNumber().equals(((PersonKey) nextNode.getNode().get_data1()).getIdNumber())){
                result = findTestResultForPerson(((Person) nextNode.getNode().get_value1()), PCRId);
            }else {
                result = findTestResultForPerson(((Person) nextNode.getNode().get_value2()), PCRId);
            }
            if (result.getResponseType() == ResponseType.SUCCESS){
                return result;
            }
            if (result.getResponseType() == ResponseType.INCORRECT_PCR_FORMAT){
                return new PersonPCRResult(ResponseType.INCORRECT_PCR_FORMAT, null);
            }
            nextNode = treeOfPeople.getNext(nextNode.getNode(), ((PersonKey) nextNode.getKey()));
        }
        return new PersonPCRResult(ResponseType.PCR_DOESNT_EXIST, null);
    }

    public ResultWIthNumberOfResults searchSickPeopleInAllRegions(Date dateFrom, Date dateTo){
        if (dateFrom.compareTo(dateTo) > 0){
            return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null, 0);
        }
        int numberOfResults = 0;
        String resultString = "";
        NodeWithKey firstNode = treeOfRegions.getFirst();
        if (firstNode == null){
            return new ResultWIthNumberOfResults(ResponseType.SUCCESS, resultString, 0);
        }else {
            ResultWIthNumberOfResults result = getSickPeopleStringForRegion(firstNode, dateFrom, dateTo);
            resultString += result.getResultInfo();
            numberOfResults += result.getNumberOfResults();
        }
        NodeWithKey nextNode = treeOfRegions.getNext(firstNode.getNode(), (RegionKey) firstNode.getKey());
        while (nextNode != null){
            ResultWIthNumberOfResults result = getSickPeopleStringForRegion(nextNode, dateFrom, dateTo);
            resultString += result.getResultInfo();
            numberOfResults += result.getNumberOfResults();
            nextNode = treeOfRegions.getNext(nextNode.getNode(), (RegionKey) nextNode.getKey());
        }
        return new ResultWIthNumberOfResults(ResponseType.SUCCESS, resultString, numberOfResults);
    }

    private ResultWIthNumberOfResults getSickPeopleStringForRegion(NodeWithKey pNodeWithKey, Date dateFrom, Date dateTo){
        String resultString = "";
        PCRKeyRegion pKeyFrom = new PCRKeyRegion(true,dateFrom);
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,null);
        PCRKeyRegion pKeyTo = new PCRKeyRegion(true,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,null);
        if (((RegionKey) pNodeWithKey.getNode().get_data1()).getRegionId() == ((RegionKey) pNodeWithKey.getKey()).getRegionId()){
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                        + "\nDatum a cas testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null,resultString, listOfFoundNodes.size());
        }else {
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Chory na zaklade testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                        + "\nDatum a cas testu:"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null,resultString, listOfFoundNodes.size());
        }
    }

    private ResultWIthNumberOfResults getTestsStringForRegion(NodeWithKey pNodeWithKey, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,null);
        PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,null);
        if (((RegionKey) pNodeWithKey.getNode().get_data1()).getRegionId() == ((RegionKey) pNodeWithKey.getKey()).getRegionId()){
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                        + "\nDatum a cas testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null, resultString, listOfFoundNodes.size());
            //return resultString;
        }else {
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                        + "\nDatum a cas testu:"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                        + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null, resultString, listOfFoundNodes.size());
            //return resultString;
        }
    }

    public PersonPCRResult searchTestsForPerson(String personId){
        String resultString = "";
        PersonKey pKey = new PersonKey(personId);
        PersonData pData = new PersonData(pKey,null);
        BST23Node personNode = treeOfPeople.find(pData);
        if (personNode == null){
            return new PersonPCRResult(ResponseType.PERSON_DOESNT_EXIST,null);
        }else {
            if (((PersonKey) personNode.get_data1()).getIdNumber().equals(personId)){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Person) personNode.get_value1()).getTreeOfTestsByDate().inOrder();
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre osobu " +
                            ((Person) personNode.get_value1()).getName() + " " +
                            ((Person) personNode.get_value1()).getSurname() + ".";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Person) personNode.get_value2()).getTreeOfTests().inOrder();
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    if (((PCR) listOfFoundNodes.get(i).get_value1()).isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person = ((PCR) listOfFoundNodes.get(i).get_value1()).getPerson();
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + ((PCR) listOfFoundNodes.get(i).get_value1()).getPCRId()
                            + "\nDatum a cas testu:"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getDate() + "."
                            + (((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMonth()+1) + "."
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getYear() + " "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getHours() + ":"
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getWorkplaceId() + "\nKod okresu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDistrictId() + "\nKod kraja: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + ((PCR) listOfFoundNodes.get(i).get_value1()).getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre osobu " +
                            ((Person) personNode.get_value2()).getName() + " " +
                            ((Person) personNode.get_value2()).getSurname() + ".";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public boolean saveDataToFile() throws IOException {
        //ukladanie okresov
        FileWriter csvWriter = new FileWriter("okresy.csv");

        NodeWithKey nextDistrict = treeOfDistricts.getFirst();
        if (nextDistrict != null){
            csvWriter.append(""+ ((DistrictKey) nextDistrict.getKey()).getDistrictId());
            csvWriter.append(",");
            csvWriter.append(((District) nextDistrict.getNode().get_value1()).getName());
            csvWriter.append("\n");
            nextDistrict = treeOfDistricts.getNext(
                    nextDistrict.getNode(), ((DistrictKey) nextDistrict.getKey()));
        }
        while (nextDistrict != null){
            if (nextDistrict.getKey().compareTo(((DistrictKey) nextDistrict.getNode().get_data1())) == 0){
                csvWriter.append(""+ ((DistrictKey) nextDistrict.getKey()).getDistrictId());
                csvWriter.append(",");
                csvWriter.append(((District) nextDistrict.getNode().get_value1()).getName());
                csvWriter.append("\n");
            }else {
                csvWriter.append(""+ ((DistrictKey) nextDistrict.getKey()).getDistrictId());
                csvWriter.append(",");
                csvWriter.append(((District) nextDistrict.getNode().get_value2()).getName());
                csvWriter.append("\n");
            }
            nextDistrict = treeOfDistricts.getNext(
                    nextDistrict.getNode(), ((DistrictKey) nextDistrict.getKey()));
        }
        csvWriter.flush();
        csvWriter.close();

        //ukladanie krajov
        csvWriter = new FileWriter("kraje.csv");

        NodeWithKey nextRegion = treeOfRegions.getFirst();
        if (nextRegion != null){
            csvWriter.append(""+ ((RegionKey) nextRegion.getKey()).getRegionId());
            csvWriter.append(",");
            csvWriter.append(((Region) nextRegion.getNode().get_value1()).getName());
            csvWriter.append("\n");
            nextRegion = treeOfRegions.getNext(
                    nextRegion.getNode(), ((RegionKey) nextRegion.getKey()));
        }
        while (nextRegion != null){
            if (nextRegion.getKey().compareTo(((RegionKey) nextRegion.getNode().get_data1())) == 0){
                csvWriter.append(""+ ((RegionKey) nextRegion.getKey()).getRegionId());
                csvWriter.append(",");
                csvWriter.append(((Region) nextRegion.getNode().get_value1()).getName());
                csvWriter.append("\n");
            }else {
                csvWriter.append(""+ ((RegionKey) nextRegion.getKey()).getRegionId());
                csvWriter.append(",");
                csvWriter.append(((Region) nextRegion.getNode().get_value2()).getName());
                csvWriter.append("\n");
            }
            nextRegion = treeOfRegions.getNext(
                    nextRegion.getNode(), ((RegionKey) nextRegion.getKey()));
        }
        csvWriter.flush();
        csvWriter.close();

        //ukladanie pracovisk
        csvWriter = new FileWriter("pracoviska.csv");

        NodeWithKey nextWorkplace = treeOfWorkplace.getFirst();
        if (nextWorkplace != null){
            csvWriter.append(""+ ((WorkplaceKey) nextWorkplace.getKey()).getWorkplaceId());
            csvWriter.append("\n");
            nextWorkplace = treeOfWorkplace.getNext(
                    nextWorkplace.getNode(), ((WorkplaceKey) nextWorkplace.getKey()));
        }
        while (nextWorkplace != null){
            csvWriter.append(""+ ((WorkplaceKey) nextWorkplace.getKey()).getWorkplaceId());
            csvWriter.append("\n");
            nextWorkplace = treeOfWorkplace.getNext(
                    nextWorkplace.getNode(), ((WorkplaceKey) nextWorkplace.getKey()));
        }
        csvWriter.flush();
        csvWriter.close();

        //ukladanie osob
        csvWriter = new FileWriter("osoby.csv");
        FileWriter csvWriterTests = new FileWriter("testy.csv");

        NodeWithKey nextPerson = treeOfPeople.getFirst();
        if (nextPerson != null){
            csvWriter.append(((PersonKey) nextPerson.getKey()).getIdNumber());
            csvWriter.append(",");
            csvWriter.append(((Person) nextPerson.getNode().get_value1()).getName());
            csvWriter.append(",");
            csvWriter.append(((Person) nextPerson.getNode().get_value1()).getSurname());
            csvWriter.append(",");
            csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getDate());
            csvWriter.append(",");
            csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getMonth());
            csvWriter.append(",");
            csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getYear());
            csvWriter.append("\n");
            //ukladanie jeho testov
            writeTestsForPersonToFile(csvWriterTests,((Person) nextPerson.getNode().get_value1()));
            nextPerson = treeOfPeople.getNext(
                    nextPerson.getNode(), ((PersonKey) nextPerson.getKey()));
        }
        while (nextPerson != null){
            if (nextPerson.getKey().compareTo(((PersonKey) nextPerson.getNode().get_data1())) == 0){
                csvWriter.append(((PersonKey) nextPerson.getKey()).getIdNumber());
                csvWriter.append(",");
                csvWriter.append(((Person) nextPerson.getNode().get_value1()).getName());
                csvWriter.append(",");
                csvWriter.append(((Person) nextPerson.getNode().get_value1()).getSurname());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getDate());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getMonth());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value1()).getDateOfBirth().getYear());
                csvWriter.append("\n");
                writeTestsForPersonToFile(csvWriterTests,((Person) nextPerson.getNode().get_value1()));
            }else {
                csvWriter.append(((PersonKey) nextPerson.getKey()).getIdNumber());
                csvWriter.append(",");
                csvWriter.append(((Person) nextPerson.getNode().get_value2()).getName());
                csvWriter.append(",");
                csvWriter.append(((Person) nextPerson.getNode().get_value2()).getSurname());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value2()).getDateOfBirth().getDate());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value2()).getDateOfBirth().getMonth());
                csvWriter.append(",");
                csvWriter.append(""+((Person) nextPerson.getNode().get_value2()).getDateOfBirth().getYear());
                csvWriter.append("\n");
                writeTestsForPersonToFile(csvWriterTests,((Person) nextPerson.getNode().get_value2()));
            }
            nextPerson = treeOfPeople.getNext(
                    nextPerson.getNode(), ((PersonKey) nextPerson.getKey()));
        }
        csvWriterTests.flush();
        csvWriterTests.close();
        csvWriter.flush();
        csvWriter.close();
        return true;
    }

    public void writeTestsForPersonToFile(FileWriter pFileWriter, Person pPerson) throws IOException {
        NodeWithKey nextTest = pPerson.getTreeOfTests().getFirst();
        if (nextTest != null){
            pFileWriter.append(""+ ((PCRKey) nextTest.getKey()).getPCRId());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getDate());
            pFileWriter.append(",");
            pFileWriter.append(""+(((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getMonth()+1));
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getYear());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getHours());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getMinutes());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getSeconds());
            pFileWriter.append(",");
            pFileWriter.append(((PCR) nextTest.getNode().get_value1()).getPatientId());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getWorkplaceId());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDistrictId());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getRegionId());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).isResult());
            pFileWriter.append(",");
            pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDescription());
            pFileWriter.append("\n");
            nextTest = pPerson.getTreeOfTests().getNext(
                    nextTest.getNode(), ((PCRKey) nextTest.getKey()));
        }
        while (nextTest != null){
            if (nextTest.getKey().compareTo(((PCRKey) nextTest.getNode().get_data1())) == 0){
                pFileWriter.append(""+ ((PCRKey) nextTest.getKey()).getPCRId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getDate());
                pFileWriter.append(",");
                pFileWriter.append(""+(((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getMonth()+1));
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getYear());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getHours());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getMinutes());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDateAndTimeOfTest().getSeconds());
                pFileWriter.append(",");
                pFileWriter.append(((PCR) nextTest.getNode().get_value1()).getPatientId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getWorkplaceId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDistrictId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getRegionId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).isResult());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value1()).getDescription());
                pFileWriter.append("\n");
            }else {
                pFileWriter.append(""+ ((PCRKey) nextTest.getKey()).getPCRId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getDate());
                pFileWriter.append(",");
                pFileWriter.append(""+(((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getMonth()+1));
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getYear());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getHours());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getMinutes());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDateAndTimeOfTest().getSeconds());
                pFileWriter.append(",");
                pFileWriter.append(((PCR) nextTest.getNode().get_value2()).getPatientId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getWorkplaceId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDistrictId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getRegionId());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).isResult());
                pFileWriter.append(",");
                pFileWriter.append(""+((PCR) nextTest.getNode().get_value2()).getDescription());
                pFileWriter.append("\n");
            }
            nextTest = pPerson.getTreeOfTests().getNext(
                    nextTest.getNode(), ((PCRKey) nextTest.getKey()));
        }
    }

    public boolean loadDataFromFile() throws IOException {
        BST23<PersonKey, Person> newTreeOfPeople = new BST23<>();
        BST23<RegionKey, Region> newTreeOfRegions = new BST23<>();
        BST23<DistrictKey, District> newTreeOfDistricts = new BST23<>();
        BST23<WorkplaceKey, Workplace> newTreeOfWorkplace = new BST23<>();

        //nacitavanie krajov
        BufferedReader regionsReader = new BufferedReader(new FileReader("kraje.csv"));
        String row;
        while ((row = regionsReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0] != null && data[1] != null){
                RegionKey rKey = new RegionKey(Integer.parseInt(data[0]));
                Region rValue = new Region(Integer.parseInt(data[0]), data[1]);
                RegionData rData = new RegionData(rKey,rValue);
                newTreeOfRegions.insert(rData);
            }

        }
        regionsReader.close();

        //nacitavanie okresov
        BufferedReader districtsReader = new BufferedReader(new FileReader("okresy.csv"));
        while ((row = districtsReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0] != null && data[1] != null){
                DistrictKey dKey = new DistrictKey(Integer.parseInt(data[0]));
                District dValue = new District(Integer.parseInt(data[0]), data[1]);
                DistrictData dData = new DistrictData(dKey,dValue);
                newTreeOfDistricts.insert(dData);
            }

        }
        districtsReader.close();

        //nacitavanie pracovisk
        BufferedReader workplaceReader = new BufferedReader(new FileReader("pracoviska.csv"));
        while ((row = workplaceReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0] != null){
                WorkplaceKey wKey = new WorkplaceKey(Integer.parseInt(data[0]));
                Workplace wValue = new Workplace(Integer.parseInt(data[0]));
                WorkplaceData wData = new WorkplaceData(wKey,wValue);
                newTreeOfWorkplace.insert(wData);
            }

        }
        workplaceReader.close();

        //nacitavanie osob
        BufferedReader personReader = new BufferedReader(new FileReader("osoby.csv"));
        while ((row = personReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0] != null){
                PersonKey pKey = new PersonKey(data[0]);
                Person pValue = new Person(
                        data[1],
                        data[2],
                        Integer.parseInt(data[5]),
                        Integer.parseInt(data[4]),
                        Integer.parseInt(data[3]),
                        data[0]);
                PersonData pData = new PersonData(pKey,pValue);
                newTreeOfPeople.insert(pData);
            }
        }
        personReader.close();

        //priradenie novych stromov
        treeOfRegions = newTreeOfRegions;
        treeOfDistricts = newTreeOfDistricts;
        treeOfWorkplace = newTreeOfWorkplace;
        treeOfPeople = newTreeOfPeople;

        //nacitavanie testov
        BufferedReader testReader = new BufferedReader(new FileReader("testy.csv"));
        while ((row = testReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0] != null){
                boolean result = false;
                if (data[11] != null && data[11].equals("true")){
                    result = true;
                }
                insertPCRTest(
                        data[7],
                        Integer.parseInt(data[3]),
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[1]),
                        Integer.parseInt(data[4]),
                        Integer.parseInt(data[5]),
                        Integer.parseInt(data[6]),
                        Integer.parseInt(data[8]),
                        Integer.parseInt(data[9]),
                        Integer.parseInt(data[10]),
                        result,
                        data[12],
                        data[0]);
            }
        }
        testReader.close();


        return true;
    }
}
