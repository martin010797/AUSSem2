package forms;

import Main_system.PCRSystem;
import Main_system.ResponseType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ThreadLocalRandom;

public class PCRInsert {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextField day;
    private JTextField month;
    private JTextField year;
    private JTextField minute;
    private JTextField hour;
    private JTextField workplaceId;
    private JTextField districtId;
    private JTextField regionId;
    private JTextField personId;
    private JComboBox result;
    private JTextField note;
    private JButton goToMenuButton;
    private JButton insertPCRButton;
    private JPanel PCRInsertPanel;

    public PCRInsert(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        m = pMenu;
        frame = pFrame;
        pcrSystem = pPcrSystem;

        //result = new JComboBox();
        result.addItem("Negativny");
        result.addItem("Pozitivny");
        goToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFieldsEmpty();
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        insertPCRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln vsetky polia. Poznamka je volitelna");
                }else {
                    boolean res;
                    if (result.getSelectedIndex() == 0){
                        res = false;
                    }else {
                        res = true;
                    }
                    PCRSystem.ResponseAndPCRTestId responseAndTest = pcrSystem.insertPCRTest(
                            personId.getText(),
                            Integer.parseInt(year.getText()),
                            Integer.parseInt(month.getText()),
                            Integer.parseInt(day.getText()),
                            Integer.parseInt(hour.getText()),
                            Integer.parseInt(minute.getText()),
                            ThreadLocalRandom.current().nextInt(1, 59 - 1),
                            Integer.parseInt(workplaceId.getText()),
                            Integer.parseInt(districtId.getText()),
                            Integer.parseInt(regionId.getText()),
                            res,
                            note.getText(),
                            null);
                    switch (responseAndTest.getResponse()){
                        case SUCCESS: {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "PCR test vlozeny do systemu.\n Kod testu: " + responseAndTest.getPCRTestId());
                            setFieldsEmpty();

                            frame.setContentPane(m.getMenuPanel());
                            frame.pack();
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                            break;
                        }
                        case PCR_WITH_ID_EXISTS: {
                            JOptionPane.showMessageDialog(null, "PCR test je duplicitny!");
                            break;
                        }
                        case PCR_EXISTS_FOR_THAT_TIME: {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "PCR test v danom case a na danom pracovisku uz existuje.");
                            break;
                        }
                        case PERSON_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba s danym rodnym cislom v systeme neexistuje.");
                            break;
                        }
                        case REGION_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Kraj s takym cislom neexistuje.");
                            break;
                        }
                        case DISTRICT_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Okres s takym cislom neexistuje.");
                            break;
                        }
                        case WORKPLACE_DOESNT_EXIST:{
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Pracovisko s takym cislom neexistuje.");
                            break;
                        }
                    }
                }
            }
        });
    }

    public JPanel getPCRInsertPanel() {
        return PCRInsertPanel;
    }

    private void setFieldsEmpty(){
        hour.setText("");
        minute.setText("");
        day.setText("");
        month.setText("");
        year.setText("");
        personId.setText("");
        workplaceId.setText("");
        districtId.setText("");
        regionId.setText("");
        note.setText("");
        result.setSelectedIndex(0);
    }

    private boolean emptyFields(){
        if (hour.getText().equals("")){
            return true;
        }
        if (minute.getText().equals("")){
            return true;
        }
        if (day.getText().equals("")){
            return true;
        }
        if (month.getText().equals("")){
            return true;
        }
        if (year.getText().equals("")){
            return true;
        }
        if (personId.getText().equals("")){
            return true;
        }
        if (workplaceId.getText().equals("")){
            return true;
        }
        if (districtId.getText().equals("")){
            return true;
        }
        if (regionId.getText().equals("")){
            return true;
        }
        return false;
    }
}
