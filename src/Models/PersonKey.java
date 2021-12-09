package Models;

public class PersonKey implements Comparable<PersonKey> {
    private String idNumber;

    public PersonKey(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Override
    public int compareTo(PersonKey o) {
        //komparator opacne lebo aj v implementovani stromu som spravil omylom opacne
        if (idNumber.compareTo(o.idNumber) < 0){
            return 1;
        }else if (idNumber.compareTo(o.idNumber) > 0){
            return -1;
        }else {
            return 0;
        }
    }
}
