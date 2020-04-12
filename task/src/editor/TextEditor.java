package editor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextEditor extends JFrame {

    private JTextArea textArea;
    private JTextField textField;
    private JCheckBox useRegex;
    private Pattern pattern;
    private Matcher matcher;
    private JFileChooser jfc = new JFileChooser(".", null);
    private ArrayList<Integer> searchIndexes;
    private int currentSearchIndex;

    // class constructor
    public TextEditor() {
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        setTextArea();
        setButtons();
        setFileMenu();

        setVisible(true);
        add(jfc);
    }

    // method to initialize text area
    private void setTextArea() {

        //creating text area
        textArea = new JTextArea();

        // creating a background panel
        JPanel backgroundPanel = new JPanel(new BorderLayout());
        // setting symmetric borders
        setMargin(backgroundPanel, 0, 8, 8, 8);

        // wrapping text area by a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(backgroundPanel.add(scrollPane), BorderLayout.CENTER);

        // setting names to the components
        textArea.setName("TextArea");
        scrollPane.setName("ScrollPane");
        jfc.setName("FileChooser");
    }

    public static void setMargin(JComponent aComponent, int aTop,
                                 int aRight, int aBottom, int aLeft) {

        Border border = aComponent.getBorder();

        Border marginBorder = new EmptyBorder(new Insets(aTop, aLeft,
                aBottom, aRight));
        aComponent.setBorder(border == null ? marginBorder
                : new CompoundBorder(marginBorder, border));
    }


    // method returning actual actionListener on choosing the action
    // (implemented like a method for choosing appropriate listener for buttons and menu items either)
    public ActionListener getActionListener(String action) {

        switch (action) {

            // action listener for load button and menu
            case "load":
                return actionEvent -> {
                    try {
                        int returnValue = jfc.showOpenDialog(null);
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            textField.setText(jfc.getSelectedFile().getName());
                            File file = jfc.getSelectedFile();
                            FileReader reader = new FileReader(file);
                            BufferedReader bufferedReader = new BufferedReader(reader);
                            textArea.read(bufferedReader, null);
                        }
                    } catch (IOException e) {
                        textArea.setText(null);
                    }
                };

            // action listener for save button and menu
            case "save":
                return actionEvent -> {
                    try {
                        int returnValue = jfc.showSaveDialog(null);
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            textField.setText(jfc.getSelectedFile().getName());
                            File file = jfc.getSelectedFile();
                            FileWriter writer = new FileWriter(file);
                            writer.write(textArea.getText());
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

            // action listener for search button and menu
            case "search":
                return actionEvent -> {
                    if (useRegex.isSelected()) {
                        pattern = Pattern.compile(textField.getText());
                        matcher = pattern.matcher(textArea.getText());
                    }
                    searchIndexes = new ArrayList<>();
                    currentSearchIndex = 0;
                    new Thread(() -> {
                        int index = 0;
                        String foundText = null;

                        // search with regex
                        if (useRegex.isSelected()) {
                            if (matcher.find()) {
                                index = matcher.start();
                                foundText = matcher.group();
                                searchIndexes.add(index);
                                while (matcher.find())
                                    searchIndexes.add(matcher.start());
                            }
                        // search without regex
                        } else {
                            if (textArea.getText().contains(textField.getText())) {
                                index = textArea.getText().indexOf(textField.getText());
                                foundText = textField.getText();
                                searchIndexes.add(textArea.getText().indexOf(foundText));
                                int start = searchIndexes.get(0) + foundText.length();
                                while ((start = textArea.getText().indexOf(foundText, start)) != -1) {
                                    searchIndexes.add(start);
                                    start += foundText.length();
                                }
                            }
                        }
                        // setting caret position after search
                        if (foundText != null) {
                            textArea.setCaretPosition(index + foundText.length());
                            textArea.select(index, index + foundText.length());
                            textArea.grabFocus();
                        }
                    }).start();
                };

            // action listener for search previous button
            case "previous":
                return actionEvent -> {
                    if (searchIndexes.size() > 0) {
                        if (currentSearchIndex == 0)
                            currentSearchIndex = searchIndexes.size() - 1;
                        else
                            currentSearchIndex--;
                        int index = 0;
                        String foundText = null;
                        if (useRegex.isSelected()) {
                            if (matcher.find(searchIndexes.get(currentSearchIndex))) {
                                index = matcher.start();
                                foundText = matcher.group();
                            }
                        } else {
                            index = searchIndexes.get(currentSearchIndex);
                            foundText = textField.getText();
                        }
                        if (foundText != null) {
                            textArea.setCaretPosition(index + foundText.length());
                            textArea.select(index, index + foundText.length());
                            textArea.grabFocus();
                        }
                    }
                };

            // action listener for search next button
            case "next":
                return actionEvent -> {
                    if (searchIndexes.size() > 0) {
                        if (currentSearchIndex == searchIndexes.size() - 1)
                            currentSearchIndex = 0;
                        else
                            currentSearchIndex++;
                        int index = 0;
                        String foundText = null;
                        if (useRegex.isSelected()) {
                            if (matcher.find(searchIndexes.get(currentSearchIndex))) {
                                index = matcher.start();
                                foundText = matcher.group();
                            }
                        } else {
                            index = searchIndexes.get(currentSearchIndex);
                            foundText = textField.getText();
                        }
                        if (foundText != null) {
                            textArea.setCaretPosition(index + foundText.length());
                            textArea.select(index, index + foundText.length());
                            textArea.grabFocus();
                        }
                    }
                };

            // action listener for regex selector
            case "regex":
                return actionEvent -> {
                    useRegex.setSelected(true);
                };
            default:
                return actionEvent -> System.exit(0);
        }
    }

    // creating button panel and setting buttons
    public void setButtons() {
        JButton save;
        JButton load;
        JButton search;
        JButton previous;
        JButton next;
        JPanel buttonPanel;

        // creating a button panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        setMargin(buttonPanel, 0, 0, 0, 0);

        // creating and setting buttons and search field
        save = new JButton(new ImageIcon("res/save_grad_30.png"));
        save.addActionListener(getActionListener("save"));

        load = new JButton(new ImageIcon("res/load_grad_30.png"));
        load.addActionListener(getActionListener("load"));

        search = new JButton(new ImageIcon("res/search_grad_30.png"));
        search.addActionListener(getActionListener("search"));

        previous = new JButton(new ImageIcon("res/prev_grad_30.png"));
        previous.addActionListener(getActionListener("previous"));

        next = new JButton(new ImageIcon("res/next_grad_30.png"));
        next.addActionListener(getActionListener("next"));

        useRegex = new JCheckBox("Use regex");

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 30));

        // adding buttons to the button panel
        buttonPanel.add(load);
        buttonPanel.add(save);
        buttonPanel.add(textField);
        buttonPanel.add(search);
        buttonPanel.add(previous);
        buttonPanel.add(next);
        buttonPanel.add(useRegex);

        add(buttonPanel, BorderLayout.NORTH);

        // setting names to the components
        save.setName("SaveButton");
        load.setName("OpenButton");
        search.setName("StartSearchButton");
        previous.setName("PreviousMatchButton");
        next.setName("NextMatchButton");
        useRegex.setName("UseRegExCheckbox");
        textField.setName("SearchField");
    }

    // creating menu bar and setting menu items
    public void setFileMenu() {
        JMenuBar menuBar;
        JMenu fileMenu;
        JMenuItem loadMenuItem;
        JMenuItem saveMenuItem;
        JMenuItem exitMenuItem;
        JMenu searchMenu;
        JMenuItem startMenuItem;
        JMenuItem previousMenuItem;
        JMenuItem nextMenuItem;
        JMenuItem regexMenuItem;

        //adding menu bar to the frame
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // setting menu "File"
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(getActionListener("load"));
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(getActionListener("save"));
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(getActionListener("exit"));

        // setting menu "search"
        searchMenu = new JMenu("Search");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(searchMenu);
        startMenuItem = new JMenuItem("Start search");
        startMenuItem.addActionListener(getActionListener("search"));
        previousMenuItem = new JMenuItem("Previous match");
        previousMenuItem.addActionListener(getActionListener("previous"));
        nextMenuItem = new JMenuItem("Next match");
        nextMenuItem.addActionListener(getActionListener("next"));
        regexMenuItem = new JMenuItem("Use regular expressions");
        regexMenuItem.addActionListener(getActionListener("regex"));

        // adding menu items to the file menu
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // adding menu items to the search menu
        searchMenu.add(startMenuItem);
        searchMenu.add(previousMenuItem);
        searchMenu.add(nextMenuItem);
        searchMenu.add(regexMenuItem);

        // setting names to the components
        fileMenu.setName("MenuFile");
        loadMenuItem.setName("MenuOpen");
        saveMenuItem.setName("MenuSave");
        exitMenuItem.setName("MenuExit");
        searchMenu.setName("MenuSearch");
        startMenuItem.setName("MenuStartSearch");
        previousMenuItem.setName("MenuPreviousMatch");
        nextMenuItem.setName("MenuNextMatch");
        regexMenuItem.setName("MenuUseRegExp");
    }
}
