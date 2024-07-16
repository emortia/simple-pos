import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SimplePOS extends JFrame {
    private List<Product> inventory;
    private List<Product> cart;
    private DefaultTableModel inventoryTableModel;
    private DefaultTableModel cartTableModel;

    private JTable inventoryTable;
    private JTable cartTable;
    private JTextArea receiptTextArea;

    // File name for inventory data
    private static final String INVENTORY_FILE = "inventory.txt";

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Add to Cart" : value.toString());
            return this;
        }
    }

    // Button Editor class
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    addToCart(button);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Add to Cart" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public SimplePOS() {
        inventory = new ArrayList<>();
        cart = new ArrayList<>();
        inventoryTableModel = new DefaultTableModel();
        cartTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cart table editing
            }
        };
    
        setTitle("Simple POS System");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        initializeUI();
    
        // Load inventory data from file
        loadInventoryFromFile();
    
        setVisible(true);
    }        

    private void initializeUI() {
        // Create main panels
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        JPanel cartPanel = new JPanel(new BorderLayout());
        JPanel receiptPanel = new JPanel(new BorderLayout());
    
        // Set titles for each panel
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Inventory"));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Cart"));
        receiptPanel.setBorder(BorderFactory.createTitledBorder("Receipt"));
    
        // Create inventory table
        String[] inventoryHeaders = {"Name", "Price", "Stock", "Action"};
        inventoryTableModel.setColumnIdentifiers(inventoryHeaders);
        inventoryTable = new JTable(inventoryTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Enable editing only for the Action column
            }
        };
        inventoryTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering
        JScrollPane inventoryScrollPane = new JScrollPane(inventoryTable);
    
        // Set custom renderer and editor for the Action column
        inventoryTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        inventoryTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
    
        // Create cart table
        String[] cartHeaders = {"Name", "Price", "Quantity"};
        cartTableModel.setColumnIdentifiers(cartHeaders);
        cartTable = new JTable(cartTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cart table editing
            }
        };
        cartTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
    
        // Create buttons without borders
        JButton checkoutButton = new JButton("Checkout");
        JButton editCartButton = new JButton("Edit Cart");
        JButton deleteItemButton = new JButton("Delete Item");
        JButton addProductButton = new JButton("Add Product");
        JButton editProductButton = new JButton("Edit Product");
        JButton deleteProductButton = new JButton("Delete Product");
    
        // Create receipt text area
        receiptTextArea = new JTextArea();
        receiptTextArea.setEditable(false);
        JScrollPane receiptScrollPane = new JScrollPane(receiptTextArea);
    
        // Set preferred sizes
        inventoryScrollPane.setPreferredSize(new Dimension(400, 400));
        cartScrollPane.setPreferredSize(new Dimension(300, 400));
        receiptScrollPane.setPreferredSize(new Dimension(250, 400));
    
        // Add panels to main frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(inventoryPanel, BorderLayout.WEST);
        getContentPane().add(cartPanel, BorderLayout.CENTER);
        getContentPane().add(receiptPanel, BorderLayout.EAST);
    
        // Add components to panels
        inventoryPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(checkoutButton);
        buttonPanel.add(editCartButton);
        buttonPanel.add(deleteItemButton);
        
    
        // Create a panel for the buttons with FlowLayout (horizontal layout)
        JPanel inventoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inventoryButtonPanel.add(addProductButton);
        inventoryButtonPanel.add(editProductButton);
        inventoryButtonPanel.add(deleteProductButton);
    
        inventoryPanel.add(inventoryButtonPanel, BorderLayout.SOUTH);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(buttonPanel,BorderLayout.SOUTH);
        receiptPanel.add(receiptScrollPane, BorderLayout.CENTER);
    
        // Add action listeners

        deleteItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCartItem();
            }
        });

        editCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editCart();
            }
        });

        checkoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkout();
            }
        });
    
        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });
    
        editProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editProduct();
            }
        });
    
        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProduct();
            }
        });
    
        // Load inventory data from file
        loadInventoryFromFile();
    
        // Add sample data if file is not found
        if (inventory.isEmpty()) {
            addSampleData();
        }
    }
    
    private void addProduct() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        Object[] inputFields = {
            "Name:", nameField,
            "Price:", priceField,
            "Stock:", stockField
        };
    
        int option = JOptionPane.showConfirmDialog(this, inputFields, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            double price;
            int stock;
            try {
                price = Double.parseDouble(priceField.getText());
                stock = Integer.parseInt(stockField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid values.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            Product product = new Product(name, price, stock);
            inventory.add(product);
            updateInventoryTable();
            saveInventoryToFile();
        }
    }
    
    private void editProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String name = (String) inventoryTableModel.getValueAt(selectedRow, 0);
        double price = (double) inventoryTableModel.getValueAt(selectedRow, 1);
        int stock = (int) inventoryTableModel.getValueAt(selectedRow, 2);
    
        JTextField nameField = new JTextField(name);
        JTextField priceField = new JTextField(String.valueOf(price));
        JTextField stockField = new JTextField(String.valueOf(stock));
        Object[] inputFields = {
            "Name:", nameField,
            "Price:", priceField,
            "Stock:", stockField
        };
    
        int option = JOptionPane.showConfirmDialog(this, inputFields, "Edit Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            name = nameField.getText();
            try {
                price = Double.parseDouble(priceField.getText());
                stock = Integer.parseInt(stockField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid values.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            Product product = inventory.get(selectedRow);
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            updateInventoryTable();
            saveInventoryToFile();
        }
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0); // Clear existing cart table
    
        // Populate cart data into table
        for (Product product : cart) {
            Object[] rowData = {product.getName(), product.getPrice(), product.getStock()};
            cartTableModel.addRow(rowData);
        }
    }

    private void editCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product in the cart to edit.",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Get current values from the cart table
        String name = (String) cartTableModel.getValueAt(selectedRow, 0);
        double price = (double) cartTableModel.getValueAt(selectedRow, 1);
        int currentQuantity = (int) cartTableModel.getValueAt(selectedRow, 2);
    
        // Prompt user for new quantity
        String newQuantityStr = JOptionPane.showInputDialog(this, "Enter new quantity:", currentQuantity);
        if (newQuantityStr == null || newQuantityStr.isEmpty()) {
            return; // User canceled or entered empty input
        }
    
        try {
            int newQuantity = Integer.parseInt(newQuantityStr);
            if (newQuantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Update cart list with new quantity
            Product productToUpdate = null;
            for (Product cartProduct : cart) {
                if (cartProduct.getName().equals(name)) {
                    productToUpdate = cartProduct;
                    break;
                }
            }
    
            if (productToUpdate != null) {
                productToUpdate.setStock(newQuantity);
            }
    
            // Update cart table with new quantity
            cartTableModel.setValueAt(newQuantity, selectedRow, 2);
    
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity input. Please enter a number.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCartItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product in the cart to delete.",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Get the name of the product to delete
        String nameToDelete = (String) cartTableModel.getValueAt(selectedRow, 0);
    
        // Find the corresponding product in the cart list and remove it
        Product productToRemove = null;
        for (Product cartProduct : cart) {
            if (cartProduct.getName().equals(nameToDelete)) {
                productToRemove = cartProduct;
                break;
            }
        }
    
        if (productToRemove != null) {
            cart.remove(productToRemove);
            cartTableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Product deleted from cart successfully.",
                    "Delete Item", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Product not found in cart.",
                    "Delete Item", JOptionPane.ERROR_MESSAGE);
        }
    }
    
       
    private void deleteProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected product?", "Delete Product", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            inventory.remove(selectedRow);
            updateInventoryTable();
            saveInventoryToFile();
        }
    }
    
    private void addSampleData() {
        // Adding sample products to inventory
        inventory.add(new Product("Product 1", 10.0, 20));
        inventory.add(new Product("Product 2", 15.0, 15));
        inventory.add(new Product("Product 3", 20.0, 10));
    
        // Update inventory table with sample data
        updateInventoryTable();
    }    

    public void addToCart(JButton button) {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to cart.",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Get product details from selected row
        String name = (String) inventoryTableModel.getValueAt(selectedRow, 0);
        double price = (double) inventoryTableModel.getValueAt(selectedRow, 1);
        int stock = (int) inventoryTableModel.getValueAt(selectedRow, 2);
    
        // Prompt user for quantity
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (quantityStr == null || quantityStr.isEmpty()) {
            return; // User canceled or entered empty input
        }
    
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantity > stock) {
                JOptionPane.showMessageDialog(this, "Insufficient stock available.",
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Add product to cart
            Product product = new Product(name, price, quantity);
            cart.add(product);
    
            // Update cart table
            Object[] cartRowData = {name, price, quantity};
            cartTableModel.addRow(cartRowData);
    
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity input. Please enter a number.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkout() {
        // Calculate total and generate receipt
        double total = 0;
        StringBuilder receiptBuilder = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##");
    
        receiptBuilder.append("Receipt:\n\n");
        receiptBuilder.append(String.format("%-20s %-10s %-10s\n", "Name", "Price", "Quantity"));
        receiptBuilder.append("----------------------------------------------\n");
    
        // Calculate total and update inventory after confirming purchase
        List<Product> productsToRemove = new ArrayList<>();
        for (Product cartProduct : cart) {
            double lineTotal = cartProduct.getPrice() * cartProduct.getStock();
            receiptBuilder.append(String.format("%-20s %-10s %-10s\n", cartProduct.getName(),
                    df.format(cartProduct.getPrice()), cartProduct.getStock()));
            total += lineTotal;
    
            // Find the corresponding product in inventory to update stock
            for (Product inventoryProduct : inventory) {
                if (inventoryProduct.getName().equals(cartProduct.getName())) {
                    int newStock = inventoryProduct.getStock() - cartProduct.getStock();
                    inventoryProduct.setStock(newStock);
                    productsToRemove.add(cartProduct);
                    break;
                }
            }
        }
    
        // Remove products from cart after checkout
        cart.removeAll(productsToRemove);
    
        receiptBuilder.append("----------------------------------------------\n");
        receiptBuilder.append(String.format("Total: $%s\n", df.format(total)));
    
        // Display receipt in text area
        receiptTextArea.setText(receiptBuilder.toString());
    
        // Clear cart table and update UI
        cartTableModel.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Checkout completed successfully!", "Checkout",
                JOptionPane.INFORMATION_MESSAGE);
    
        // Update inventory table
        updateInventoryTable();
    
        // Save updated inventory to file
        saveInventoryToFile();
    }

    private void updateInventoryTable() {
        // Clear current inventory table model
        inventoryTableModel.setRowCount(0);
    
        // Populate inventory data into table
        for (Product product : inventory) {
            Object[] rowData = {product.getName(), product.getPrice(), product.getStock()};
            inventoryTableModel.addRow(rowData);
        }
    }     
    
    private void loadInventoryFromFile() {
        inventory.clear(); // Clear existing inventory data
        
        String filePath = System.getProperty("user.dir") + File.separator + INVENTORY_FILE;
        File inventoryFile = new File(filePath);
    
        if (!inventoryFile.exists()) {
            System.out.println("Inventory file not found. Adding sample data.");
            addSampleData();
            return;
        }
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    double price = Double.parseDouble(parts[1]);
                    int stock = Integer.parseInt(parts[2]);
                    inventory.add(new Product(name, price, stock));
                }
            }
            
            // Update inventory table
            updateInventoryTable();
            
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory from file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void saveInventoryToFile() {
        String filePath = System.getProperty("user.dir") + File.separator + INVENTORY_FILE;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Product product : inventory) {
                bw.write(product.getName() + "," + product.getPrice() + "," + product.getStock());
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving inventory to file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimplePOS();
            }
        });
    }
}
