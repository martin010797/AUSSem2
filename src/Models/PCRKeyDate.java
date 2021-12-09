package Models;

import java.util.Date;
import java.util.UUID;

public class PCRKeyDate implements Comparable<PCRKeyDate>{
    private Date date;

    public PCRKeyDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(PCRKeyDate o) {
        //komparator opacne lebo aj v implementovani stromu som spravil omylom opacne
        if (date.compareTo(o.date) < 0){
            return 1;
        }else if (date.compareTo(o.date) > 0){
            return -1;
        }else {
            return 0;
        }
    }
}
