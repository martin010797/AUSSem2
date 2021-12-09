package Models;

import Structure.BST23;

public class District {
    private int districtId;
    private String name;
    private BST23<PCRKeyDistrict, PCR> treeOfTests = new BST23<>();

    public District(int districtId, String name) {
        this.districtId = districtId;
        this.name = name;
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

    public BST23<PCRKeyDistrict, PCR> getTreeOfTestedPeople() {
        return treeOfTests;
    }

    public void setTreeOfTestedPeople(BST23<PCRKeyDistrict, PCR> treeOfTests) {
        this.treeOfTests = treeOfTests;
    }
}
