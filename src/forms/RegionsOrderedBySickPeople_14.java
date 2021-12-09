package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

public class RegionsOrderedBySickPeople_14 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;
    private OutputForTests outputForTestsForm;

    private JTextField numberOfDaysTextField;
    private JButton listRegionsButton;
    private JButton goBackToMenuButton;
    private JTextField dayTextField;
    private JTextField monthTextField;
    private JTextField yearTextField;
    private JPanel regionsOrderedBySickPeoplePanel;

    public RegionsOrderedBySickPeople_14(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem, OutputForTests pOutput) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;
        outputForTestsForm = pOutput;

        listRegionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln vsetky polia.");
                }else {
                    Date dateTo = new Date(
                            Integer.parseInt(yearTextField.getText()),
                            (Integer.parseInt(monthTextField.getText())-1),
                            Integer.parseInt(dayTextField.getText()),
                            23,
                            59,
                            59);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateTo);
                    cal.add(Calendar.DATE, -Integer.parseInt(numberOfDaysTextField.getText()));
                    cal.add(Calendar.SECOND, 2);
                    Date dateFrom = cal.getTime();
                    PersonPCRResult response = pcrSystem.getSortedRegionsBySickPeople(dateFrom, dateTo);
                    switch (response.getResponseType()){
                        case LOWER_FROM_DATE:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Chyba s vyskladanim datumu.");
                            break;
                        }
                        case SUCCESS:{
                            if (response.getResultInfo().compareTo("") == 0){
                                outputForTestsForm.setTextForOutputPane(
                                        "Ziadne najdene kraje.");
                            }else {
                                String resultText = "Kraje zoradene podla poctu chorych pre datum  " +
                                        dayTextField.getText() + "." + monthTextField.getText() + "." +
                                        yearTextField.getText() + ":\n";
                                resultText += response.getResultInfo();
                                outputForTestsForm.setTextForOutputPane(resultText);
                            }
                            frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                            frame.pack();
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                            setFieldsEmpty();
                            break;
                        }
                    }
                }
            }
        });
        goBackToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFieldsEmpty();
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    public JPanel getRegionsOrderedBySickPeoplePanel() {
        return regionsOrderedBySickPeoplePanel;
    }

    private void setFieldsEmpty(){
        numberOfDaysTextField.setText("");
        dayTextField.setText("");
        monthTextField.setText("");
        yearTextField.setText("");
    }

    private boolean emptyFields(){
        if (numberOfDaysTextField.getText().equals("")){
            return true;
        }
        if (dayTextField.getText().equals("")){
            return true;
        }
        if (monthTextField.getText().equals("")){
            return true;
        }
        if (yearTextField.getText().equals("")){
            return true;
        }
        return false;
    }
}
