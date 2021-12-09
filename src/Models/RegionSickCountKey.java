package Models;

public class RegionSickCountKey implements  Comparable<RegionSickCountKey>{
    private int numberOfSickPeople;
    private int regionId;

    public RegionSickCountKey(int numberOfSickPeople, int regionId) {
        this.numberOfSickPeople = numberOfSickPeople;
        this.regionId = regionId;
    }

    public int getNumberOfSickPeople() {
        return numberOfSickPeople;
    }

    public int getRegionId() {
        return regionId;
    }

    @Override
    public int compareTo(RegionSickCountKey o) {
        //vyssi pocet chorych znamena mensi kluc(aby nalavo daval najvyssie pocty chorych)
        if (numberOfSickPeople < o.numberOfSickPeople){
            return -1;
        }else if (numberOfSickPeople > o.numberOfSickPeople ){
            return 1;
        }else {
            if (regionId < o.regionId){
                return 1;
            }else if (regionId > o.regionId ){
                return -1;
            }else {
                return 0;
            }
        }
    }
}
