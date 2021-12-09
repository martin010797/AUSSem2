package Models;

import java.util.UUID;

public class PCRKey implements Comparable<PCRKey>{
    private UUID PCRId;

    public PCRKey(UUID PCRId) {
        this.PCRId = PCRId;
    }

    public UUID getPCRId() {
        return PCRId;
    }

    public void setPCRId(UUID PCRId) {
        this.PCRId = PCRId;
    }

    @Override
    public int compareTo(PCRKey o) {
        //komparator opacne lebo aj v implementovani stromu som spravil omylom opacne
        if (PCRId.compareTo(o.PCRId) < 0){
            return 1;
        }else if (PCRId.compareTo(o.PCRId) > 0){
            return -1;
        }else {
            return 0;
        }
    }
}
