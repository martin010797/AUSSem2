package forms;

import Main_system.PCRSystem;
import Main_system.ResponseType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DeletePCRTest_18 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextField PCRIdTextField;
    private JButton deleteTestButton;
    private JButton goBackToMenuButton;
    private JPanel deletePCRTestPanel;

    public DeletePCRTest_18(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;

        deleteTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (PCRIdTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln Id PCR testu.");
                }else {
                    ResponseType response = pcrSystem.deletePCRTest(PCRIdTextField.getText());
                    switch (response){
                        case INCORRECT_PCR_FORMAT:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Neplatne Id PCR testu.");
                            break;
                        }
                        case PERSON_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Ziadne osoby neexistuju.");
                            break;
                        }
                        case PCR_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Test so zadanym Id neexistuje.");
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
                                    "PCR test vymazany.");
                            break;
                        }
                    }
                }
            }
        });
        goBackToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PCRIdTextField.setText("");
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    public JPanel getDeletePCRTestPanel() {
        return deletePCRTestPanel;
    }
}
