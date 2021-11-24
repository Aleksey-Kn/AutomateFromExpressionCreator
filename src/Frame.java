import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Frame extends JFrame {
    private JLabel canCreate = null;
    private List<String> chainsList;

    private Frame() {
        super("Automate generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200, 100, 950, 600);
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
        chainsPane.add(new JScrollPane(chains));
        JPanel checkChainsPane = new JPanel();
        checkChainsPane.setLayout(new BoxLayout(checkChainsPane, BoxLayout.Y_AXIS));
        chainsPane.add(new JScrollPane(checkChainsPane));

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
            createTable(tableModel, terminals.getText(), states.getText());
            automatePane.updateUI();
        });
        automatePane.add(new JScrollPane(regulations));
        JButton checkChains = new JButton("Check chains");
        automatePane.add(checkChains);
        JTextField userChain = new JTextField();
        automatePane.add(userChain);
        JButton checkUserChain = new JButton("Check this chain");
        automatePane.add(checkUserChain);

        checkUserChain.addActionListener(l -> {
            checkChainsPane.removeAll();
            if(canCreate != null){
                automatePane.remove(canCreate);
                canCreate = null;
            }
            try {
                Automate automate = new Automate(Arrays.stream(terminals.getText().split(" "))
                        .map(String::trim).map(s -> s.charAt(0)).collect(Collectors.toSet()),
                        new TreeSet<>(List.of(states.getText().split(" "))),
                        regulationsFormTable(tableModel),
                        startState.getText().trim(),
                        new TreeSet<>(List.of(endStates.getText().split(" "))));
                StringBuilder cause = new StringBuilder();
                List<String> logs = new LinkedList<>();
                boolean isCreate = automate.canCreate(userChain.getText().trim(), cause, logs);
                logs.forEach(log -> checkChainsPane.add(new JLabel(log)));
                canCreate = new JLabel(isCreate? "Can create": cause.toString());
                automatePane.add(canCreate);
            } catch (Exception e) {
                JLabel exceptionLabel = new JLabel(e.getMessage());
                exceptionLabel.setForeground(Color.RED);
                checkChainsPane.add(exceptionLabel);
                e.printStackTrace();
            }
            chainsPane.updateUI();
            automatePane.updateUI();
        });

        generateAutomate.addActionListener(l -> {
            try {
                AutomateDTO automateDTO = new FromRegularExpressionGenerator(expression.getText().trim()).generateAutomate();
                states.setText(String.join(" ", automateDTO.getStates()));
                terminals.setText(String.join(" ", automateDTO.getTerminals().stream()
                        .map(c -> Character.toString(c)).collect(Collectors.toSet())));
                startState.setText(automateDTO.getStartState());
                endStates.setText(String.join(" ", automateDTO.getEndStares()));
                createTable(tableModel, terminals.getText(), states.getText());
                for(int ri = 0; ri < tableModel.getRowCount(); ri++){
                    for(int ci = 1; ci < tableModel.getColumnCount(); ci++){
                        if(automateDTO.getRegulations().containsKey((String) tableModel.getValueAt(ri, 0))
                                && automateDTO.getRegulations().get((String) tableModel.getValueAt(ri, 0))
                                    .containsKey(tableModel.getColumnName(ci).charAt(0))){
                            tableModel.setValueAt(automateDTO.getRegulations()
                                    .get((String) tableModel.getValueAt(ri, 0))
                                    .get(tableModel.getColumnName(ci).charAt(0)), ri, ci);
                        } else{
                            tableModel.setValueAt("", ri, ci);
                        }
                    }
                }
            } catch (IllegalArgumentException e){
                states.setText(e.getMessage());
            } catch (Exception e){
                e.printStackTrace();
            }
            automatePane.updateUI();
        });

        chainsFromRegular.addActionListener(l -> {
            chains.removeAll();
            try {
                chainsList = new FromRegularExpressionGenerator(expression.getText().trim())
                        .generateChains(Integer.parseInt(fromSize.getText()), Integer.parseInt(toSize.getText()));
                        chainsList.forEach(e -> chains.add(new JLabel(e)));
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
                Automate automate = new Automate(Arrays.stream(terminals.getText().split(" "))
                        .map(String::trim).map(s -> s.charAt(0)).collect(Collectors.toSet()),
                        new TreeSet<>(List.of(states.getText().split(" "))),
                        regulationsFormTable(tableModel),
                        startState.getText().trim(),
                        new TreeSet<>(List.of(endStates.getText().split(" "))));
                chainsList.forEach(c -> {
                    StringBuilder cause = new StringBuilder();
                    StringBuilder logConcatenation = new StringBuilder();
                    List<String> logs = new LinkedList<>();
                    boolean isCreate = automate.canCreate(c, cause, logs);
                    logs.forEach(log -> logConcatenation.append(log).append("->"));
                    logConcatenation.delete(logConcatenation.length() - 2, logConcatenation.length());
                    JLabel automateOutputLabel = new JLabel();
                    if(isCreate){
                        automateOutputLabel.setForeground(Color.GREEN);
                        automateOutputLabel.setText(logConcatenation.toString());
                    } else{
                        automateOutputLabel.setForeground(Color.RED);
                        automateOutputLabel.setText(logConcatenation.append(": ").append(cause).toString());
                    }
                    checkChainsPane.add(automateOutputLabel);
                });
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

    private Set<String[]> regulationsFormTable(DefaultTableModel model){
        Set<String[]> result = new HashSet<>();
        for(int ri = 0; ri < model.getRowCount(); ri++){
            for(int ci = 1; ci < model.getColumnCount(); ci++){
                if(!((String)model.getValueAt(ri, ci)).trim().equals("")){
                    result.add(new String[]{(String) model.getValueAt(ri, 0), model.getColumnName(ci),
                            (String) model.getValueAt(ri, ci)});
                }
            }
        }
        return result;
    }

    private void createTable(DefaultTableModel tableModel, String terminals, String states){
        Vector<String> columnIdentifier = new Vector<>(List.of(terminals.split(" ")));
        columnIdentifier.add(0, " ");
        tableModel.setColumnIdentifiers(columnIdentifier);
        String[] rowsHandler = states.split(" ");
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
    }

    public static void main(String[] args) {
        new Frame();
    }
}
