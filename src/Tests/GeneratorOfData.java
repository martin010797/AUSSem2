package Tests;

import Structure.BST23;
import Structure.BST23Node;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratorOfData {

    public void testInsert(int numberOfValues){
        BST23<TestKey, TestingObjectValue> tree = new BST23<>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            values.add(i+1);
        }
        for (int i = 0; i < numberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            TestKey d = new TestKey(values.get(randomIndex));
            TestingObjectValue v = new TestingObjectValue("Martin", "NEG");
            //TestData d = new TestData(numberOfValues-i);
            values.remove(randomIndex);
            TestingData data = new TestingData(d,v);
            if(!tree.insert(data)){
                System.out.println("nevlozene");
            }else {
                System.out.println("Vlozene cislo " + data.get_data1().getKey());
            }
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " neulozene spravne");
            }
        }
        for (int i = 0; i < numberOfValues; i++){
            TestKey d = new TestKey(i+1);
            TestingData data = new TestingData(d);
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " nenajdene");
            }
            if (i == numberOfValues-1){
                System.out.println(" Vsetky vlozene prvky najdene");
            }
        }

        //overovanie vzdialenosti vsetkych listov od korena
        if (testDepth(tree.get_root())) {
            System.out.println("Hlbka vsetkych listov je rovnaka.");
        }else {
            System.out.println("Hlbka vsetkych listov nie je rovnaka!");
        }

    }

    public void testDelete(int numberOfValues){
        BST23<TestKey, TestingObjectValue> tree = new BST23<>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            values.add(i+1);
        }

        for (int i = 0; i < numberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            TestKey d = new TestKey(values.get(randomIndex));
            TestingObjectValue v = new TestingObjectValue("Martin", "NEG");
            values.remove(randomIndex);
            TestingData data = new TestingData(d,v);
            if(!tree.insert(data)){
                System.out.println("nevlozene");
            }else {
                System.out.println("Vlozene cislo " + data.get_data1().getKey());
            }
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " neulozene spravne");
            }
        }

        for (int i = 0; i < numberOfValues; i++){
            TestKey d = new TestKey(i+1);
            TestingObjectValue v = new TestingObjectValue("Martin", "NEG");
            TestingData data = new TestingData(d,v);
            if (tree.delete(data) == false){
                System.out.println(data.get_data1().getKey() + " nevymazane");
            }
            if (i == numberOfValues-1){
                System.out.println(" Vsetko vymazane");
            }
        }
    }

    public boolean testDepth(BST23Node root){
        int depth = 1;
        int totalDepth = 0;
        BST23Node prev = null;
        BST23Node temp = root;
        while (temp != null){
            if (!temp.isThreeNode()){
                if (temp.get_left1() == null && temp.get_right1() == null){
                    //ak je listom
                    if (totalDepth == 0) {
                        totalDepth = depth;
                    }
                    if (totalDepth != depth){
                        //listy nemaju rovnaku hlbku
                        return false;
                    }
                    if (totalDepth == depth){
                        prev = temp;
                        temp = prev.get_parent();
                        depth--;
                    }
                }else {
                    //ak nie je listom
                    if (prev == temp.get_left1()){
                        prev = temp;
                        temp = prev.get_right1();
                        depth++;
                    }else if(prev == temp.get_parent()){
                        prev = temp;
                        temp = prev.get_left1();
                        depth++;
                    }else if(prev == temp.get_right1()){
                        prev = temp;
                        temp = prev.get_parent();
                        depth--;
                    }
                }
            }else {
                if (temp.get_left1() == null &&
                        temp.get_right1() == null &&
                        temp.get_left2() == null &&
                        temp.get_right2() == null){
                    //je listom
                    if (totalDepth == 0) {
                        totalDepth = depth;
                    }
                    if (totalDepth != depth){
                        //listy nemaju rovnaku hlbku
                        return false;
                    }
                    if (totalDepth == depth){
                        prev = temp;
                        temp = prev.get_parent();
                        depth--;
                    }
                }else {
                    //nie je listom
                    if (prev == temp.get_parent()){
                        prev = temp;
                        temp = prev.get_left1();
                        depth++;
                    }else if (prev == temp.get_left1()){
                        prev = temp;
                        temp = prev.get_right1();
                        depth++;
                    }else if (prev == temp.get_right1()){
                        prev = temp;
                        temp = prev.get_right2();
                        depth++;
                    }else if (prev == temp.get_right2()){
                        prev = temp;
                        temp = prev.get_parent();
                        depth--;
                    }
                }
            }
        }
        return true;
    }

    public void testRandomOperation(int numberOfValues, double probabilityOfInsert){
        BST23<TestKey, TestingObjectValue> tree = new BST23<>();
        ArrayList<Integer> valuesForInsert = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            valuesForInsert.add(i+1);
        }
        ArrayList<Integer> valuesInserted = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            double typeOfOperation = Math.random();
            System.out.println(i + ". operacia: " + typeOfOperation);
            if (typeOfOperation < probabilityOfInsert){
                //insert
                int randomIndex;
                if (valuesForInsert.size() == 1){
                    randomIndex = 0;
                }else {
                    randomIndex = ThreadLocalRandom.current().nextInt(0, valuesForInsert.size() - 1);
                }
                Integer value = valuesForInsert.get(randomIndex);
                TestKey d = new TestKey(value);
                String name = "Martin"+value;
                TestingObjectValue v = new TestingObjectValue(name, "NEG");
                valuesForInsert.remove(randomIndex);
                TestingData data = new TestingData(d,v);
                if(!tree.insert(data)){
                    System.out.println("cislo " + value + " nevlozene");
                }else {
                    System.out.println("Vlozene cislo " + value);
                    valuesInserted.add(value);
                }
                if (tree.find(data) == null){
                    System.out.println(value + " neulozene spravne");
                }
            }else {
                //delete
                if (tree.get_root() == null){
                    System.out.println("Nie je co mazat");
                }else {
                    int randomIndex;
                    if (valuesInserted.size() == 1){
                        randomIndex = 0;
                    }else {
                        randomIndex = ThreadLocalRandom.current().nextInt(0, valuesInserted.size() - 1);
                    }
                    Integer value = valuesInserted.get(randomIndex);
                    TestKey d = new TestKey(value);
                    String name = "Martin"+value;
                    TestingObjectValue v = new TestingObjectValue(name, "NEG");
                    TestingData data = new TestingData(d,v);
                    System.out.println("pojde sa mazat " + value);
                    if (!tree.delete(data)){
                        System.out.println(value + " nevymazane");
                    }else {
                        System.out.println("Vymazane cislo " + value);
                        valuesInserted.remove(randomIndex);
                    }
                }
            }

        }
        if (testDepth(tree.get_root())) {
            System.out.println("Po testovani operacii je hlbka vsetkych listov rovnaka.");
        }else {
            System.out.println("Po testovani operacii nie je hlbka vsetkych listov rovnaka!");
        }
        int notFound = 0;
        for (Integer value: valuesInserted) {
            TestKey d = new TestKey(value);
            TestingData data = new TestingData(d);
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " nenajdene");
                notFound++;
            }
        }
        System.out.println("Pocet nenajdenych prvkov po operaciach: " + notFound);
    }

    public void testIntervalSearch(TestingData min, TestingData max, int numberOfValues){
        BST23<TestKey, TestingObjectValue> tree = new BST23<>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < numberOfValues; i++){
            values.add(i+1);
        }
        for (int i = 0; i < numberOfValues; i++){
            int randomIndex;
            if (values.size() == 1){
                randomIndex = 0;
            }else {
                randomIndex = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
            }
            TestKey d = new TestKey(values.get(randomIndex));
            TestingObjectValue v = new TestingObjectValue("Martin", "NEG");
            //TestData d = new TestData(numberOfValues-i);
            values.remove(randomIndex);
            TestingData data = new TestingData(d,v);
            if(!tree.insert(data)){
                System.out.println("nevlozene");
            }else {
                System.out.println("Vlozene cislo " + data.get_data1().getKey());
            }
            if (tree.find(data) == null){
                System.out.println(data.get_data1().getKey() + " neulozene spravne");
            }
        }

        ArrayList<BST23Node> listOfFoundNodes = tree.intervalSearch(min,max);
        System.out.println("Interval od " + min.get_data1().getKey() + " do "+ max.get_data1().getKey());
        for (int i = 0; i < listOfFoundNodes.size(); i++){
            System.out.println(((TestKey)listOfFoundNodes.get(i).get_data1()).getKey());
        }
    }
}
