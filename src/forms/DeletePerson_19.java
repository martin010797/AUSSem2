package forms;

import Main_system.PCRSystem;
import Main_system.ResponseType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DeletePerson_19 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextField personIdTextField;
    private JButton deletePersonButton;
    private JButton goBackToMenuButton;
    private JPanel deletePersonPanel;

    public DeletePerson_19(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;

        deletePersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (personIdTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln rodne cislo osoby.");
                }else {
                    ResponseType response = pcrSystem.deletePerson(personIdTextField.getText());
                    switch (response){
                        case PERSON_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba s danym rodnym cislom neexistuje.");
                            break;
                        }
                        case PROBLEM_WITH_DELETING:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Nastal problem pri mazani.");
                            break;
                        }
                        case SUCCESS:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba vymazana.");
                            break;
                        }
                    }
                }
            }
        });
        goBackToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                personIdTextField.setText("");
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    public JPanel getDeletePersonPanel() {
        return deletePersonPanel;
    }
}
