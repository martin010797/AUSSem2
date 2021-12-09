package forms;

import Main_system.PCRSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneratorOfData {
    private JTextField numberOfRegionsTextField;
    private JTextField numberOfDistrictsTextField;
    private JTextField numberOfWorkplacesTextField;
    private JTextField numberOfPeopleTextField;
    private JTextField numberOfTestsTextField;
    private JButton generateDataButton;
    private JButton skipGeneratorButton;
    private JPanel generatorPanel;

    private JFrame frame;
    private menu menuForm;

    public GeneratorOfData() {
        numberOfRegionsTextField.setText("10");
        numberOfDistrictsTextField.setText("200");
        numberOfWorkplacesTextField.setText("1000");

        frame = new JFrame("PCR system");
        frame.setContentPane(generatorPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        generateDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emptyFields()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vypln hodnoty pre generovanie.");
                }else {
                    menuForm = new menu(frame,
                            new PCRSystem(
                                    Integer.parseInt(numberOfRegionsTextField.getText()),
                                    Integer.parseInt(numberOfDistrictsTextField.getText()),
                                    Integer.parseInt(numberOfWorkplacesTextField.getText()),
                                    Integer.parseInt(numberOfPeopleTextField.getText()),
                                    Integer.parseInt(numberOfTestsTextField.getText())
                            )
                    );
                    frame.setContentPane(menuForm.getMenuPanel());
                    frame.pack();
                    frame.setVisible(true);
                    frame.setLocationRelativeTo(null);
                }
            }
        });
        skipGeneratorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuForm = new menu(frame, new PCRSystem());
                frame.setContentPane(menuForm.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    private boolean emptyFields(){
        if (numberOfRegionsTextField.getText().equals("")){
            return true;
        }
        if (numberOfDistrictsTextField.getText().equals("")){
            return true;
        }
        if (numberOfWorkplacesTextField.getText().equals("")){
            return true;
        }
        if (numberOfPeopleTextField.getText().equals("")){
            return true;
        }
        if (numberOfTestsTextField.getText().equals("")){
            return true;
        }
        return false;
    }
}
