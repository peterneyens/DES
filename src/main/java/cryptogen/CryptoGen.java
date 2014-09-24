/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen;

import cryptogen.stenagography.Stenagography;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.Clock;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author cornel
 */
public class CryptoGen extends JFrame implements ActionListener {

    private JTextField txtDesFile, txtDesKey, txtStenagoImage;
    private JButton btnDesStart, btnDesFile, btnStenagoStart, btnStenagoImage;
    private JTextArea txtStenagoText, txtConsole;

    public CryptoGen() {
        initGui();
    }

    public void initGui() {
        //configureren JFrame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("DES Program");

        //hoofdpaneel aanmaken
        JPanel pMain = new JPanel(new BorderLayout());
        this.getContentPane().add(pMain);

        //constraints aanmaken voor objecten te positioneren
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        //paneel DES aanmaken
        JPanel pDes = new JPanel(new GridBagLayout());
        pDes.setBorder(BorderFactory.createTitledBorder("DES"));

        //compontenten toevoegen aan DES paneel
        gbc.gridx = 0;
        gbc.gridy = 0;
        pDes.add(new JLabel("File:"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        pDes.add(new JLabel("Key:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtDesFile = new JTextField(10);
        pDes.add(txtDesFile, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtDesKey = new JTextField(10);
        pDes.add(txtDesKey, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        btnDesFile = new JButton("Select File");
        btnDesFile.addActionListener(this);
        pDes.add(btnDesFile, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        btnDesStart = new JButton("Start Encryption");
        btnDesStart.addActionListener(this);
        pDes.add(btnDesStart, gbc);

        //des paneel toevoegen aan frame
        pMain.add(pDes, BorderLayout.WEST);

        //paneel stenagografie aanmaken
        JPanel pStenago = new JPanel(new GridBagLayout());
        pStenago.setBorder(BorderFactory.createTitledBorder("Stenagography"));

        //compontenten toevoegen aan Stenago paneel
        gbc.gridx = 0;
        gbc.gridy = 0;
        pStenago.add(new JLabel("Text:"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        pStenago.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        txtStenagoText = new JTextArea(5, 25);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        txtStenagoText.setBorder(border);
        pStenago.add(txtStenagoText, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        txtStenagoImage = new JTextField(10);
        pStenago.add(txtStenagoImage, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        btnStenagoImage = new JButton("Select Image");
        btnStenagoImage.addActionListener(this);
        pStenago.add(btnStenagoImage, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        btnStenagoStart = new JButton("Start Encryption");
        btnStenagoStart.addActionListener(this);
        pStenago.add(btnStenagoStart, gbc);

        //stenago paneel toevoegen aan frame
        pMain.add(pStenago, BorderLayout.EAST);

        //console venster maken
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        txtConsole = new JTextArea(20, 20);
        JScrollPane scrollPane = new JScrollPane(txtConsole);
        pMain.add(scrollPane, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        CryptoGen cg = new CryptoGen();

        cg.pack();
        cg.setResizable(false);
        cg.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnDesFile) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                txtDesFile.setText(fch.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == btnStenagoImage) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP", "bmp", "bmp");
            fch.setFileFilter(filter);
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                txtStenagoImage.setText(fch.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == btnStenagoStart) {
            //set Console window
            Stenagography.console = txtConsole;
            Stenagography.DEBUG = true;

            txtConsole.append("Encoding started!" + "\n\r");
            
            //start encoding
            BufferedImage img = Stenagography.encode(txtStenagoImage.getText(), txtStenagoText.getText());

            //open dialog to save the file
            JFileChooser fch = new JFileChooser();

            int returnVal = fch.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    ImageIO.write(img, "bmp", fch.getSelectedFile());
                } catch (Exception ex) {
                    txtConsole.append("Fatal Error (main): " + ex.getMessage() + "\n\r");
                }
            }
        } else if (e.getSource() == btnDesStart) {
            System.out.println("Start encrypting file");
            txtConsole.append("Encrypting started!" + "\n\r");
            long before = System.currentTimeMillis();
            DesEncryption.encryptFile(txtDesFile.getText(), txtDesKey.getText());
            long after = System.currentTimeMillis();
            txtConsole.append("Time encrypting in milliseconds " + (after - before));
        }

    }
}
