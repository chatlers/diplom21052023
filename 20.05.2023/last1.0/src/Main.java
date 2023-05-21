import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class Main {
    public static void main(String[] args) {
        System.setProperty("java.security.policy", "C:/Users/makss/OneDrive/Desktop/last1/java.policy");
        JFrame frame = new JFrame("Network Scanner");
        frame.setSize(1920, 1080);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Создаем панель для кнопки Scan и поля ввода IP-адреса
        JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton scanButton = new JButton("Scan");
        scanPanel.add(scanButton);

        JLabel ipLabel = new JLabel("IP Address:");
        JTextField ipTextField = new JTextField("192.168.0.", 20);
        scanPanel.add(ipLabel);
        scanPanel.add(ipTextField);

        panel.add(scanPanel, BorderLayout.NORTH);

        // Создаем таблицу на 4 столбца
        String[] columns = {"IP Address", "MAC address", "Device name", "Device Status"};
        Object[][] data = {{"1.1.1.1", "Device 1", "Up"}, {"2.2.2.2", "Device 2", "Down"}, {"3.3.3.3", "Device 3", "Unknown"},
                {"1.1.1.1", "Device 1", "Up"}, {"1.1.1.1", "Device 5", "Up"}};
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Создаем всплывающее меню "Файл" с пунктом "Сохранить файл"
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem saveMenuItem = new JMenuItem("Сохранить файл");
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        frame.add(panel);
        frame.setVisible(true);

        // Создаем модель таблицы
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("IP Address");
        model.addColumn("MAC address");
        model.addColumn("Device name");
        model.addColumn("Device status");
//        model.addColumn("Device OS");
        table.setModel(model);

        scanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Создаем экземпляр класса Network и сканируем сеть
                Network network = new Network(ipTextField.getText());
                network.scanNetwork();

                // Считываем данные из файла и заносим их в таблицу
                try {
                    File inputFile = new File("output.txt");
                    BufferedReader reader = new BufferedReader(new FileReader(inputFile));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split("\t");
                        model.addRow(values);
                    }

                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 14));

        // Создаем всплывающее окно "О программе"
        JMenuItem aboutMenuItem = new JMenuItem("О программе");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Начальная версия автоматизированной информационной системы,\n" +
                                "позволяющее сканировать подсеть по заданному адресу.\n\n" +
                                "Для начала работы с программой, укажите подсеть, в которой\n" +
                                "нужно произвести поиск устройств.\n\n" +
                                "Для сохранения данных в таблицу используйте меню \"Файл - Сохранить файл\"", "Сканирование сети"
                        , JOptionPane.INFORMATION_MESSAGE);
            }
        });
        fileMenu.add(aboutMenuItem);

        ////////
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JTextField searchTextField = new JTextField("", 20);
        searchPanel.add(searchTextField);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);


        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = searchTextField.getText().trim().toLowerCase();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel)table.getModel());
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                table.setRowSorter(sorter);
            }
        });


        // Создаем всплывающее окно "Сохранить файл"
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        int rowCount = model.getRowCount();
                        int colCount = model.getColumnCount();

                        String data = "";
                        for (int i = 0; i < colCount; i++) {
                            data += model.getColumnName(i) + "\t";
                        }
                        data += "\n";
                        for (int i = 0; i < rowCount; i++) {
                            for (int j = 0; j < colCount; j++) {
                                data += model.getValueAt(i, j) + "\t";
                            }
                            data += "\n";
                        }

                        // Записываем данные в файл
                        FileWriter writer = new FileWriter(selectedFile);
                        writer.write(data);
                        writer.close();

                        JOptionPane.showMessageDialog(frame, "Файл успешно сохранен!", "Сохранение файла", JOptionPane.INFORMATION_MESSAGE);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
}
