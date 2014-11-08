/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen;

import cryptogen.steganography.Steganography;
import cryptogen.des.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

    private JTextField txtDesFile, txtDesKey, txtSteganoImage;
    private JButton btnDesEncode, btnDesDecode, btnDesFile,
                    btnSteganoEncode, btnSteganoDecode, btnSteganoImage;
    private JTextArea txtSteganoText, txtConsole;

    //private DesService des = new SyncDesService();
    //private DesService des = new AsyncDesService();
    private DesService des = new AkkaDesService();
    //private DesService des = new DistributedDesService();
    
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
        gbc.gridx = 1;
        gbc.gridy = 3;
        btnDesDecode = new JButton("Start Decryption");
        btnDesDecode.addActionListener(this);
        pDes.add(btnDesDecode, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        btnDesEncode = new JButton("Start Encryption");
        btnDesEncode.addActionListener(this);
        pDes.add(btnDesEncode, gbc);

        //des paneel toevoegen aan frame
        pMain.add(pDes, BorderLayout.WEST);

        //paneel stenagografie aanmaken
        JPanel pStegano = new JPanel(new GridBagLayout());
        pStegano.setBorder(BorderFactory.createTitledBorder("Steganography"));

        //compontenten toevoegen aan Stegano paneel
        gbc.gridx = 0;
        gbc.gridy = 0;
        pStegano.add(new JLabel("Text:"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        pStegano.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        txtSteganoText = new JTextArea(5, 25);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        JScrollPane spStegano = new JScrollPane(txtSteganoText);
        txtSteganoText.setBorder(border);
        pStegano.add(spStegano, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        txtSteganoImage = new JTextField(10);
        pStegano.add(txtSteganoImage, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        btnSteganoImage = new JButton("Select Image");
        btnSteganoImage.addActionListener(this);
        pStegano.add(btnSteganoImage, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        btnSteganoDecode = new JButton("Start Decryption");
        btnSteganoDecode.addActionListener(this);
        pStegano.add(btnSteganoDecode, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        btnSteganoEncode = new JButton("Start Encryption");
        btnSteganoEncode.addActionListener(this);
        pStegano.add(btnSteganoEncode, gbc);

        //stenago paneel toevoegen aan frame
        pMain.add(pStegano, BorderLayout.EAST);

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
        } else if (e.getSource() == btnSteganoImage) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP", "bmp", "bmp");
            fch.setFileFilter(filter);
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                txtSteganoImage.setText(fch.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == btnSteganoEncode) {
            //set Console window
            Steganography.console = txtConsole;
            Steganography.DEBUG = true;

            txtConsole.append("Encoding started!" + "\n\r");

            //start encoding
            BufferedImage img = Steganography.encode(txtSteganoImage.getText(), txtSteganoText.getText());
            JFileChooser fch = new JFileChooser();

            int returnVal = fch.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                writeImage(fch.getSelectedFile().getAbsolutePath(), "bmp", img);
            }

            txtSteganoText.setText("");
            
            txtConsole.append("Encoding finished!" + "\n\r");
        } else if (e.getSource() == btnSteganoDecode) {
            Steganography.console = txtConsole;
            Steganography.DEBUG = true;
            
            txtConsole.append("Decoding started!" + "\n\r");
            
            String str = Steganography.decode(txtSteganoImage.getText());
            
            txtSteganoText.setText(str);
            
            txtConsole.append("Decoding finished!" + "\n\r");
        } else if (e.getSource() == btnDesEncode) {
            txtConsole.append("Encrypting started!" + "\n\r");
            long before = System.currentTimeMillis();
            des.encryptFile(txtDesFile.getText(), txtDesKey.getText());
            long after = System.currentTimeMillis();
            txtConsole.append("Time encrypting in milliseconds " + (after - before) + "\n\r");
        } else if (e.getSource() == btnDesDecode) {
            txtConsole.append("Decrypting started!" + "\n\r");
            long before = System.currentTimeMillis();
            des.decryptFile(txtDesFile.getText(), txtDesKey.getText());
            long after = System.currentTimeMillis();
            txtConsole.append("Time decrypting in milliseconds " + (after - before) + "\n\r");
        }

    }

    public void writeImage(String path, String ext, BufferedImage img) {
        try {
            File f = new File(path);

            f.delete();
            ImageIO.write(img, ext, f);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error while saving file!!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
