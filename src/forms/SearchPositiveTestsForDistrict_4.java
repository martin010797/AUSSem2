package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;
import Main_system.ResultWIthNumberOfResults;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class SearchPositiveTestsForDistrict_4 {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;
    private OutputForTests outputForTestsForm;

    private JTextField dayFromTextField;
    private JTextField monthFromTextField;
    private JTextField yearFromTextField;
    private JTextField dayToTextField;
    private JTextField monthToTextField;
    private JTextField yearToTextField;
    private JTextField districtIdTextField;
    private JButton searchForTestsButton;
    private JButton goBackToMenuButton;
    private JPanel SearchPositiveTestsForDistrictPanel;

    public SearchPositiveTestsForDistrict_4(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem, OutputForTests pOutput) {
        pcrSystem = pPcrSystem;
        m = pMenu;
        frame = pFrame;
        outputForTestsForm = pOutput;

        searchForTestsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln vsetky polia.");
                }else {
                    Date dateFrom = new Date(
                            Integer.parseInt(yearFromTextField.getText()),
                            (Integer.parseInt(monthFromTextField.getText())-1),
                            Integer.parseInt(dayFromTextField.getText()),
                            0,
                            0,
                            0);
                    Date dateTo = new Date(
                            Integer.parseInt(yearToTextField.getText()),
                            (Integer.parseInt(monthToTextField.getText())-1),
                            Integer.parseInt(dayToTextField.getText()),
                            23,
                            59,
                            59);
                    ResultWIthNumberOfResults response = pcrSystem.searchTestsInDistrict(
                            Integer.parseInt(districtIdTextField.getText()),
                            dateFrom,
                            dateTo,
                            true);
                    switch (response.getResponseType()){
                        case DISTRICT_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Okres so zadanym cislom neexistuje.");
                            break;
                        }
                        case LOWER_FROM_DATE:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Datum od musi byt mensi ako do.");
                            break;
                        }
                        case SUCCESS:{
                            outputForTestsForm.setTextForOutputPane(
                                    response.getResultInfo()+("Pocet testov = "+response.getNumberOfResults()));
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

    public JPanel getSearchPositiveTestsForDistrictPanel() {
        return SearchPositiveTestsForDistrictPanel;
    }

    private void setFieldsEmpty(){
        dayFromTextField.setText("");
        monthFromTextField.setText("");
        yearFromTextField.setText("");
        dayToTextField.setText("");
        monthToTextField.setText("");
        yearToTextField.setText("");
        districtIdTextField.setText("");
    }

    private boolean emptyFields(){
        if (dayFromTextField.getText().equals("")){
            return true;
        }
        if (monthFromTextField.getText().equals("")){
            return true;
        }
        if (yearFromTextField.getText().equals("")){
            return true;
        }
        if (dayToTextField.getText().equals("")){
            return true;
        }
        if (monthToTextField.getText().equals("")){
            return true;
        }
        if (yearToTextField.getText().equals("")){
            return true;
        }
        if (districtIdTextField.getText().equals("")){
            return true;
        }
        return false;
    }
}
