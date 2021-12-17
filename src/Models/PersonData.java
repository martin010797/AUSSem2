package Models;

import Structure.BST23Node;

public class PersonData extends BST23Node<PersonKey, Person> {

    public PersonData(PersonKey key, Person person){
        super(key, person);
    }
}
