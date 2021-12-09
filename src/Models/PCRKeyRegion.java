package Models;

import java.util.Date;

public class PCRKeyRegion  implements Comparable<PCRKeyRegion>{
    private boolean positivity;
    private Date date;

    public PCRKeyRegion(boolean positivity, Date date) {
        this.positivity = positivity;
        this.date = date;
    }

    public boolean isPositivity() {
        return positivity;
    }

    public void setPositivity(boolean positivity) {
        this.positivity = positivity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(PCRKeyRegion o) {
        //pozitivne testy budu nalavo a negativne napravo stromu
        if (positivity && !o.positivity){
            return 1;
        }else if (!positivity && o.positivity){
            return -1;
        }else if (date.compareTo(o.date) < 0){
            return 1;
        }else if (date.compareTo(o.date) > 0){
            return -1;
        }else {
            return 0;
        }
    }
}
