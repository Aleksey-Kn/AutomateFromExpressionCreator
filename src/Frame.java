import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class Frame extends JFrame {
    private JLabel canCreate = null;
    private FromRegularExpressionGenerator fromRegularExpressionGenerator;

    private Frame() {
        super("Automate generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200, 100, 650, 500);
        setLayout(new BorderLayout());

        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
        add(inputPane, BorderLayout.WEST);
        JPanel chainsPane = new JPanel();
        chainsPane.setLayout(new BoxLayout(chainsPane, BoxLayout.X_AXIS));
        add(chainsPane, BorderLayout.CENTER);
        JPanel automatePane = new JPanel();
        automatePane.setLayout(new BoxLayout(automatePane, BoxLayout.Y_AXIS));
        add(automatePane, BorderLayout.EAST);
        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.X_AXIS));
        add(sizePanel, BorderLayout.SOUTH);
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        add(menuPanel, BorderLayout.NORTH);

        JPanel chains = new JPanel();
        chains.setLayout(new BoxLayout(chains, BoxLayout.Y_AXIS));
        chainsPane.add(chains);
        JPanel checkChainsPane = new JPanel();
        checkChainsPane.setLayout(new BoxLayout(checkChainsPane, BoxLayout.Y_AXIS));
        chainsPane.add(checkChainsPane);

        JButton about = new JButton("About author");
        menuPanel.add(about);
        JButton theme = new JButton("Theme");
        menuPanel.add(theme);
        JButton open = new JButton("Open initial data");
        menuPanel.add(open);
        JButton saveAutomate = new JButton("Save expression to file");
        menuPanel.add(saveAutomate);
        JButton saveChains = new JButton("Save chains to file");
        menuPanel.add(saveChains);

        sizePanel.add(new JLabel("Size of chains: "));
        sizePanel.add(new JLabel("from "));
        JTextField fromSize = new JTextField();
        sizePanel.add(fromSize);
        sizePanel.add(new JLabel(" to "));
        JTextField toSize = new JTextField();
        sizePanel.add(toSize);

        inputPane.add(new JLabel("Regular expression:"));
        JTextField expression = new JTextField();
        inputPane.add(expression);
        JButton chainsFromRegular = new JButton("Generate chains");
        inputPane.add(chainsFromRegular);
        JButton generateAutomate = new JButton("Generate automate");
        inputPane.add(generateAutomate);

        automatePane.add(new JLabel("Set of state:"));
        JTextField states = new JTextField();
        automatePane.add(states);
        automatePane.add(new JLabel("Terminals symbol:"));
        JTextField terminals = new JTextField();
        automatePane.add(terminals);
        automatePane.add(new JLabel("Start state:"));
        JTextField startState = new JTextField();
        automatePane.add(startState);
        automatePane.add(new JLabel("End states:"));
        JTextField endStates = new JTextField();
        automatePane.add(endStates);
        JButton update = new JButton("Update table");
        automatePane.add(update);
        DefaultTableModel tableModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        JTable regulations = new JTable(tableModel);
        update.addActionListener(l -> {
            Vector<String> columnIdentifier = new Vector<>(java.util.List.of(terminals.getText().split(" ")));
            columnIdentifier.add(0, "");
            tableModel.setColumnIdentifiers(columnIdentifier);
            String[] rowsHandler = states.getText().split(" ");
            int it = 0;
            while (it < tableModel.getRowCount() && it < rowsHandler.length){
                tableModel.setValueAt(rowsHandler[it], it, 0);
                it++;
            }
            if(it < tableModel.getRowCount()) {
                for(int i = tableModel.getRowCount() - it; i > 0; i--)
                    tableModel.removeRow(tableModel.getRowCount() - 1);
            } else {
                while (it < rowsHandler.length) {
                    tableModel.addRow(new String[]{rowsHandler[it++]});
                }
            }
        });
        automatePane.add(regulations);
        JButton checkChains = new JButton("Check chains");
        automatePane.add(checkChains);
        JTextField userChain = new JTextField();
        automatePane.add(userChain);
        JButton checkUserChain = new JButton("Check this chain");
        automatePane.add(checkUserChain);

        // чекнуть допустимость пользовательской цепочки (checkUserChain)

        //построить автомат из регулрярки (generateAutomate)

        chainsFromRegular.addActionListener(l -> {
            chains.removeAll();
            try {
                new FromRegularExpressionGenerator(expression.getText())
                        .generateChains(Integer.parseInt(fromSize.getText()), Integer.parseInt(toSize.getText()))
                        .forEach(e -> chains.add(new JLabel(e)));
            } catch (Exception e) {
                JLabel exceptionLabel = new JLabel(e.getMessage());
                exceptionLabel.setForeground(Color.RED);
                chains.add(exceptionLabel);
                e.printStackTrace();
            }
            chainsPane.updateUI();
        });

        checkChains.addActionListener(l -> {
            checkChainsPane.removeAll();
            if(canCreate != null){
                automatePane.remove(canCreate);
                canCreate = null;
            }
            try {

            } catch (Exception e) {
                JLabel exceptionLabel = new JLabel(e.getMessage());
                exceptionLabel.setForeground(Color.RED);
                checkChainsPane.add(exceptionLabel);
                e.printStackTrace();
            }
            chainsPane.updateUI();
        });

        about.addActionListener(l ->
                JOptionPane.showMessageDialog(null, "Терентьев Данил, ИП-814"));
        theme.addActionListener(l ->
                JOptionPane.showMessageDialog(null,
                        """
                                Написать программу, которая по заданному регулярному выражению построит эквивалентный ДКА.
                                Функция переходов ДКА может изображаться в виде таблицы и графа (выбор вида отображения посредством меню).
                                Программа должна сгенерировать по РВ несколько цепочек в заданном диапазоне длин и проверить их допустимость построенным автоматом.
                                Процесс разбора цепочек автоматом отображать на экране. Предусмотреть возможность разбора цепочки,
                                введённой пользователем. В качестве исходных данных допускаются РВ, порождающие цепочки,
                                имеющие определенное количество циклических повторений всех символов алфавита или некоторой их части, заканчивающиеся на заданную цепочку.
                                Например, (а+b+с)*ааса, или ((а+b)(а+b))*аасb, и т.п.
                                """));
        saveAutomate.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла для сохранения",
                    "Сохранение автомата", JOptionPane.QUESTION_MESSAGE);
            if(name != null) {
                try {
                    PrintWriter writer = new PrintWriter(new FileWriter(name));
                    writer.print("M({");
                    writer.print(states.getText());
                    writer.print("}, {");
                    writer.print(terminals.getText());
                    writer.print("}, P, ");
                    writer.print(startState.getText());
                    writer.print(", {");
                    writer.print(endStates.getText());
                    writer.println("})");
                    writer.println("P:");
                    for(int i = 0; i < tableModel.getRowCount(); i++){
                        for (int j = 0; j < tableModel.getColumnCount(); j++)
                            writer.print(tableModel.getValueAt(i, j) + "\t");
                        writer.println();
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveChains.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла для сохранения",
                    "Сохранение цепочек", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt"));
                    for (Component component : chainsPane.getComponents()) {
                        writer.println(((Label) component).getText());
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        open.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла",
                    "Чтение регулярного выражения из файла", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    Scanner scanner = new Scanner(new File(name + ".txt"));
                    StringBuilder stringBuilder = new StringBuilder();
                    while (scanner.hasNextLine())
                        stringBuilder.append(scanner.nextLine());
                    expression.setText(stringBuilder.toString());
                    scanner.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new Frame();
    }
}
