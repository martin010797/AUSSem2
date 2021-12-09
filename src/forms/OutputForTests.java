package forms;

import Main_system.PCRSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OutputForTests {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;

    private JTextPane outputTextPane;
    private JPanel OutputForTestsPanel;
    private JButton goBackToMenuButton;

    public OutputForTests(menu pMenu, JFrame pFrame, PCRSystem pPcrSystem) {
        m = pMenu;
        frame = pFrame;
        pcrSystem = pPcrSystem;

        goBackToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputTextPane.setText("");
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    public JPanel getOutputForTestsPanel() {
        return OutputForTestsPanel;
    }

    public JTextPane getOutputTextPane() {
        return outputTextPane;
    }

    public void setTextForOutputPane(String text) {
        this.outputTextPane.setText(text);
    }
}
