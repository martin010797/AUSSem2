package Models;

import java.util.Date;

public class PCRKeyDistrict implements Comparable<PCRKeyDistrict>{
    private boolean positivity;
    private Date date;

    public PCRKeyDistrict(boolean positivity, Date date) {
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
    public int compareTo(PCRKeyDistrict o) {
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
