package cryptogen;

import cryptogen.steganography.Steganography;
import cryptogen.des.*;
import helpers.ConsoleHelper;
import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Cornel, Nick, Arno, Peter
 */
public class CryptoGen extends JFrame implements ActionListener {

    //private DesService des = new SyncDesService();
    private DesService des = new AsyncDesService();
    //private DesService des = new AkkaDesService();
    //private DesService des = new DistributedDesService();
    
    private JTextField txtDesInputFile, txtDesEncryptedFile, txtDesKey, txtSteganoImage,
            txtSteganoFile, txtSteganoOutputImage;
    private JButton btnDesEncode, btnDesDecode, btnDesFile, btnDesOutputFile,
            btnSteganoEncode, btnSteganoDecode, btnSteganoImage, btnSteganoFile, btnSteganoOutputImage;
    private JTextArea txtConsole;
    private JCheckBox cbUse3des;

    public CryptoGen() {
        initGui();
        
        pack();
        setResizable(false);
        setVisible(true);
    }

    public void initGui() {
        //configureren JFrame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Crypto Generator");

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

        
        //componenten toevoegen aan paneel
        // DES row 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        pDes.add(new JLabel("Input File:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtDesInputFile = new JTextField(10);
        pDes.add(txtDesInputFile, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        btnDesFile = new JButton("Select File");
        btnDesFile.addActionListener(this);
        pDes.add(btnDesFile, gbc);
        
        // DES row 1
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        pDes.add(new JLabel("Encrypted File:"), gbc);
//        gbc.gridx = 1;
//        gbc.gridy = 1;
//        txtDesEncryptedFile = new JTextField(10);
//        pDes.add(txtDesEncryptedFile, gbc);
//        gbc.gridx = 2;
//        gbc.gridy = 1;
//        btnDesOutputFile = new JButton("Select File");
//        btnDesOutputFile.addActionListener(this);
//        pDes.add(btnDesOutputFile, gbc);
        // DES row 2
        gbc.gridx = 0;
        gbc.gridy = 3;
        pDes.add(new JLabel("Key:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        txtDesKey = new JTextField(10);
        pDes.add(txtDesKey, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        cbUse3des = new JCheckBox("Use 3DES");
        pDes.add(cbUse3des, gbc);
 
        // DES row 3
        gbc.gridx = 1;
        gbc.gridy = 4;
        btnDesEncode = new JButton("Start Encryption");
        btnDesEncode.addActionListener(this);
        pDes.add(btnDesEncode, gbc);
        gbc.gridx = 2;
        gbc.gridy = 4;
        btnDesDecode = new JButton("Start Decryption");
        btnDesDecode.addActionListener(this);
        pDes.add(btnDesDecode, gbc);

        //des paneel toevoegen aan frame
        pMain.add(pDes, BorderLayout.WEST);

        //paneel steganography aanmaken
        JPanel pStegano = new JPanel(new GridBagLayout());
        pStegano.setBorder(BorderFactory.createTitledBorder("Steganography"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        pStegano.add(new JLabel("Normal image:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        txtSteganoImage = new JTextField(10);
        pStegano.add(txtSteganoImage, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        btnSteganoImage = new JButton("Select image");
        btnSteganoImage.addActionListener(this);
        pStegano.add(btnSteganoImage, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        pStegano.add(new JLabel("Encoded image:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtSteganoOutputImage = new JTextField(10);
        pStegano.add(txtSteganoOutputImage, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        btnSteganoOutputImage = new JButton("Select image");
        btnSteganoOutputImage.addActionListener(this);
        pStegano.add(btnSteganoOutputImage, gbc);
        
        //compontenten toevoegen aan Stegano paneel
        gbc.gridx = 0;
        gbc.gridy = 2;
        pStegano.add(new JLabel("Input/output file:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtSteganoFile = new JTextField(10);
        pStegano.add(txtSteganoFile, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        btnSteganoFile = new JButton("Select file");
        btnSteganoFile.addActionListener(this);
        pStegano.add(btnSteganoFile, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        btnSteganoEncode = new JButton("Start Encryption");
        btnSteganoEncode.addActionListener(this);
        pStegano.add(btnSteganoEncode, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        btnSteganoDecode = new JButton("Start Decryption");
        btnSteganoDecode.addActionListener(this);
        pStegano.add(btnSteganoDecode, gbc);

        //stenago paneel toevoegen aan frame
        pMain.add(pStegano, BorderLayout.EAST);

        //console venster maken
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        txtConsole = new JTextArea(20, 20);
        JScrollPane scrollPane = new JScrollPane(txtConsole);
        pMain.add(scrollPane, BorderLayout.SOUTH);
        
        // add console textarea to helper class
        ConsoleHelper.console = this.txtConsole;
    }

    public static void main(String[] args) {
        new CryptoGen();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnDesFile) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                String filePath = fch.getSelectedFile().getAbsolutePath();
                txtDesInputFile.setText(filePath);
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
                String path = fch.getSelectedFile().getAbsolutePath();
                txtSteganoImage.setText(path);
                
                //autocomplete output path
                int dotPos = path.lastIndexOf(".");
                String outputPath = path.substring(0, dotPos) + ".encoded" + path.substring(dotPos);
                txtSteganoOutputImage.setText(outputPath);
            }
        } else if (e.getSource() == btnSteganoOutputImage) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP", "bmp", "bmp");
            fch.setFileFilter(filter);
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                String path = fch.getSelectedFile().getAbsolutePath();
                txtSteganoOutputImage.setText(path);
            }
        } else if (e.getSource() == btnSteganoEncode) {
            
            String inputImage = txtSteganoImage.getText();
            String outputImage = txtSteganoOutputImage.getText();
            String inputFilePath = txtSteganoFile.getText();
            
            if(inputImage.equals("")) {
                ConsoleHelper.appendError("You must enter a normal image. This is the source image that will be used to embed the file into.");
                return;
            }
            
            if(outputImage.equals("")) {
                ConsoleHelper.appendError("You must choose a location for the encoded image.");
                return;
            }
            
            if(inputFilePath.equals("")) {
                ConsoleHelper.appendError("You must enter an input file that you want to have encrypted.");
                return;
            }
            
            if(!CryptoGen.isFile(inputImage)) {
                ConsoleHelper.appendError("You must enter a valid \"normal image\" path.");
                return;
            }
            
            if(!CryptoGen.isFile(inputFilePath)) {
                ConsoleHelper.appendError("You must enter a valid \"input file\" path.");
                return;
            }
            
            ConsoleHelper.start("steganography encoding");
            //start encoding
            BufferedImage img = Steganography.encode(inputImage, inputFilePath);
            writeImage(outputImage, "bmp", img);
            
            ConsoleHelper.finish("steganography encoding");
            
        } else if (e.getSource() == btnSteganoDecode) {
            
            String imagePath = txtSteganoOutputImage.getText();
            String outputPath = txtSteganoFile.getText();
            
            if(imagePath.equals("")) {
                ConsoleHelper.appendError("You must choose an encoded image. This is an image that has data embedded with steganography.");
                return;
            }
            
            if(outputPath.equals("")) {
                ConsoleHelper.appendError("You must choose a location for the output file.");
                return;
            }
            
            ConsoleHelper.start("steganography decoding");
            Steganography.decode(imagePath, outputPath);
            ConsoleHelper.finish("steganography decoding");
            
        } else if (e.getSource() == btnDesEncode) {
            
            String inputFile = txtDesInputFile.getText();
            String key = txtDesKey.getText();

            if(key.equals("")) {
                ConsoleHelper.appendError("You must enter a key first.");
                return;
            }
            
            if(inputFile.equals("")) {
                ConsoleHelper.appendError("You must choose an input file first.");
                return;
            }

            if(!CryptoGen.isFile(inputFile)) {
                ConsoleHelper.appendError("You must enter a valid path for the input file.");
                return;
            }
            
            if(cbUse3des.isSelected()) {
                ConsoleHelper.start("3des encryption");
                //des.encryptFile3Des(inputFile, outputFile, key);
                des.encryptFile3Des(inputFile, key);
                ConsoleHelper.finish("3des encryption");
            } else {
                ConsoleHelper.start("des encryption");
                //des.encryptFile(inputFile, outputFile, key);
                des.encryptFile(inputFile, key);
                ConsoleHelper.finish("des encryption");
            }

            
        } else if (e.getSource() == btnDesDecode) {
            
            String inputFile = txtDesInputFile.getText();
            String key = txtDesKey.getText();

            if(key.equals("")) {
                ConsoleHelper.appendError("You must enter a key first.");
                return;
            }
            if(inputFile.equals("") || !CryptoGen.isFile(inputFile)) {
                ConsoleHelper.appendError("You must enter a valid path for the input file.");
                return;
            }
            

            if(cbUse3des.isSelected()) {
                ConsoleHelper.start("3des decryption");
                //des.decryptFile3Des(inputFile, outputFile, key);
                des.decryptFile3Des(inputFile, key);
                ConsoleHelper.finish("3des decryption");
            } else {
                ConsoleHelper.start("des decryption");
                //des.decryptFile(inputFile, outputFile, key);
                des.decryptFile(inputFile, key);
                ConsoleHelper.finish("des decryption");
            }
            
        } else if (e.getSource() == btnSteganoFile) {
            //create file choose window
            JFileChooser fch = new JFileChooser();
            fch.showSaveDialog(this);

            //check of een file is geselecteerd
            if (fch.getSelectedFile() != null) {
                txtSteganoFile.setText(fch.getSelectedFile().getAbsolutePath());
            }
        }

    }

    public void writeImage(String path, String ext, BufferedImage img) {
        try {
            File f = new File(path);

            f.delete();
            ImageIO.write(img, ext, f);
        } catch (Exception e) {
            ConsoleHelper.appendError("Error while saving file.");
            ConsoleHelper.appendError(e.getMessage());
        }
    }
    
    public static boolean isFile (String path) {
        File file = new File(path);
        if(file.exists())
            return true;
        else 
            return false;
    }
}
