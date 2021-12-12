package Models;

import Structure.BST23Node;

public class PersonData extends BST23Node<PersonKey, Address> {

    public PersonData(PersonKey key, Address address){
        super(key, address);
    }
}
