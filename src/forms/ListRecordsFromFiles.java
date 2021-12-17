package forms;

import Main_system.PCRSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ListRecordsFromFiles {
    private PCRSystem pcrSystem;
    private menu m;
    private JFrame frame;
    private OutputForTests outputForTestsForm;

    private JPanel listRecordsPanel;
    private JButton unsortedPCRButton;
    private JButton addressRegionButton;
    private JButton addressDistrictButton;
    private JButton addressWorkplaceButton;
    private JButton addresspersonButton;
    private JButton goBackToMenuButton;

    public ListRecordsFromFiles(PCRSystem pcrSystem, menu m, JFrame frame, OutputForTests outputForTestsForm) {
        this.pcrSystem = pcrSystem;
        this.m = m;
        this.frame = frame;
        this.outputForTestsForm = outputForTestsForm;

        unsortedPCRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.getAllRecordsPCRUnsorted();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        addressRegionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.listAllRegionNodes();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        addressDistrictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.listAllDistrictNodes();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        addressWorkplaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.listAllWorkplaceNodes();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        addresspersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.listAllPeopleNodes();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        goBackToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(m.getMenuPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    public JPanel getListRecordsPanel() {
        return listRecordsPanel;
    }
}
