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
    private JButton unsortedRegionButton;
    private JButton addressDistrictButton;
    private JButton unsortedDistrictButton;
    private JButton addressWorkplaceButton;
    private JButton unsortedWorkplaceButton;
    private JButton addresspersonButton;
    private JButton unsortedPersonButton;
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
        unsortedRegionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.getAllRecordsRegionUnsorted();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        unsortedDistrictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.getAllRecordsDistrictUnsorted();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        unsortedWorkplaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.getAllRecordsWorkplaceUnsorted();
                outputForTestsForm.setTextForOutputPane(result);
                frame.setContentPane(outputForTestsForm.getOutputForTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        unsortedPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = pcrSystem.getAllRecordsPersonUnsorted();
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
                String result = pcrSystem.listAllRegionAddressNodes();
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
                String result = pcrSystem.listAllDistrictsAddressNodes();
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
                String result = pcrSystem.listAllWorkplaceAddressNodes();
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
                String result = pcrSystem.listAllPeopleAddressNodes();
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
