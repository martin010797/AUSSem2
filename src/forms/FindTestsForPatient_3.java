package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FindTestsForPatient_3 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;
    private OutputForTests outputForTestsForm;

    private JTextField personIdTextField;
    private JButton searchButton;
    private JButton goBackToMenuButton;
    private JPanel findTestForPatientPanel;

    public FindTestsForPatient_3(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem, OutputForTests pOutput) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;
        outputForTestsForm = pOutput;

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (personIdTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln rodne cislo osoby.");
                }else {
                    PersonPCRResult response = pcrSystem.searchTestsForPerson(personIdTextField.getText());
                    switch (response.getResponseType()){
                        case PERSON_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba s danym rodnym cislom v systeme neexistuje.");
                            break;
                        }
                        case SUCCESS:{
                            outputForTestsForm.setTextForOutputPane(response.getResultInfo());
                            frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                            frame.pack();
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                            personIdTextField.setText("");
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

    public JPanel getFindTestForPatientPanel() {
        return findTestForPatientPanel;
    }
}
