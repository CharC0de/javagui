package javagui.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageProductsWindow extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete,btnBack;

    public ManageProductsWindow(int userId) {
        setTitle("Manage Products");
        setSize(600, 400);
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table Panel
        productTable = new JTable();
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0);
        productTable.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnBack = new JButton("Back");
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        buttonPanel.add(btnBack);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load Products
        loadProducts(userId);

        // Button Action Listeners
        btnAdd.addActionListener(e -> showAddProductDialog(userId));
        btnUpdate.addActionListener(e -> updateProduct(userId));
        btnDelete.addActionListener(e -> deleteProduct(userId));
        btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new DashboardWindow(userId).setVisible(true);
                dispose(); // Close the login window
            }
        });
    }

    private void loadProducts(int userId) {
        tableModel.setRowCount(0); // Clear existing rows
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, name, price, no_stock FROM products WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                float price = rs.getFloat("price");
                int stock = rs.getInt("no_stock");
                tableModel.addRow(new Object[]{id, name, price, stock});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddProductDialog(int userId) {
        // Create dialog
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        // Dialog input fields
        dialog.add(new JLabel("Name:"));
        JTextField txtName = new JTextField();
        dialog.add(txtName);

        dialog.add(new JLabel("Price:"));
        JTextField txtPrice = new JTextField();
        dialog.add(txtPrice);

        dialog.add(new JLabel("Stock:"));
        JTextField txtStock = new JTextField();
        dialog.add(txtStock);

        // Add Product button
        JButton btnAddProduct = new JButton("Add Product");
        dialog.add(btnAddProduct);

        // Cancel button
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnCancel);

        // Add button action
        btnAddProduct.addActionListener(e -> {
            String name = txtName.getText();
            String price = txtPrice.getText();
            String stock = txtStock.getText();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO products (user_id, name, price, no_stock) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setString(2, name);
                stmt.setFloat(3, Float.parseFloat(price));
                stmt.setInt(4, Integer.parseInt(stock));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                loadProducts(userId);
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error adding product!");
            }
        });

        // Cancel button action
        btnCancel.addActionListener(e -> dialog.dispose());

        // Show dialog
        dialog.setVisible(true);
    }

    private void updateProduct(int userId) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = JOptionPane.showInputDialog(this, "Enter new name:", tableModel.getValueAt(selectedRow, 1));
        String price = JOptionPane.showInputDialog(this, "Enter new price:", tableModel.getValueAt(selectedRow, 2));
        String stock = JOptionPane.showInputDialog(this, "Enter new stock:", tableModel.getValueAt(selectedRow, 3));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE products SET name = ?, price = ?, no_stock = ? WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setFloat(2, Float.parseFloat(price));
            stmt.setInt(3, Integer.parseInt(stock));
            stmt.setInt(4, id);
            stmt.setInt(5, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            loadProducts(userId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating product!");
        }
    }

    private void deleteProduct(int userId) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM products WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            loadProducts(userId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting product!");
        }
    }
}
