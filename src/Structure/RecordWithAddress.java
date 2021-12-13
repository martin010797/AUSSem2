package Structure;

public class RecordWithAddress<T> {
    private T record;
    private int address;

    public RecordWithAddress(T record, int address) {
        this.record = record;
        this.address = address;
    }

    public T getRecord() {
        return record;
    }

    public void setRecord(T record) {
        this.record = record;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}
