import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.prefs.Preferences;

public class Main extends JFrame {

    private JTextArea textArea;
    private JFileChooser fileChooser;
    private File currentFile;
    private JCheckBoxMenuItem darkModeMenuItem;

    public Main() {
        setTitle("Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Load window size and position from preferences
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        int windowX = prefs.getInt("windowX", 100);
        int windowY = prefs.getInt("windowY", 100);
        int windowWidth = prefs.getInt("windowWidth", 800);
        int windowHeight = prefs.getInt("windowHeight", 600);
        setLocation(windowX, windowY);
        setSize(windowWidth, windowHeight);

        // Create a text area for editing text
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Default font and size
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Create a file chooser with modern look and feel
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        // Create menu bar with File and Settings menus
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);

        // Create menu items for Settings menu
        darkModeMenuItem = new JCheckBoxMenuItem("Dark Mode");
        settingsMenu.add(darkModeMenuItem);

        // Add font settings submenu under Settings
        JMenuItem changeFontItem = new JMenuItem("Change Font...");
        settingsMenu.add(changeFontItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        // Add accelerators (keyboard shortcuts) to menu items
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        darkModeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        changeFontItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));

        // Add action listeners to menu items
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveFileAs());
        darkModeMenuItem.addActionListener(e -> toggleDarkMode());
        changeFontItem.addActionListener(e -> changeFont());

        // Set default dark mode state based on preferences
        darkModeMenuItem.setSelected(prefs.getBoolean("darkMode", false));
        if (darkModeMenuItem.isSelected()) {
            applyDarkMode();
        }

        // Add mouse wheel listener for font size adjustment
        textArea.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                int fontSize = textArea.getFont().getSize();
                fontSize -= e.getWheelRotation() * 2; // Increase/decrease font size by 2 for each scroll notch
                textArea.setFont(textArea.getFont().deriveFont((float) fontSize));
            }
            else
            {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                int scrollAmount = verticalScrollBar.getUnitIncrement();
                int delta = e.getUnitsToScroll() * scrollAmount;
                verticalScrollBar.setValue(verticalScrollBar.getValue() + delta * 15);
            }
        });

        // Save window size and position on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Store current window size and position in preferences
                prefs.putInt("windowX", getLocation().x);
                prefs.putInt("windowY", getLocation().y);
                prefs.putInt("windowWidth", getWidth());
                prefs.putInt("windowHeight", getHeight());
                super.windowClosing(e);
            }
        });

        setVisible(true);
    }

    private void openFile() {
        UIManager.put("FileChooserUI", "com.sun.java.swing.plaf.windows.WindowsFileChooserUI");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.setText(""); // Clear existing text
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
            } catch (IOException e) {
                showErrorDialog("Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            writer.write(textArea.getText());
        } catch (IOException e) {
            showErrorDialog("Error saving file: " + e.getMessage());
        }
    }

    private void saveFileAs() {
        UIManager.put("FileChooserUI", "com.sun.java.swing.plaf.windows.WindowsFileChooserUI");
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            currentFile = selectedFile;
            saveFile();
        }
    }

    private void toggleDarkMode() {
        boolean darkModeEnabled = darkModeMenuItem.isSelected();
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.putBoolean("darkMode", darkModeEnabled);
        if (darkModeEnabled) {
            applyDarkMode();
        } else {
            applyLightMode();
        }
    }

    private void applyDarkMode() {
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
    }

    private void applyLightMode() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
    }

    private void changeFont() {
        Font selectedFont = (Font) JOptionPane.showInputDialog(
                this,
                "Choose Font",
                "Font Chooser",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                textArea.getFont());

        if (selectedFont != null) {
            textArea.setFont(selectedFont);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main editor = new Main();
            if (args.length > 0) {
                String filePath = args[0];
                editor.openFile(new File(filePath));
            }
        });
    }

    private void openFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            currentFile = file;
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.setText(""); // Clear existing text
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
            } catch (IOException e) {
                showErrorDialog("Error opening file: " + e.getMessage());
            }
        }
    }

}
