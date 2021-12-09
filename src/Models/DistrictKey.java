package Models;

import java.util.Date;

public class DistrictKey implements Comparable<DistrictKey>{
    private int districtId;

    public DistrictKey(int districtId) {
        this.districtId = districtId;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    @Override
    public int compareTo(DistrictKey o) {
        if (districtId < o.districtId){
            return 1;
        }else if (districtId > o.districtId ){
            return -1;
        }else {
            return 0;
        }
    }
}
