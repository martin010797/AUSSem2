package forms;

import Main_system.PCRSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonInsert {

    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextField name;
    private JTextField surname;
    private JTextField day;
    private JTextField month;
    private JTextField year;
    private JTextField personalId;
    private JButton Insert;
    private JPanel PersonInsertPanel;
    private JButton goToMenu;

    public PersonInsert(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        m = pMenu;
        frame = pFrame;
        pcrSystem = pPcrSystem;

        Insert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(null,"Vypln vsetky polia");
                }else {
                    try {
                        if (pcrSystem.insertPerson(
                                name.getText(),
                                surname.getText(),
                                Integer.parseInt(year.getText()),
                                Integer.parseInt(month.getText()),
                                Integer.parseInt(day.getText()),
                                personalId.getText())){
                            JOptionPane.showMessageDialog(null,"Osoba vlozena");
                            setFieldsEmpty();

                            frame.setContentPane(m.getMenuPanel());
                            frame.pack();
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                        }else {
                            //chyba ze uz existuje
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Osoba s rodnym cislom "+personalId.getText()+" uz existuje");
                        }
                    }catch (Exception exc){
                        JOptionPane.showMessageDialog(null,"Nastala chyba pri vkladani");
                    }

                }
            }
        });
        goToMenu.addActionListener(new ActionListener() {
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

    public JPanel getPersonInsertPanel() {
        return PersonInsertPanel;
    }

    private void setFieldsEmpty(){
        name.setText("");
        surname.setText("");
        day.setText("");
        month.setText("");
        year.setText("");
        personalId.setText("");
    }

    private boolean emptyFields(){
        if (name.getText().equals("")){
            return true;
        }
        if (surname.getText().equals("")){
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
        if (personalId.getText().equals("")){
            return true;
        }
        return false;
    }
}
