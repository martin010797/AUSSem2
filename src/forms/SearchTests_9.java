package forms;

import Main_system.PCRSystem;
import Main_system.PersonPCRResult;
import Main_system.ResponseType;
import Main_system.ResultWIthNumberOfResults;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class SearchTests_9 {
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
    private JButton searchForTestsButton;
    private JButton goBackToMenuButton;
    private JPanel searchTestsPanel;

    public SearchTests_9(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem, OutputForTests pOutput) {
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
                    ResultWIthNumberOfResults responsePositive = pcrSystem.searchTestsInAllRegions(
                            dateFrom,
                            dateTo,
                            true);
                    ResultWIthNumberOfResults responseNegative = pcrSystem.searchTestsInAllRegions(
                            dateFrom,
                            dateTo,
                            false);
                    String result = "";
                    switch (responsePositive.getResponseType()){
                        case LOWER_FROM_DATE:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Datum od musi byt mensi ako do.");
                            break;
                        }
                        case SUCCESS:{
                            //pozitivne testy uklada do vysledku
                            result = responsePositive.getResultInfo();
                            if (responseNegative.getResponseType() == ResponseType.SUCCESS){
                                //ak naslo negativne tak ich tiez prida do vysledku
                                result += responseNegative.getResultInfo();
                            }else {
                                //chyba pri nacitani dat
                                JOptionPane.showMessageDialog(
                                        null,
                                        "Chyba pri nacitani dat.");
                                break;
                            }
                            if (result.equals("")){
                                result = "Ziadne najdene testy v zadanych datumoch.";
                            }
                            result+= "Pocet testov = "
                                    + (responseNegative.getNumberOfResults()
                                    + responsePositive.getNumberOfResults());
                            outputForTestsForm.setTextForOutputPane(result);
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

    public JPanel getSearchTestsPanel() {
        return searchTestsPanel;
    }

    private void setFieldsEmpty(){
        dayFromTextField.setText("");
        monthFromTextField.setText("");
        yearFromTextField.setText("");
        dayToTextField.setText("");
        monthToTextField.setText("");
        yearToTextField.setText("");
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
        return false;
    }
}
