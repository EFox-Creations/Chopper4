/*
 * Created by JFormDesigner on Mon Sep 06 16:34:21 CDT 2021
 */

package me.vixen.chopperbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.lang3.ObjectUtils;

import java.awt.event.*;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.event.*;
import java.awt.*;
import java.io.FileNotFoundException;

/**
 * @author VixenKasai
 */
public class ChopperConsole {
    public JFrame frame;
    public ChopperConsole() {
        initComponents();
    }

    private void chopTokenSelected(ChangeEvent e) {
        if (rdbtnChopper.isSelected())
            rdbtnTestBot.setSelected(false);
    }

    private void testTokenSelected(ChangeEvent e) {
        if (rdbtnTestBot.isSelected())
            rdbtnChopper.setSelected(false);
    }

    private void btnLaunchMouseClicked(MouseEvent e) {
        if (!btnLaunch.isEnabled()) return;
        ChopBot.TOKEN tokenToUse = null;
        if (rdbtnChopper.isSelected()) tokenToUse = ChopBot.TOKEN.CHOPPER;
        else if (rdbtnTestBot.isSelected()) tokenToUse = ChopBot.TOKEN.TESTBOT;

        try {
            btnLaunch.setText("Loading...");
            Entry.createBot(tokenToUse);
            JOptionPane.showMessageDialog(frame, "Loaded", "Success", JOptionPane.INFORMATION_MESSAGE);
            btnLaunch.setEnabled(false);
        } catch (ExceptionInInitializerError e0) {
            JOptionPane.showMessageDialog(frame, "You may not create more than one bot", "Action Disallowed", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e1) {
            JOptionPane.showMessageDialog(frame, "No Token Provided", "Config Missing", JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e2) {
            JOptionPane.showMessageDialog(frame, "The bot's config failed to load.", "Config Missing", JOptionPane.ERROR_MESSAGE);
        } catch (LoginException e3) {
            JOptionPane.showMessageDialog(frame, "The bot failed to login.", "Login failed", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException e4) {
            JOptionPane.showMessageDialog(frame, "Failed to wait for JDA", "Waiting failed", JOptionPane.WARNING_MESSAGE);
        } finally {
            btnLaunch.setText("Launch");
        }
    }

    private void btnCloseMouseClicked(MouseEvent e) {
        String confirm_shutdown = JOptionPane.showInputDialog(frame, "Please type \"Chopper\" to shut down the bot", "Confirm Shutdown", JOptionPane.WARNING_MESSAGE);
        if (confirm_shutdown.equals("Chopper"))
            System.exit(Entry.shutdown());
    }

    public void addToLog(String str) {
        String text = txtLogList.getText();
        text += str + "\n";
        txtLogList.setText(text);
    }

    // TODO i want to be able to edit member profiles from the console
    private void initComponents() {
        FlatDarkLaf.setup();
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        pnlTabs = new JTabbedPane();
        pnlLunch = new JPanel();
        rdbtnChopper = new JRadioButton();
        rdbtnTestBot = new JRadioButton();
        lblChooseToken = new JLabel();
        btnLaunch = new JButton();
        scrollPane1 = new JScrollPane();
        txtLogList = new JTextPane();
        btnClose = new JButton();
        lblLog = new JLabel();

        //======== pnlTabs ========
        {

            //======== pnlLunch ========
            {

                //---- rdbtnChopper ----
                rdbtnChopper.setText("Chopper");
                rdbtnChopper.addChangeListener(e -> chopTokenSelected(e));

                //---- rdbtnTestBot ----
                rdbtnTestBot.setText("Test Bot");
                rdbtnTestBot.addChangeListener(e -> testTokenSelected(e));

                //---- lblChooseToken ----
                lblChooseToken.setText("Choose a launch token");

                //---- btnLaunch ----
                btnLaunch.setText("Launch");
                btnLaunch.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        btnLaunchMouseClicked(e);
                    }
                });

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(txtLogList);
                }

                //---- btnClose ----
                btnClose.setText("SHUTDOWN");
                btnClose.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        btnCloseMouseClicked(e);
                    }
                });

                //---- lblLog ----
                lblLog.setText("Recent log actions:");

                GroupLayout pnlLunchLayout = new GroupLayout(pnlLunch);
                pnlLunch.setLayout(pnlLunchLayout);
                pnlLunchLayout.setHorizontalGroup(
                    pnlLunchLayout.createParallelGroup()
                        .addGroup(pnlLunchLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(pnlLunchLayout.createParallelGroup()
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                                .addGroup(GroupLayout.Alignment.TRAILING, pnlLunchLayout.createSequentialGroup()
                                    .addGap(0, 487, Short.MAX_VALUE)
                                    .addComponent(btnClose))
                                .addGroup(pnlLunchLayout.createSequentialGroup()
                                    .addGroup(pnlLunchLayout.createParallelGroup()
                                        .addGroup(pnlLunchLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblChooseToken, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(rdbtnChopper, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(rdbtnTestBot, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnLaunch, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(lblLog))
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addContainerGap())
                );
                pnlLunchLayout.setVerticalGroup(
                    pnlLunchLayout.createParallelGroup()
                        .addGroup(pnlLunchLayout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addComponent(lblChooseToken)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rdbtnChopper)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rdbtnTestBot)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btnLaunch)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblLog)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnClose)
                            .addContainerGap())
                );
            }
            pnlTabs.addTab("Launch & Log", pnlLunch);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String confirm_shutdown = JOptionPane.showInputDialog(frame, "Please type \"Chopper\" to shut down the bot", "Confirm Shutdown", JOptionPane.WARNING_MESSAGE);
                if (confirm_shutdown.equals("Chopper"))
                    System.exit(Entry.shutdown());
            }
        });
        frame.setName("frmConsole");
        frame.setTitle("Chopper Console");
        frame.add(pnlTabs);
        frame.setSize(600,500);
        frame.setMinimumSize(new Dimension(600,500));
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        this.frame = frame;

    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JTabbedPane pnlTabs;
    private JPanel pnlLunch;
    private JRadioButton rdbtnChopper;
    private JRadioButton rdbtnTestBot;
    private JLabel lblChooseToken;
    private JButton btnLaunch;
    private JScrollPane scrollPane1;
    private JTextPane txtLogList;
    private JButton btnClose;
    private JLabel lblLog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
