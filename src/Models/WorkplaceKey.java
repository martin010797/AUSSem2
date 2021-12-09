package Models;

public class WorkplaceKey implements Comparable<WorkplaceKey>{
    private int workplaceId;

    public WorkplaceKey(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    public int getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    @Override
    public int compareTo(WorkplaceKey o) {
        if (workplaceId < o.workplaceId){
            return 1;
        }else if (workplaceId > o.workplaceId ){
            return -1;
        }else {
            return 0;
        }
    }
}
