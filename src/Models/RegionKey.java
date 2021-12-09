package Models;

public class RegionKey implements Comparable<RegionKey>{
    private int regionId;

    public RegionKey(int regionId) {
        this.regionId = regionId;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    @Override
    public int compareTo(RegionKey o) {
        if (regionId < o.regionId){
            return 1;
        }else if (regionId > o.regionId ){
            return -1;
        }else {
            return 0;
        }
    }
}
