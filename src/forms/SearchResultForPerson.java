package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchResultForPerson {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JPanel SearchResultForPersonPanel;
    private JTextField personIdTextField;
    private JTextField PCRIdTextField;
    private JButton searchForResultButton;
    private JButton goBatckToMenuButton;

    public SearchResultForPerson(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;

        goBatckToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFieldsEmpty();
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        searchForResultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln rodne cislo a kod testu.");
                }else {
                    PersonPCRResult response = pcrSystem.findTestResultForPerson(
                            personIdTextField.getText(),
                            PCRIdTextField.getText());
                    switch (response.getResponseType()){
                        case PERSON_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba s danym rodnym cislom neexistuje.");
                            break;
                        }
                        case PCR_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Pre osobu " + response.getResultInfo()
                                            + " neexistuje PCR test s danym kodom.");
                            break;
                        }
                        case SUCCESS:{
                            JOptionPane.showMessageDialog(null, response.getResultInfo());
                            break;
                        }
                    }
                }
            }
        });
    }

    public JPanel getSearchResultForPersonPanel() {
        return SearchResultForPersonPanel;
    }

    private void setFieldsEmpty(){
        personIdTextField.setText("");
        PCRIdTextField.setText("");
    }

    private boolean emptyFields(){
        if (personIdTextField.getText().equals("")){
            return true;
        }
        if (PCRIdTextField.getText().equals("")){
            return true;
        }
        return false;
    }
}
