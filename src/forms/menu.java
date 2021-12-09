package forms;

import Main_system.PCRSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class menu {
    private PCRSystem pcrSystem;
    private PersonInsert personInsertForm;
    private PCRInsert PCRInsertForm;
    private SearchResultForPerson searchResultForPersonForm;
    private SearchTestForWorkplace_15 searchTestForWorkplace_15form;
    private SearchPositiveTestsForDistrict_4 searchPositiveTestsForDistrict_4form;
    private FindTestsForPatient_3 findTestsForPatient_3forml;
    private SearchForTestsInDistrict_5 searchForTestsInDistrict_5form;
    private SearchPositiveTestsForRegion_6 searchPositiveTestsForRegion_6form;
    private SearchForTestsInRegion_7 searchForTestsInRegion_7form;
    private SearchPositiveTests_8 searchPositiveTests_8form;
    private SearchTests_9 searchTests_9form;
    private SearchSickInDistrict_10 searchSickInDistrict_10form;
    private SearchSickInRegion_11 searchSickInRegion_11form;
    private SearchSickInAllRegions_12 searchSickInAllRegions_12form;
    private DistrictsOrderedBySickPeople_13 districtsOrderedBySickPeople_13form;
    private RegionsOrderedBySickPeople_14 regionsOrderedBySickPeople_14form;
    private FindPCRTest_16 findPCRTest_16form;
    private DeletePCRTest_18 deletePCRTest_18form;
    private DeletePerson_19 deletePerson_19form;

    private OutputForTests outputForTestsForm;

    private JFrame frame;
    private JButton Button1;
    private JPanel MenuPanel;
    private JButton Button2;
    private JButton Button4;
    private JButton Button15;
    private JButton Button17;
    private JButton Button3;
    private JButton Button5;
    private JButton Button6;
    private JButton Button7;
    private JButton Button8;
    private JButton Button9;
    private JButton Button10;
    private JButton Button11;
    private JButton Button12;
    private JButton Button13;
    private JButton Button14;
    private JButton Button16;
    private JButton Button18;
    private JButton Button19;
    private JButton loadDataButton;
    private JButton saveDataButton;

    public menu(JFrame pFrame, PCRSystem pPcrSystem) {
        pcrSystem = pPcrSystem;
        frame = pFrame;

        outputForTestsForm = new OutputForTests(this, frame, pcrSystem);

        personInsertForm =  new PersonInsert(this, frame, pcrSystem);
        PCRInsertForm = new PCRInsert(this, frame, pcrSystem);
        searchResultForPersonForm = new SearchResultForPerson(this, frame, pcrSystem);
        searchTestForWorkplace_15form = new SearchTestForWorkplace_15(this, frame, pcrSystem, outputForTestsForm);
        searchPositiveTestsForDistrict_4form = new SearchPositiveTestsForDistrict_4(
                this,
                frame,
                pcrSystem,
                outputForTestsForm);
        findTestsForPatient_3forml = new FindTestsForPatient_3(this,frame,pcrSystem,outputForTestsForm);
        searchForTestsInDistrict_5form = new SearchForTestsInDistrict_5(this,frame,pcrSystem,outputForTestsForm);
        searchPositiveTestsForRegion_6form = new SearchPositiveTestsForRegion_6(
                this,
                frame,
                pcrSystem,
                outputForTestsForm);
        searchForTestsInRegion_7form = new SearchForTestsInRegion_7(this,frame,pcrSystem,outputForTestsForm);
        searchPositiveTests_8form = new SearchPositiveTests_8(this,frame,pcrSystem,outputForTestsForm);
        searchTests_9form = new SearchTests_9(this,frame,pcrSystem,outputForTestsForm);
        searchSickInDistrict_10form = new SearchSickInDistrict_10(this,frame,pcrSystem,outputForTestsForm);
        searchSickInRegion_11form = new SearchSickInRegion_11(this,frame,pcrSystem,outputForTestsForm);
        searchSickInAllRegions_12form = new SearchSickInAllRegions_12(this,frame,pcrSystem,outputForTestsForm);
        districtsOrderedBySickPeople_13form = new DistrictsOrderedBySickPeople_13(
                this,
                frame,
                pcrSystem,
                outputForTestsForm);
        regionsOrderedBySickPeople_14form = new RegionsOrderedBySickPeople_14(
                this,
                frame,
                pcrSystem,
                outputForTestsForm);
        findPCRTest_16form = new FindPCRTest_16(this, frame, pcrSystem);
        deletePCRTest_18form = new DeletePCRTest_18(this, frame, pcrSystem);
        deletePerson_19form = new DeletePerson_19(this, frame, pcrSystem);

        //pcrSystem = new PCRSystem();
        Button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(PCRInsertForm.getPCRInsertPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchResultForPersonForm.getSearchResultForPersonPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchPositiveTestsForDistrict_4form.getSearchPositiveTestsForDistrictPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button15.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchTestForWorkplace_15form.getSearchTestForWorkplacePanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button17.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(personInsertForm.getPersonInsertPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(findTestsForPatient_3forml.getFindTestForPatientPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchForTestsInDistrict_5form.getSearchForTestsInDistrictPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchPositiveTestsForRegion_6form.getSearchPositiveTestsForRegionPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchForTestsInRegion_7form.getSearchForTestsInRegionPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchPositiveTests_8form.getSearchPositiveTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchTests_9form.getSearchTestsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchSickInDistrict_10form.getSearchSickInDistrictPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchSickInRegion_11form.getSearchSickInRegionPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button12.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(searchSickInAllRegions_12form.getSearchSickInAllRegionsPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button13.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(districtsOrderedBySickPeople_13form.getDistrictsOrderedBySickPeoplePanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button14.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(regionsOrderedBySickPeople_14form.getRegionsOrderedBySickPeoplePanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button16.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(findPCRTest_16form.getFindPCRTestPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button18.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(deletePCRTest_18form.getDeletePCRTestPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        Button19.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(deletePerson_19form.getDeletePersonPanel());
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            }
        });
        loadDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (pcrSystem.loadDataFromFile()){
                        JOptionPane.showMessageDialog(
                                null,
                                "Udaje boli nacitane.");
                    }else {
                        JOptionPane.showMessageDialog(
                                null,
                                "Udaje sa nepodarilo nacitat.");
                    }
                }catch (IOException exception){
                    JOptionPane.showMessageDialog(
                            null,
                            "Chyba pri nacitani dat.");
                }
            }
        });
        saveDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (pcrSystem.saveDataToFile()){
                        JOptionPane.showMessageDialog(
                                null,
                                "Udaje ulozene do suboru.");
                    }else {
                        JOptionPane.showMessageDialog(
                                null,
                                "Udaje sa neulozili spravne.");
                    }
                }catch (IOException exception){
                    JOptionPane.showMessageDialog(
                            null,
                            "Chyba pri praci so subormi.");
                }
            }
        });
    }

    public JPanel getMenuPanel() {
        return MenuPanel;
    }

    public void setMenuPanel(JPanel menuPanel) {
        MenuPanel = menuPanel;
    }

    public void setPcrSystem(PCRSystem pcrSystem) {
        this.pcrSystem = pcrSystem;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}

