package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FindPCRTest_16 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextField PCRIdTextField;
    private JButton findTestButton;
    private JButton goBackToMenuButton;
    private JPanel findPCRTestPanel;

    public FindPCRTest_16(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;

        findTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (PCRIdTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln kod testu.");
                }else {
                    PersonPCRResult response = pcrSystem.findPCRTestById(PCRIdTextField.getText());
                    switch (response.getResponseType()){
                        case INCORRECT_PCR_FORMAT:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Nespravny format kodu PCR testu.");
                            break;
                        }
                        case PCR_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Test s danym kodom neexistuje.");
                            break;
                        }
                        case SUCCESS:{
                            if (response.getResultInfo() == null){
                                JOptionPane.showMessageDialog(
                                        null,
                                        "Ziadne osoby sa v systeme nenachadzaju, tak neexistuju ani testy.");
                            }else {
                                JOptionPane.showMessageDialog(null, response.getResultInfo());
                                break;
                            }
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

    public JPanel getFindPCRTestPanel() {
        return findPCRTestPanel;
    }
}
