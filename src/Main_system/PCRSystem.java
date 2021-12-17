package Main_system;

import Models.*;
import Structure.*;
import Structure.Old2_3Tree.BST23_old;
import Structure.Old2_3Tree.NodeWithKey_old;
import Tests.TestingData;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PCRSystem {
    private static final int UNDEFINED = -1;

    private BST23<PersonKey, Person> treeOfPeople;
    private BST23<DistrictKey, District> treeOfDistricts;
    private BST23<RegionKey, Region> treeOfRegions;
    private BST23<WorkplaceKey, Workplace> treeOfWorkplace;

    //unserted file kvoli vyhnutiu sa duplicite
    UnsortedFile<PCR> pcrUnsortedFile;

    public PCRSystem() {
        treeOfPeople = new BST23<PersonKey, Person>(
                "pcrSystemFiles/peopleAddress",
                PersonKey.class,
                Person.class);
        treeOfDistricts = new BST23<DistrictKey, District>(
                "pcrSystemFiles/districtAddress",
                DistrictKey.class,
                District.class);
        treeOfRegions = new BST23<RegionKey, Region>(
                "pcrSystemFiles/regionAddress",
                RegionKey.class,
                Region.class);
        treeOfWorkplace = new BST23<WorkplaceKey, Workplace>(
                "pcrSystemFiles/workplaceAddress",
                WorkplaceKey.class,
                Workplace.class);

        pcrUnsortedFile = new UnsortedFile<>("pcrSystemFiles/pcrUnsorted", PCR.class);
    }

    public PCRSystem(int pNumberOfRegions,
                     int pNumberOfDistricts,
                     int pNumberOfWorkplaces,
                     int pNumberOfPeople,
                     int pNumberOfTests){
        treeOfPeople = new BST23<PersonKey, Person>(
                "pcrSystemFiles/peopleAddress",
                PersonKey.class,
                Person.class);
        treeOfDistricts = new BST23<DistrictKey, District>(
                "pcrSystemFiles/districtAddress",
                DistrictKey.class,
                District.class);
        treeOfRegions = new BST23<RegionKey, Region>(
                "pcrSystemFiles/regionAddress",
                RegionKey.class,
                Region.class);
        treeOfWorkplace = new BST23<WorkplaceKey, Workplace>(
                "pcrSystemFiles/workplaceAddress",
                WorkplaceKey.class,
                Workplace.class);

        pcrUnsortedFile = new UnsortedFile<>("pcrSystemFiles/pcrUnsorted", PCR.class);
        generateDistrictsRegionsAndWorkplaces(
                pNumberOfRegions,
                pNumberOfDistricts,
                pNumberOfWorkplaces,
                pNumberOfPeople,
                pNumberOfTests);
    }

    public void endWorkWithTrees(){
        treeOfDistricts.endWorkWithFile();
        treeOfWorkplace.endWorkWithFile();
        treeOfRegions.endWorkWithFile();
        treeOfPeople.endWorkWithFile();
    }

    public void endWorkWithUnsortedFiles(){
        pcrUnsortedFile.endWorkWithFile();
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
        PersonData pData = new PersonData(pKey,new Person());
        BST23Node<PersonKey,Person> testedPersonNode = treeOfPeople.find(pData);
        if (testedPersonNode == null){
            //osoba sa v systeme nenachadza
            return new ResponseAndPCRTestId(ResponseType.PERSON_DOESNT_EXIST,null);
        }else {
            //vytvorenie testu
            Person person;
            PersonKey personKey;
            int personAddress;
            //District district;
            int districtAddress = -1;
            //Region region;
            int regionAddress = -1;
            //Workplace workplace;
            int workplaceAddress = -1;
            if (((PersonKey) testedPersonNode.get_data1()).getIdNumber().equals(personIdNumber)){
                person = testedPersonNode.get_value1();
                personKey = testedPersonNode.get_data1();
            }else{
                person = testedPersonNode.get_value2();
                personKey = testedPersonNode.get_data2();
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
                    personKey,
                    pTestId);
            //vlozenie testu do unsorted file
            int pcrAddress = pcrUnsortedFile.insert(testValue);
            Address pcrAddressRecord = new Address(pcrAddress);
            PCRKey testKey = new PCRKey(testValue.getPCRId());
            PCRData personTestData = new PCRData(testKey, pcrAddressRecord);
            //vlozenie testu do stromu testov v osobe
            if(!person.insertPCRForPerson(personTestData)){
                //osoba neexistuje tak sa vymaze nevlozeny pcr test z unsorted file
                pcrUnsortedFile.delete(pcrAddress);
                return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
            }
            //vlozenie testu do stromov v osobe podla datumu
            PCRKeyDate pKeyDate = new PCRKeyDate(testValue.getDateAndTimeOfTest());
            PCRWorkplaceData pDateData = new PCRWorkplaceData(pKeyDate,pcrAddressRecord);
            if (!person.insertPCRByDateForPerson(pDateData)){
                //nepodarilo sa vlozit tak vymaz z testov pre osobu podla id testu
                PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                person.deletePCRTest(deletedPersonTestData);
                person.getTreeOfTests().endWorkWithFile();
                //vymaze sa aj test z unsorted file
                pcrUnsortedFile.delete(pcrAddress);
                return new ResponseAndPCRTestId(ResponseType.PCR_EXISTS_FOR_THAT_TIME, testValue.getPCRId().toString());
            }
            //pre dany okres vlozi test
            DistrictKey dKey = new DistrictKey(districtId);
            DistrictData dData = new DistrictData(dKey,new District());
            BST23Node<DistrictKey,District> testedDistrictNode = treeOfDistricts.find(dData);
            if(testedDistrictNode == null){
                //vymaze sa test z osoby lebo sa nemoze vkladat do systemu pokial neexistuje okres
                PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                person.deletePCRTest(deletedPersonTestData);
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                //vymaze sa aj test z unsorted file
                pcrUnsortedFile.delete(pcrAddress);
                person.getTreeOfTests().endWorkWithFile();
                person.getTreeOfTestsByDate().endWorkWithFile();
                return new ResponseAndPCRTestId(ResponseType.DISTRICT_DOESNT_EXIST,testValue.getPCRId().toString());
            }else {
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRDistrictPositiveData districtTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId){
                    testValue.setDistrict(testedDistrictNode.get_data1());
                    if(!testedDistrictNode.get_value1().insertTest(districtTestData)){
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
                    }
                    testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                }else {
                    testValue.setDistrict(testedDistrictNode.get_data2());
                    if (!testedDistrictNode.get_value2().insertTest(districtTestData)){
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        return new ResponseAndPCRTestId(ResponseType.PCR_WITH_ID_EXISTS,testValue.getPCRId().toString());
                    }
                    testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                }
            }
            //pre dany kraj vlozi test
            RegionKey rKey = new RegionKey(regionId);
            RegionData rData = new RegionData(rKey,new Region());
            BST23Node<RegionKey,Region> testedRegionNode = treeOfRegions.find(rData);
            if(testedRegionNode == null){
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                    testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                }else {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                    testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                }
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                person.deletePCRTest(deletedPersonTestData);
                //vymaze sa aj test z unsorted file
                pcrUnsortedFile.delete(pcrAddress);
                person.getTreeOfTests().endWorkWithFile();
                person.getTreeOfTestsByDate().endWorkWithFile();
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                }else {
                    testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                }
                return new ResponseAndPCRTestId(ResponseType.REGION_DOESNT_EXIST, testValue.getPCRId().toString());
            }else {
                PCRKeyRegion pcrKeyRegion = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRRegionData regionTestData = new PCRRegionData(pcrKeyRegion,pcrAddressRecord);
                if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                    testValue.setRegion(testedRegionNode.get_data1());
                    if (!testedRegionNode.get_value1().insertTest(regionTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                        }else {
                            testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                        }
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_WITH_ID_EXISTS,
                                testValue.getPCRId().toString());
                    }
                    testedRegionNode.get_value1().getTreeOfTests().endWorkWithFile();
                }else {
                    testValue.setRegion(testedRegionNode.get_data2());
                    if (!testedRegionNode.get_value2().insertTest(regionTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                        }else {
                            testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                        }
                        //district.getTreeOfTestedPeople().endWorkWithFile();
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_WITH_ID_EXISTS,
                                testValue.getPCRId().toString());
                    }
                    testedRegionNode.get_value2().getTreeOfTests().endWorkWithFile();
                }
            }
            //pre dane pracovisko vlozi test
            WorkplaceKey wKey = new WorkplaceKey(workplaceId);
            WorkplaceData wData = new WorkplaceData(wKey,new Workplace());
            BST23Node<WorkplaceKey,Workplace> workplaceNode = treeOfWorkplace.find(wData);
            if(workplaceNode == null){
                PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                    testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                }else {
                    PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                    testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                }
                if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                    PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                    testedRegionNode.get_value1().deletePCRTest(deletedRegionTestData);
                }else {
                    PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                    testedRegionNode.get_value2().deletePCRTest(deletedRegionTestData);
                }
                PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                person.deletePCRTestByDate(deletedPersonDateTestData);
                PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                person.deletePCRTest(deletedPersonTestData);
                //vymaze sa aj test z unsorted file
                pcrUnsortedFile.delete(pcrAddress);
                person.getTreeOfTests().endWorkWithFile();
                person.getTreeOfTestsByDate().endWorkWithFile();
                if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                    testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                }else {
                    testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                }
                if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                    testedRegionNode.get_value1().getTreeOfTests().endWorkWithFile();
                }else {
                    testedRegionNode.get_value2().getTreeOfTests().endWorkWithFile();
                }
                return new ResponseAndPCRTestId(
                        ResponseType.WORKPLACE_DOESNT_EXIST,
                        testValue.getPCRId().toString());
            }else {
                PCRKeyDate testWorkplaceKey = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                PCRWorkplaceData workplaceTestData = new PCRWorkplaceData(testWorkplaceKey, pcrAddressRecord);
                if (((WorkplaceKey) workplaceNode.get_data1()).getWorkplaceId() == workplaceId){
                    testValue.setWorkplace(workplaceNode.get_data1());
                    if (!workplaceNode.get_value1().insertTest(workplaceTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                        }
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                            testedRegionNode.get_value1().deletePCRTest(deletedRegionTestData);
                        }else {
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                            testedRegionNode.get_value2().deletePCRTest(deletedRegionTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                        }else {
                            testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                        }
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            testedRegionNode.get_value1().getTreeOfTests().endWorkWithFile();
                        }else {
                            testedRegionNode.get_value2().getTreeOfTests().endWorkWithFile();
                        }
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_EXISTS_FOR_THAT_TIME,
                                testValue.getPCRId().toString());
                    }
                    workplaceNode.get_value1().getTreeOfTests().endWorkWithFile();
                }else {
                    testValue.setWorkplace(workplaceNode.get_data2());
                    if (!workplaceNode.get_value2().insertTest(workplaceTestData)){
                        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
                        //mazania kvoli tomu aby neostali data ked sa nemoze vkladat
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value1().deletePCRTest(deletedDistrictTestData);
                        }else {
                            PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, pcrAddressRecord);
                            testedDistrictNode.get_value2().deletePCRTest(deletedDistrictTestData);
                        }
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                            testedRegionNode.get_value1().deletePCRTest(deletedRegionTestData);
                        }else {
                            PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, pcrAddressRecord);
                            testedRegionNode.get_value2().deletePCRTest(deletedRegionTestData);
                        }
                        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
                        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,pcrAddressRecord);
                        person.deletePCRTestByDate(deletedPersonDateTestData);
                        PCRData deletedPersonTestData = new PCRData(testKey, pcrAddressRecord);
                        person.deletePCRTest(deletedPersonTestData);
                        //vymaze sa aj test z unsorted file
                        pcrUnsortedFile.delete(pcrAddress);
                        person.getTreeOfTests().endWorkWithFile();
                        person.getTreeOfTestsByDate().endWorkWithFile();
                        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == districtId) {
                            testedDistrictNode.get_value1().getTreeOfTestedPeople().endWorkWithFile();
                        }else {
                            testedDistrictNode.get_value2().getTreeOfTestedPeople().endWorkWithFile();
                        }
                        //district.getTreeOfTestedPeople().endWorkWithFile();
                        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == regionId){
                            testedRegionNode.get_value1().getTreeOfTests().endWorkWithFile();
                        }else {
                            testedRegionNode.get_value2().getTreeOfTests().endWorkWithFile();
                        }
                        //region.getTreeOfTests().endWorkWithFile();
                        return new ResponseAndPCRTestId(
                                ResponseType.PCR_EXISTS_FOR_THAT_TIME,
                                testValue.getPCRId().toString());
                    }
                    workplaceNode.get_value2().getTreeOfTests().endWorkWithFile();
                }
            }
            pcrUnsortedFile.updateOnAddress(testValue,pcrAddress);
            person.getTreeOfTests().endWorkWithFile();
            person.getTreeOfTestsByDate().endWorkWithFile();
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
        PersonData personData = new PersonData(personKey, new Person());
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
            Address tempPCRValue;
            if (((PCRKey) nextTestNode.getNode().get_data1()).compareTo(((PCRKey) nextTestNode.getKey())) == 0){
                tempPCRValue = ((Address) nextTestNode.getNode().get_value1());
            }else {
                tempPCRValue = ((Address) nextTestNode.getNode().get_value2());
            }
            BST23Node tempNode = new PCRData(tempPCRKey,tempPCRValue);
            listOfTests.add(new NodeWithKey(tempNode,tempPCRKey));
            nextTestNode = person.getTreeOfTests().getNext(nextTestNode.getNode(), ((PCRKey) nextTestNode.getKey()));
            while (nextTestNode != null){
                tempPCRKey = new PCRKey(((PCRKey) nextTestNode.getKey()).getPCRId());
                if (((PCRKey) nextTestNode.getNode().get_data1()).compareTo(((PCRKey) nextTestNode.getKey())) == 0){
                    tempPCRValue = ((Address) nextTestNode.getNode().get_value1());
                }else {
                    tempPCRValue = ((Address) nextTestNode.getNode().get_value2());
                }
                tempNode = new PCRData(tempPCRKey,tempPCRValue);
                listOfTests.add(new NodeWithKey(tempNode,tempPCRKey));
                nextTestNode = person.getTreeOfTests().getNext(nextTestNode.getNode(), ((PCRKey) nextTestNode.getKey()));
            }
            //mazanie testov
            for (int i = 0; i < listOfTests.size(); i++){
                NodeWithKey personNodeWithKey = new NodeWithKey(treeOfPeople.find(personData),personKey);
                int testResultAddress = -1;
                if (((PCRKey) listOfTests.get(i).getNode().get_data1()).compareTo((PCRKey) listOfTests.get(i).getKey()) == 0){
                    testResultAddress = ((Address) listOfTests.get(i).getNode().get_value1()).getAddressInUnsortedFile();
                }else {
                    testResultAddress = ((Address) listOfTests.get(i).getNode().get_value2()).getAddressInUnsortedFile();
                }
                if (!deleteTestInAllTrees(personNodeWithKey, listOfTests.get(i).getNode(), (PCRKey) listOfTests.get(i).getKey())){
                    return ResponseType.PROBLEM_WITH_DELETING;
                }else{
                    pcrUnsortedFile.delete(testResultAddress);
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
        PCRData tData = new PCRData(tKey, new Address(UNDEFINED));
        //najdenie testu
        NodeWithKey firstNode = treeOfPeople.getFirst();
        BST23Node<PCRKey,Address> testResult;
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
        int testResultAddress = -1;
        if (((PCRKey) testResult.get_data1()).compareTo(tKey) == 0){
            testResultAddress = testResult.get_value1().getAddressInUnsortedFile();
        }else {
            testResultAddress = testResult.get_value2().getAddressInUnsortedFile();
        }
        if(deleteTestInAllTrees(nextNode, testResult, tKey)){
            pcrUnsortedFile.delete(testResultAddress);
            return ResponseType.SUCCESS;
        }else {
            return ResponseType.PROBLEM_WITH_DELETING;
        }
    }

    private boolean deleteTestInAllTrees(NodeWithKey personNode, BST23Node testResult, PCRKey testKey){
        PCR testValue;
        int testAddress = -1;
        Person person;
        if (((PCRKey) testResult.get_data1()).compareTo(testKey) == 0){
            testAddress = ((Address) testResult.get_value1()).getAddressInUnsortedFile();
            testValue = pcrUnsortedFile.find(testAddress);
        }else {
            testAddress = ((Address) testResult.get_value2()).getAddressInUnsortedFile();
            testValue = pcrUnsortedFile.find(testAddress);
        }
        if (((PersonKey) personNode.getKey()).getIdNumber().equals(((PersonKey) personNode.getNode().get_data1()).getIdNumber())){
            person = ((Person) personNode.getNode().get_value1());
        }else {
            person = ((Person) personNode.getNode().get_value2());
        }

        //mazanie pre okres
        PCRKeyDistrict districtPCRKey = new PCRKeyDistrict(testValue.isResult(),testValue.getDateAndTimeOfTest());
        PCRDistrictPositiveData deletedDistrictTestData = new PCRDistrictPositiveData(districtPCRKey, new Address(testAddress));
        District district;
        BST23Node<DistrictKey,District> testedDistrictNode = treeOfDistricts.find(new DistrictData(testValue.getDistrict(),new District()));
        if (((DistrictKey) testedDistrictNode.get_data1()).getDistrictId() == testValue.getDistrict().getDistrictId()) {
            district = testedDistrictNode.get_value1();
        }else {
            district = testedDistrictNode.get_value2();
        }
        if(!district.deletePCRTest(deletedDistrictTestData)){
            return false;
        }
        district.getTreeOfTestedPeople().endWorkWithFile();

        //mazanie pre kraj
        PCRKeyRegion regionPCRKey = new PCRKeyRegion(testValue.isResult(),testValue.getDateAndTimeOfTest());
        PCRRegionData deletedRegionTestData = new PCRRegionData(regionPCRKey, new Address(testAddress));
        Region region;
        BST23Node<RegionKey,Region> testedRegionNode = treeOfRegions.find(new RegionData(testValue.getRegion(),new Region()));
        if (((RegionKey) testedRegionNode.get_data1()).getRegionId() == testValue.getRegion().getRegionId()){
            region = testedRegionNode.get_value1();
        }else {
            region = testedRegionNode.get_value2();
        }
        if (!region.deletePCRTest(deletedRegionTestData)){
            return false;
        }
        region.getTreeOfTests().endWorkWithFile();

        //mazanie pre pracovisko
        PCRKeyDate workplacePCRKey = new PCRKeyDate(testValue.getDateAndTimeOfTest());
        PCRWorkplaceData deletedWorkplaceData = new PCRWorkplaceData(workplacePCRKey, new Address(testAddress));
        Workplace workplace;
        BST23Node<WorkplaceKey,Workplace> workplaceNode = treeOfWorkplace.find(new WorkplaceData(testValue.getWorkplace(), new Workplace()));
        if (((WorkplaceKey) workplaceNode.get_data1()).getWorkplaceId() == testValue.getWorkplace().getWorkplaceId()){
            workplace = workplaceNode.get_value1();
        }else{
            workplace = workplaceNode.get_value2();
        }
        if (!workplace.deletePCRTest(deletedWorkplaceData)){
            return false;
        }
        workplace.getTreeOfTests().endWorkWithFile();

        //mazanie pre osobu
        PCRKeyDate pKeyDateDeleted = new PCRKeyDate(testValue.getDateAndTimeOfTest());
        PCRWorkplaceData deletedPersonDateTestData = new PCRWorkplaceData(pKeyDateDeleted,new Address(testAddress));
        if (!person.deletePCRTestByDate(deletedPersonDateTestData)){
            return false;
        }
        person.getTreeOfTestsByDate().endWorkWithFile();

        PCRData deletedPersonTestData = new PCRData(testKey, new Address(testAddress));
        if (!person.deletePCRTest(deletedPersonTestData)){
            return false;
        }
        person.getTreeOfTests().endWorkWithFile();
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
        PersonData pData = new PersonData(pKey,new Person());
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
            PCRData tData = new PCRData(tKey, new Address(UNDEFINED));
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
                    PCR pcr = pcrUnsortedFile.find(((Address) testNode.get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += "\nKod testu: " + pcr.getPCRId() + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: " + pcr.getDescription();
                    return new PersonPCRResult(
                            ResponseType.SUCCESS, resultString);
                } else {
                    PCR pcr = pcrUnsortedFile.find(((Address) testNode.get_value2()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += "\nKod testu: " + pcr.getPCRId() + "\n Datum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: " + pcr.getDescription();
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
        PCRData tData = new PCRData(tKey, new Address(UNDEFINED));
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
                PCR pcr = pcrUnsortedFile.find(((Address) testNode.get_value1()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                resultString += "\nKod testu: " + pcr.getPCRId() + "\nDatum a cas testu: "
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: " + pcr.getDescription();
                return new PersonPCRResult(
                        ResponseType.SUCCESS, resultString);
            } else {
                PCR pcr = pcrUnsortedFile.find(((Address) testNode.get_value2()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                resultString += "\nKod testu: " + pcr.getPCRId() + "\n Datum a cas testu: "
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes() + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: " + pcr.getDescription();
                return new PersonPCRResult(
                        ResponseType.SUCCESS, resultString);
            }
        }
    }

    public PersonPCRResult searchForTestsInWorkplace(int workplaceId, Date dateFrom, Date dateTo){
        String resultString = "";
        WorkplaceKey wKey = new WorkplaceKey(workplaceId);
        WorkplaceData wData = new WorkplaceData(wKey,new Workplace());
        BST23Node workplaceNode = treeOfWorkplace.find(wData);
        if (workplaceNode == null){
            return new PersonPCRResult(ResponseType.WORKPLACE_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyDate pKeyFrom = new PCRKeyDate(dateFrom);
            PCRWorkplaceData pDataFrom = new PCRWorkplaceData(pKeyFrom,new Address(UNDEFINED));
            PCRKeyDate pKeyTo = new PCRKeyDate(dateTo);
            PCRWorkplaceData pDataTo = new PCRWorkplaceData(pKeyTo,new Address(UNDEFINED));
            if (((WorkplaceKey) workplaceNode.get_data1()).getWorkplaceId() == workplaceId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Workplace) workplaceNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
        RegionData rData = new RegionData(rKey,new Region());
        BST23Node regionNode = treeOfRegions.find(rData);
        if (regionNode == null){
            return new PersonPCRResult(ResponseType.REGION_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
            PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,new Address(UNDEFINED));
            PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
            PCRRegionData pDataTo = new PCRRegionData(pKeyTo,new Address(UNDEFINED));
            if (((RegionKey) regionNode.get_data1()).getRegionId() == regionId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
        BST23_old<RegionSickCountKey, Region> regionsSortedByNumberOfSickPeople = new BST23_old<>();
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
        NodeWithKey_old firstRegion = regionsSortedByNumberOfSickPeople.getFirst();
        int order = 0;
        if (firstRegion == null){
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            order++;
            resultString += getStringOfRegionsBySickCount(firstRegion, order);
        }
        NodeWithKey_old nextRegion = regionsSortedByNumberOfSickPeople.getNext(
                firstRegion.getNode(), ((RegionSickCountKey) firstRegion.getKey()));
        while (nextRegion != null){
            order++;
            resultString += getStringOfRegionsBySickCount(nextRegion, order);
            nextRegion = regionsSortedByNumberOfSickPeople.getNext(
                    nextRegion.getNode(), ((RegionSickCountKey) nextRegion.getKey()));
        }
        return new PersonPCRResult(ResponseType.SUCCESS, resultString);
    }

    private String getStringOfRegionsBySickCount(NodeWithKey_old pNodeWithKey, int nextValue){
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
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,new Address(UNDEFINED));
        PCRKeyRegion pKeyTo = new PCRKeyRegion(true,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,new Address(UNDEFINED));
        ArrayList<BST23Node> listOfFoundNodes = region.getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
        return listOfFoundNodes.size();
    }

    public PersonPCRResult getSortedDistrictsBySickPeople(Date dateFrom, Date dateTo){
        if (dateFrom.compareTo(dateTo) > 0){
            return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
        }
        String resultString = "";
        int numberOfSickPeople = 0;
        BST23_old<DistrictSickCountKey, District> districtSortedByNumberOfSickPeople = new BST23_old<>();
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
        NodeWithKey_old firstDistrict = districtSortedByNumberOfSickPeople.getFirst();
        int order = 0;
        if (firstDistrict == null){
            return new PersonPCRResult(ResponseType.SUCCESS, resultString);
        }else {
            order++;
            resultString += getStringOfDistrictsBySickCount(firstDistrict, order);
        }
        NodeWithKey_old nextDistrict = districtSortedByNumberOfSickPeople.getNext(
                firstDistrict.getNode(), ((DistrictSickCountKey) firstDistrict.getKey()));
        while (nextDistrict != null){
            order++;
            resultString += getStringOfDistrictsBySickCount(nextDistrict, order);
            nextDistrict = districtSortedByNumberOfSickPeople.getNext(
                    nextDistrict.getNode(), ((DistrictSickCountKey) nextDistrict.getKey()));
        }
        return new PersonPCRResult(ResponseType.SUCCESS, resultString);
    }

    private String getStringOfDistrictsBySickCount(NodeWithKey_old pNodeWithKey, int nextValue){
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
        PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,new Address(UNDEFINED));
        PCRKeyDistrict pKeyTo = new PCRKeyDistrict(true,dateTo);
        PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,new Address(UNDEFINED));
        ArrayList<BST23Node> listOfFoundNodes = district.getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
        return listOfFoundNodes.size();
    }

    public PersonPCRResult searchSickPeopleInDistrict(int districtId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        DistrictKey dKey = new DistrictKey(districtId);
        DistrictData dData = new DistrictData(dKey,new District());
        BST23Node districtNode = treeOfDistricts.find(dData);
        if (districtNode == null){
            return new PersonPCRResult(ResponseType.DISTRICT_DOESNT_EXIST,null);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new PersonPCRResult(ResponseType.LOWER_FROM_DATE,null);
            }
            PCRKeyDistrict pKeyFrom = new PCRKeyDistrict(positivity,dateFrom);
            PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,new Address(UNDEFINED));
            PCRKeyDistrict pKeyTo = new PCRKeyDistrict(positivity,dateTo);
            PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,new Address(UNDEFINED));
            if (((DistrictKey) districtNode.get_data1()).getDistrictId() == districtId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value1()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value2()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Chory na zaklade testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
                            + "\n-----------------------------------------\n";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public ResultWIthNumberOfResults searchTestsInDistrict(int districtId, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        DistrictKey dKey = new DistrictKey(districtId);
        DistrictData dData = new DistrictData(dKey,new District());
        BST23Node districtNode = treeOfDistricts.find(dData);
        if (districtNode == null){
            return new ResultWIthNumberOfResults(ResponseType.DISTRICT_DOESNT_EXIST,null, 0);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null, 0);
            }
            PCRKeyDistrict pKeyFrom = new PCRKeyDistrict(positivity,dateFrom);
            PCRDistrictPositiveData pDataFrom = new PCRDistrictPositiveData(pKeyFrom,new Address(UNDEFINED));
            PCRKeyDistrict pKeyTo = new PCRKeyDistrict(positivity,dateTo);
            PCRDistrictPositiveData pDataTo = new PCRDistrictPositiveData(pKeyTo,new Address(UNDEFINED));
            if (((DistrictKey) districtNode.get_data1()).getDistrictId() == districtId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((District) districtNode.get_value1()).getTreeOfTestedPeople().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += "" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += "" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
        RegionData rData = new RegionData(rKey,new Region());
        BST23Node regionNode = treeOfRegions.find(rData);
        if (regionNode == null){
            return new ResultWIthNumberOfResults(ResponseType.REGION_DOESNT_EXIST,null,0);
        }else {
            if (dateFrom.compareTo(dateTo) > 0){
                return new ResultWIthNumberOfResults(ResponseType.LOWER_FROM_DATE,null,0);
            }
            PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
            PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,new Address(UNDEFINED));
            PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
            PCRRegionData pDataTo = new PCRRegionData(pKeyTo,new Address(UNDEFINED));
            if (((RegionKey) regionNode.get_data1()).getRegionId() == regionId){
                ArrayList<BST23Node> listOfFoundNodes;
                listOfFoundNodes = ((Region) regionNode.get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    Person person;
                    BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                    if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                        person = personNode.get_value1();
                    }else {
                        person = personNode.get_value2();
                    }
                    resultString += person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
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
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,new Address(UNDEFINED));
        PCRKeyRegion pKeyTo = new PCRKeyRegion(true,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,new Address(UNDEFINED));
        if (((RegionKey) pNodeWithKey.getNode().get_data1()).getRegionId() == ((RegionKey) pNodeWithKey.getKey()).getRegionId()){
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person;
                BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                    person = personNode.get_value1();
                }else {
                    person = personNode.get_value2();
                }
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Chory na zaklade testu: " + pcr.getPCRId()
                        + "\nDatum a cas testu: "
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + pcr.getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null,resultString, listOfFoundNodes.size());
        }else {
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person;
                BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                    person = personNode.get_value1();
                }else {
                    person = personNode.get_value2();
                }
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Chory na zaklade testu: " + pcr.getPCRId()
                        + "\nDatum a cas testu:"
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + pcr.getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null,resultString, listOfFoundNodes.size());
        }
    }

    private ResultWIthNumberOfResults getTestsStringForRegion(NodeWithKey pNodeWithKey, Date dateFrom, Date dateTo, boolean positivity){
        String resultString = "";
        PCRKeyRegion pKeyFrom = new PCRKeyRegion(positivity,dateFrom);
        PCRRegionData pDataFrom = new PCRRegionData(pKeyFrom,new Address(UNDEFINED));
        PCRKeyRegion pKeyTo = new PCRKeyRegion(positivity,dateTo);
        PCRRegionData pDataTo = new PCRRegionData(pKeyTo,new Address(UNDEFINED));
        if (((RegionKey) pNodeWithKey.getNode().get_data1()).getRegionId() == ((RegionKey) pNodeWithKey.getKey()).getRegionId()){
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value1()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person;
                BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                    person = personNode.get_value1();
                }else {
                    person = personNode.get_value2();
                }
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Kod testu: " + pcr.getPCRId()
                        + "\nDatum a cas testu: "
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + pcr.getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null, resultString, listOfFoundNodes.size());
        }else {
            ArrayList<BST23Node> listOfFoundNodes;
            listOfFoundNodes = ((Region) pNodeWithKey.getNode().get_value2()).getTreeOfTests().intervalSearch(pDataFrom,pDataTo);
            for (int i = 0; i < listOfFoundNodes.size(); i++){
                String res;
                PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                if (pcr.isResult()){
                    res = "POZITIVNY";
                }else {
                    res = "NEGATIVNY";
                }
                Person person;
                BST23Node<PersonKey,Person> personNode = treeOfPeople.find(new PersonData(pcr.getPerson(), new Person()));
                if (personNode.get_data1().getIdNumber().equals(pcr.getPerson().getIdNumber())){
                    person = personNode.get_value1();
                }else {
                    person = personNode.get_value2();
                }
                resultString += person.getName() + " " + person.getSurname()
                        + "\n" + person.getIdNumber() +
                        "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                        + (person.getDateOfBirth().getMonth()+1)
                        + "." + person.getDateOfBirth().getYear() + "\n"
                        + "Kod testu: " + pcr.getPCRId()
                        + "\nDatum a cas testu:"
                        + pcr.getDateAndTimeOfTest().getDate() + "."
                        + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                        + pcr.getDateAndTimeOfTest().getYear() + " "
                        + pcr.getDateAndTimeOfTest().getHours() + ":"
                        + pcr.getDateAndTimeOfTest().getMinutes()
                        + "\nKod pracoviska: "
                        + pcr.getWorkplaceId() + "\nKod okresu: "
                        + pcr.getDistrictId() + "\nKod kraja: "
                        + pcr.getRegionId() + "\nVysledok testu: "
                        + res + "\nPoznamka k testu: "
                        + pcr.getDescription()
                        + "\n-----------------------------------------\n";
            }
            return new ResultWIthNumberOfResults(null, resultString, listOfFoundNodes.size());
        }
    }

    public PersonPCRResult searchTestsForPerson(String personId){
        String resultString = "";
        PersonKey pKey = new PersonKey(personId);
        PersonData pData = new PersonData(pKey,new Person());
        BST23Node personNode = treeOfPeople.find(pData);
        if (personNode == null){
            return new PersonPCRResult(ResponseType.PERSON_DOESNT_EXIST,null);
        }else {
            if (((PersonKey) personNode.get_data1()).getIdNumber().equals(personId)){
                ArrayList<BST23Node> listOfFoundNodes;
                Person person = ((Person) personNode.get_value1());
                listOfFoundNodes = person.getTreeOfTestsByDate().inOrder();
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu: "
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre osobu " +
                            person.getName() + " " +
                            person.getSurname() + ".";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }else {
                ArrayList<BST23Node> listOfFoundNodes;
                Person person = ((Person) personNode.get_value2());
                listOfFoundNodes = person.getTreeOfTests().inOrder();
                for (int i = 0; i < listOfFoundNodes.size(); i++){
                    String res;
                    PCR pcr = pcrUnsortedFile.find(((Address) listOfFoundNodes.get(i).get_value1()).getAddressInUnsortedFile());
                    if (pcr.isResult()){
                        res = "POZITIVNY";
                    }else {
                        res = "NEGATIVNY";
                    }
                    resultString += (i+1) + ". \n" + person.getName() + " " + person.getSurname()
                            + "\n" + person.getIdNumber() +
                            "\nNarodeny: " + person.getDateOfBirth().getDate() + "."
                            + (person.getDateOfBirth().getMonth()+1)
                            + "." + person.getDateOfBirth().getYear() + "\n"
                            + "Kod testu: " + pcr.getPCRId()
                            + "\nDatum a cas testu:"
                            + pcr.getDateAndTimeOfTest().getDate() + "."
                            + (pcr.getDateAndTimeOfTest().getMonth()+1) + "."
                            + pcr.getDateAndTimeOfTest().getYear() + " "
                            + pcr.getDateAndTimeOfTest().getHours() + ":"
                            + pcr.getDateAndTimeOfTest().getMinutes()
                            + "\nKod pracoviska: "
                            + pcr.getWorkplaceId() + "\nKod okresu: "
                            + pcr.getDistrictId() + "\nKod kraja: "
                            + pcr.getRegionId() + "\nVysledok testu: "
                            + res + "\nPoznamka k testu: "
                            + pcr.getDescription()
                            + "\n-----------------------------------------\n";
                }
                if (listOfFoundNodes.size() == 0){
                    resultString = "Ziadne najdene testy pre osobu " +
                            person.getName() + " " +
                            person.getSurname() + ".";
                }
                return new PersonPCRResult(ResponseType.SUCCESS,resultString);
            }
        }
    }

    public String getAllRecordsPCRUnsorted(){
        String result = "";
        ArrayList<RecordWithAddress<PCR>> listOfRecords = pcrUnsortedFile.getAllRecordsFromFile();
        for (RecordWithAddress value: listOfRecords){
            result += "-------------------------------------------------------\n";
            result += "Adresa: " + value.getAddress() + ", validita: " + ((PCR) value.getRecord()).isValid() + "\n";
            result += "Id testu: " + ((PCR) value.getRecord()).getPCRId() + ", pozitivita: " + ((PCR) value.getRecord()).isResult() + "\n";
            result += "Datum: " + ((PCR) value.getRecord()).getDateAndTimeOfTest() + ", popis: " + ((PCR) value.getRecord()).getDescription() + "\n";
            result += "Id kraja: " + ((PCR) value.getRecord()).getRegionId() + ", Id okresu: "
                    + ((PCR) value.getRecord()).getDistrictId() + ", ID pracoviska: " + ((PCR) value.getRecord()).getWorkplaceId() + "\n";
            result += "Rodne cislo osoby: " + ((PCR) value.getRecord()).getPatientId() + "\n";
            result += "Velkost: " + ((PCR) value.getRecord()).getSize() +"\n";
            result += "-------------------------------------------------------\n";
        }
        return result;
    }

    public String listAllPeopleNodes(){
        String result = "";
        ArrayList<BST23Node> listOfNodes = treeOfPeople.getAllNodesFromFile();
        for (BST23Node value: listOfNodes){
            PersonKey key = ((PersonKey) value.get_data1());
            Person person = ((Person) value.get_value1());
            result += "-------------------------------------------------------\n";
            result += "Node validita: " + value.isValid() + ", Adresa: " + value.get_address() + "\n";
            result += "Kluc 1: " + key.getIdNumber() +", Validita: " + key.isValid() +"\n";
            result += "Rodne cislo: " + person.getIdNumber() + ", datum narodenia: " + person.getDateOfBirth() + "\n";
            result += "Meno: " + person.getName() + ", priezvisko: " + person.getSurname() + "\n";
            key = (PersonKey) value.get_data2();
            person = (Person) value.get_value2();
            result += "Kluc 2: " + key.getIdNumber() +", Validita: " + key.isValid() +"\n";
            result += "Rodne cislo: " + person.getIdNumber() + ", datum narodenia: " + person.getDateOfBirth() + "\n";
            result += "Meno: " + person.getName() + ", priezvisko: " + person.getSurname() + "\n";
            result += "-------------------------------------------------------\n";
        }
        return result;
    }

    public String listAllRegionNodes(){
        String result = "";
        ArrayList<BST23Node> listOfNodes = treeOfRegions.getAllNodesFromFile();
        for (BST23Node value: listOfNodes){
            RegionKey key = ((RegionKey) value.get_data1());
            Region region = ((Region) value.get_value1());
            result += "-------------------------------------------------------\n";
            result += "Node validita: " + value.isValid() + ", Adresa: " + value.get_address()+ "\n";
            result += "Kluc 1: " + key.getRegionId() +", Validita: " + key.isValid() +"\n";
            result += "Nazov kraja: " + region.getName() + " ,kod kraja: " + region.getRegionId() + "\n";
            key = (RegionKey) value.get_data2();
            region = (Region) value.get_value2();
            result += "Kluc 2: " + key.getRegionId() +", Validita: " + key.isValid() +"\n";
            result += "Nazov kraja: " + region.getName() + " ,kod kraja: " + region.getRegionId() + "\n";
            result += "-------------------------------------------------------\n";
        }
        return result;
    }

    public String listAllDistrictNodes(){
        String result = "";
        ArrayList<BST23Node> listOfNodes = treeOfDistricts.getAllNodesFromFile();
        for (BST23Node value: listOfNodes){
            DistrictKey key = ((DistrictKey) value.get_data1());
            District district = ((District) value.get_value1());
            result += "-------------------------------------------------------\n";
            result += "Node validita: " + value.isValid() + ", Adresa: " + value.get_address()+ "\n";
            result += "Kluc 1: " + key.getDistrictId() +", Validita: " + key.isValid() +"\n";
            result += "Nazov okresu: " + district.getName() + " ,kod okresu: " + district.getDistrictId() + "\n";
            key = (DistrictKey) value.get_data2();
            district = (District) value.get_value2();
            result += "Kluc 2: " + key.getDistrictId() +", Validita: " + key.isValid() +"\n";
            result += "Nazov okresu: " + district.getName() + " ,kod okresu: " + district.getDistrictId() + "\n";
            result += "-------------------------------------------------------\n";
        }
        return result;
    }

    public String listAllWorkplaceNodes(){
        String result = "";
        ArrayList<BST23Node> listOfNodes = treeOfWorkplace.getAllNodesFromFile();
        for (BST23Node value: listOfNodes){
            WorkplaceKey key = ((WorkplaceKey) value.get_data1());
            Workplace workplace = ((Workplace) value.get_value1());
            result += "-------------------------------------------------------\n";
            result += "Node validita: " + value.isValid() + ", Adresa: " + value.get_address()+ "\n";
            result += "Kluc 1: " + key.getWorkplaceId() +", Validita: " + key.isValid() +"\n";
            result += "Kluc z datoveho objektu: " + workplace.getWorkplaceId() + "\n";
            key = (WorkplaceKey) value.get_data2();
            workplace = (Workplace) value.get_value2();
            result += "Kluc 2: " + key.getWorkplaceId() +", Validita: " + key.isValid() +"\n";
            result += "Kluc z datoveho objektu: " + workplace.getWorkplaceId() + "\n";
            result += "-------------------------------------------------------\n";
        }
        return result;
    }
}
