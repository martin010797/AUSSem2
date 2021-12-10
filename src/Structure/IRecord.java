package Structure;

public interface IRecord<T> {
    public  byte[] ToByteArray();
    public void FromByteArray(byte[] pArray);
    public int getSize();
    public boolean isValid();
    public void setValid(boolean pValid);
}
