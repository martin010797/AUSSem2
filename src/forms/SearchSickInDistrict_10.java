package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

public class SearchSickInDistrict_10 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;
    private OutputForTests outputForTestsForm;

    private JPanel searchSickInDistrictPanel;
    private JTextField districtIdTextField;
    private JTextField numberOfDaysTextField;
    private JButton searchPeopleButton;
    private JButton goBackToMenuButton;
    private JTextField dayTextField;
    private JTextField monthTextField;
    private JTextField yearTextField;

    public SearchSickInDistrict_10(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem, OutputForTests pOutput) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;
        outputForTestsForm = pOutput;

        searchPeopleButton.addActionListener(new ActionListener() {
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
                    PersonPCRResult response = pcrSystem.searchSickPeopleInDistrict(
                            Integer.parseInt(districtIdTextField.getText()),
                            dateFrom,
                            dateTo,
                            true);
                    switch (response.getResponseType()){
                        case DISTRICT_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Okres s danym kodom neexistuje.");
                            break;
                        }
                        case LOWER_FROM_DATE:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Chyba s vyskladanim datumu.");
                            break;
                        }
                        case SUCCESS:{
                            if (response.getResultInfo().compareTo("") == 0){
                                outputForTestsForm.setTextForOutputPane(
                                        "Ziadne najdene chore osoby pre zadany datum v danom okrese.");
                            }else {
                                String resultText = "Najdene chore osoby v okrese pre datum " + dayTextField.getText()
                                        + "." + monthTextField.getText() + "." + yearTextField.getText() + ":\n";
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

    public JPanel getSearchSickInDistrictPanel() {
        return searchSickInDistrictPanel;
    }

    private void setFieldsEmpty(){
        districtIdTextField.setText("");
        numberOfDaysTextField.setText("");
        dayTextField.setText("");
        monthTextField.setText("");
        yearTextField.setText("");
    }

    private boolean emptyFields(){
        if (districtIdTextField.getText().equals("")){
            return true;
        }
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
