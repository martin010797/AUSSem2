package Tests;

public class TestKey implements Comparable<TestKey> {
    private int key;

    public TestKey(int pKey){
        key = pKey;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public int compareTo(TestKey o) {
        if (key < o.key){
            return 1;
        }else if (key > o.key){
            return -1;
        }else {
            return 0;
        }
    }
}
