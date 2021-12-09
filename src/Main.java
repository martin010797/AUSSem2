import javax.swing.*;

import Tests.GeneratorOfData;
import Tests.TestKey;
import Tests.TestingData;
import forms.menu;

public class Main {

    public static void main(String[] args) {
        GeneratorOfData generator = new GeneratorOfData();
        //generator.testInsert(100000);
        //generator.testDelete(100000);
        //generator.testRandomOperation(300000, 0.9);
        /*TestKey minKey = new TestKey(5000);
        TestKey maxKey = new TestKey(10000);
        TestingData minTest = new TestingData(minKey);
        TestingData maxTest = new TestingData(maxKey);
        generator.testIntervalSearch(minTest, maxTest, 100000);*/

        forms.GeneratorOfData system = new forms.GeneratorOfData();
    }
}
