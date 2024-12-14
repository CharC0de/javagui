package javagui.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DashboardWindow extends JFrame {
    private JTable productsTable;
    private List<CartItem> cart = new ArrayList<>();

    public DashboardWindow(int userId) {
        setTitle("Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Title Label
        JLabel lblTitle = new JLabel("Dashboard", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        getContentPane().add(lblTitle, BorderLayout.NORTH);

        // Product Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        productsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(productsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        getContentPane().add(tablePanel, BorderLayout.CENTER);

        // Sidebar Panel
        JPanel sidebarPanel = new JPanel(new GridBagLayout());
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Manage Button
        JButton btnManage = new JButton("Manage");
        GridBagConstraints gbcManage = new GridBagConstraints();
        gbcManage.fill = GridBagConstraints.HORIZONTAL;
        gbcManage.insets = new Insets(10, 0, 10, 0);
        gbcManage.gridx = 0;
        gbcManage.gridy = 0;
        sidebarPanel.add(btnManage, gbcManage);

        // Add Cart Button
        JButton btnCart = new JButton("Cart");
        GridBagConstraints gbcCart = new GridBagConstraints();
        gbcCart.fill = GridBagConstraints.HORIZONTAL;
        gbcCart.insets = new Insets(10, 0, 10, 0);
        gbcCart.gridx = 0;
        gbcCart.gridy = 1;
        sidebarPanel.add(btnCart, gbcCart);

        // Add History Button
        JButton btnHistory = new JButton("History");
        GridBagConstraints gbcHistory = new GridBagConstraints();
        gbcHistory.fill = GridBagConstraints.HORIZONTAL;
        gbcHistory.insets = new Insets(10, 0, 10, 0);
        gbcHistory.gridx = 0;
        gbcHistory.gridy = 2;
        sidebarPanel.add(btnHistory, gbcHistory);

        add(sidebarPanel, BorderLayout.EAST);

        // Load Product Data
        loadProducts(userId);

        // Button Action Listeners
        btnManage.addActionListener(e -> {
            this.dispose();
            new ManageProductsWindow(userId).setVisible(true);
        });

        btnCart.addActionListener(e -> showCartDialog(userId));
        btnHistory.addActionListener(e -> showHistoryDialog(userId));

        // Add double-click functionality to table rows to add product to cart
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addProductToCart();
                }
            }
        });
    }

    private void loadProducts(int userId) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, name, price, no_stock FROM products WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                float price = rs.getFloat("price");
                int noStock = rs.getInt("no_stock");
                model.addRow(new Object[]{id, name, price, noStock});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        productsTable.setModel(model);
    }

    private void addProductToCart() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to cart!");
            return;
        }

        try {
            int id = (int) productsTable.getValueAt(selectedRow, 0);
            String name = (String) productsTable.getValueAt(selectedRow, 1);
            float price = (float) productsTable.getValueAt(selectedRow, 2);

            String qtyInput = JOptionPane.showInputDialog(this, "Enter quantity:");
            if (qtyInput == null || qtyInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Quantity is required.");
                return;
            }

            int qty = Integer.parseInt(qtyInput);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }

            cart.add(new CartItem(id, name, price, qty));
            JOptionPane.showMessageDialog(this, "Product added to cart!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a valid number.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product to cart.");
        }
    }

    private void showCartDialog(int userId) {
        JDialog cartDialog = new JDialog(this, "Cart", true);
        cartDialog.setSize(500, 400);
        cartDialog.setLocationRelativeTo(this);
        cartDialog.setLayout(new BorderLayout());

        DefaultTableModel cartModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty"}, 0);
        for (CartItem item : cart) {
            cartModel.addRow(new Object[]{item.getId(), item.getName(), item.getPrice(), item.getQuantity()});
        }

        JTable cartTable = new JTable(cartModel);
        cartDialog.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnFinalize = new JButton("Finalize Purchase");
        btnPanel.add(btnDelete);
        btnPanel.add(btnFinalize);

        cartDialog.add(btnPanel, BorderLayout.SOUTH);

        btnDelete.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                cart.remove(selectedRow);
                cartModel.removeRow(selectedRow);
            }
        });

        btnFinalize.addActionListener(e -> finalizePurchase(userId, cartDialog));

        cartDialog.setVisible(true);
    }


    private void finalizePurchase(int userId, JDialog dialog) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            for (CartItem item : cart) {
                String checkStockQuery = "SELECT no_stock FROM products WHERE id = ?";
                PreparedStatement checkStockStmt = conn.prepareStatement(checkStockQuery);
                checkStockStmt.setInt(1, item.getId());
                ResultSet rs = checkStockStmt.executeQuery();

                if (rs.next()) {
                    int currentStock = rs.getInt("no_stock");
                    if (currentStock < item.getQuantity()) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Insufficient stock for product: " + item.getName());
                        conn.rollback();
                        return;
                    }
                }
            }

            String insertTransactionQuery = "INSERT INTO transactions (total) VALUES (?)";
            PreparedStatement transactionStmt = conn.prepareStatement(insertTransactionQuery, 
                                                                       PreparedStatement.RETURN_GENERATED_KEYS);
            float total = (float) cart.stream()
                                      .mapToDouble(item -> item.getPrice() * item.getQuantity())
                                      .sum();
            transactionStmt.setFloat(1, total);
            transactionStmt.executeUpdate();

            ResultSet transactionRs = transactionStmt.getGeneratedKeys();
            if (!transactionRs.next()) {
                JOptionPane.showMessageDialog(dialog, "Error creating transaction.");
                conn.rollback();
                return;
            }
            int transactionId = transactionRs.getInt(1);

            String insertProductBoughtQuery = 
                "INSERT INTO products_bought (transaction_id, product_id, qty) VALUES (?, ?, ?)";
            String updateStockQuery = "UPDATE products SET no_stock = no_stock - ? WHERE id = ?";
            
            PreparedStatement productBoughtStmt = conn.prepareStatement(insertProductBoughtQuery);
            PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
            
            for (CartItem item : cart) {
                productBoughtStmt.setInt(1, transactionId);
                productBoughtStmt.setInt(2, item.getId());
                productBoughtStmt.setInt(3, item.getQuantity());
                productBoughtStmt.addBatch();

                updateStockStmt.setInt(1, item.getQuantity());
                updateStockStmt.setInt(2, item.getId());
                updateStockStmt.addBatch();
            }
            productBoughtStmt.executeBatch();
            updateStockStmt.executeBatch();

            conn.commit();

            JOptionPane.showMessageDialog(dialog, "Purchase finalized successfully!");
            cart.clear();
            dialog.dispose();

            // Refresh the product table
            loadProducts(userId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error finalizing purchase! Rolling back changes.");
        }
    }

    private void showHistoryDialog(int userId) {
        JDialog historyDialog = new JDialog(this, "Transaction History", true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());

        DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Transaction ID", "Total Amount", "Product Name", "Quantity", "Price"}, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT t.id AS transaction_id, t.total, p.name AS product_name, " +
                           "pb.qty AS quantity, p.price AS price " +
                           "FROM transactions t " +
                           "JOIN products_bought pb ON t.id = pb.transaction_id " +
                           "JOIN products p ON pb.product_id = p.id " +
                           "WHERE p.user_id = ? " +
                           "ORDER BY t.id";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                float total = rs.getFloat("total");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                float price = rs.getFloat("price");

                historyModel.addRow(new Object[]{transactionId, total, productName, quantity, price});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transaction history.");
        }

        JTable historyTable = new JTable(historyModel);
        historyDialog.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> historyDialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnClose);
        historyDialog.add(btnPanel, BorderLayout.SOUTH);

        historyDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardWindow(1).setVisible(true));
    }
}

class CartItem {
    private int id;
    private String name;
    private float price;
    private int quantity;

    public CartItem(int id, String name, float price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
