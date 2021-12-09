package Models;

public class DistrictSickCountKey implements Comparable<DistrictSickCountKey>{
    private int numberOfSickPeople;
    private int districtId;

    public DistrictSickCountKey(int numberOfSickPeople, int districtId) {
        this.numberOfSickPeople = numberOfSickPeople;
        this.districtId = districtId;
    }

    public int getNumberOfSickPeople() {
        return numberOfSickPeople;
    }

    public int getDistrictId() {
        return districtId;
    }

    @Override
    public int compareTo(DistrictSickCountKey o) {
        //vyssi pocet chorych znamena mensi kluc(aby nalavo daval najvyssie pocty chorych)
        if (numberOfSickPeople < o.numberOfSickPeople){
            return -1;
        }else if (numberOfSickPeople > o.numberOfSickPeople ){
            return 1;
        }else {
            if (districtId < o.districtId){
                return 1;
            }else if (districtId > o.districtId ){
                return -1;
            }else {
                return 0;
            }
        }
    }
}
