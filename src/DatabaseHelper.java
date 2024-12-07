
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class DatabaseHelper {
    
    // Variables to Connect to MySQL Server
    private String dbUrl; // DB URL
    private String dbUsername; // MySQL Username
    private String dbPassword; // MySQL Password
    
    private static String currentUrl = "jdbc:mysql://localhost:3306/csaaved1db";
    private static String currentUser = "root";
    private static String currentPassword = "password_here";

    // Intiializer
    public DatabaseHelper(String dbUrl, String dbUsername, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    // =====================================================================
    // =============== POPULATES HOME PAGE WITH PRODUCTS ===================
    // =====================================================================
    public void loadProducts(JPanel homePanel, JPanel homeWrapperPanel, JPanel wishlistPanel, Set<String> wishlistSet,
            JPanel ordersPanel, Set<String> orderSet, JFrame frame, String currentEmail) {
        System.out.println("Loading products with currentEmail: " + currentEmail); // Debug statement

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String sql = "SELECT p.product_name, p.price, c.category_type, p.product_id, p.current_supply " +
                    "FROM PRODUCT p " +
                    "LEFT JOIN CATEGORY c ON p.category_id = c.category_id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            homePanel.removeAll();
            homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));

            JPanel homeHeaderPanel = new JPanel(new GridLayout(1, 6)); // Adjusted to 6 columns
            homeHeaderPanel.add(new JLabel("<html><b>Product Name</b></html>"));
            homeHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
            homeHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
            homeHeaderPanel.add(new JLabel("<html><b>Stock Qty</b></html>")); // New column header
            homeHeaderPanel.add(new JLabel(""));
            homeHeaderPanel.add(new JLabel(""));
            homeHeaderPanel.setPreferredSize(new Dimension(800, 60));
            homeHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

            homeWrapperPanel.removeAll(); // Ensure wrapper panel is cleared
            homeWrapperPanel.setLayout(new BorderLayout());
            homeWrapperPanel.add(homeHeaderPanel, BorderLayout.NORTH);
            homeWrapperPanel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

            int productCount = 0; // Debug: Track number of products loaded
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String category = rs.getString("category_type");
                double price = rs.getDouble("price");
                String productId = rs.getString("product_id");
                int currentSupply = rs.getInt("current_supply"); // Fetch current supply

                JPanel productPanel = new JPanel(new GridLayout(1, 6)); // Adjusted to 6 columns
                productPanel.setPreferredSize(new Dimension(800, 30));
                JLabel productNameLabel = new JLabel("  " + productName);
                productPanel.add(productNameLabel);
                productPanel.add(new JLabel(category));
                productPanel.add(new JLabel("$" + price));
                productPanel.add(new JLabel(String.valueOf(currentSupply))); // Display current supply
                JButton buyButton = new JButton("Buy");
                buyButton.setPreferredSize(new Dimension(80, 30));
                JButton wishlistButton = new JButton("Wishlist");
                wishlistButton.setPreferredSize(new Dimension(80, 30));

                // =============== WISHLIST BUTTON LISTENER ===================
                wishlistButton.addActionListener(e -> {
                    DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);
                    if (dbHelper.isProductInWishlist(productId, currentEmail)) {
                        JOptionPane.showMessageDialog(frame, "Already in Wishlist");
                    } else {
                        wishlistSet.add(productId);
                        addToWishlist(wishlistPanel, wishlistSet, productId, productName, category, price,
                                currentSupply, currentEmail);
                        JOptionPane.showMessageDialog(frame, "Added to Wishlist");
                    }
                });
                // =============== BUY BUTTON LISTENER ===================
                buyButton.addActionListener(e -> {
                    System.out.println("Buy button clicked, currentEmail: " + currentEmail); // Debug statement

                    // Check if the product is sold out
                    if (isProductSoldOut(productId)) {
                        JOptionPane.showMessageDialog(frame, "Sorry, this product is sold out!");
                    } else {
                        // Fetch supplier and customer address
                        String supplierName = getSupplierNameByProductId(productId); // Retrieve actual supplier name
                        String customerAddress = getCustomerAddressByEmail(currentEmail); // Retrieve actual customer

                        // Debug statements
                        System.out.println("Supplier Name: " + supplierName);
                        System.out.println("Customer Address: " + customerAddress);

                        // Add the order to the database
                        DatabaseHelper.addToOrders(ordersPanel, orderSet, productId, productName, category, price,
                                supplierName, customerAddress, currentEmail, homePanel, homeWrapperPanel, wishlistPanel,
                                wishlistSet, frame);

                        // Decrement the product supply
                        decrementProductSupply(productId);
                        JOptionPane.showMessageDialog(frame, "Added to Orders");

                        // Schedule a refresh of the products panel after 1-2 seconds
                        Timer timer = new Timer(2000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Main.refreshProductsPanel(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet,
                                        ordersPanel, orderSet, frame);
                            }
                        });
                        timer.setRepeats(false); // Ensure timer only runs once
                        timer.start();
                    }
                });

                productPanel.add(buyButton);
                productPanel.add(wishlistButton);
                homePanel.add(productPanel);

                productCount++; // Increment product count
            }
            System.out.println("Total products loaded: " + productCount); // TEST STATEMENT
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Ensure to revalidate and repaint after all components are added
        homePanel.revalidate();
        homePanel.repaint();
        homeWrapperPanel.revalidate();
        homeWrapperPanel.repaint();
        frame.revalidate();
        frame.repaint();
    }

    // =====================================================================
    // ============ CUSTOMER BUY PRODUCT (ORDER) FUNCTIONALITY =============
    // =====================================================================
    public static void addToOrders(JPanel orderPanel, Set<String> orderSet, String id, String name, String category,
            double price, String supplierName, String customerAddress, String currentEmail,
            JPanel homePanel, JPanel homeWrapperPanel, JPanel wishlistPanel, Set<String> wishlistSet, JFrame frame) {

        DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);

        // Generate unique order number
        String orderNo = UUID.randomUUID().toString();

        // Calculate tax and total
        Double tax = calculateSalesTax(price);
        Double total = price + tax;
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedTotalPrice = df.format(total);
        String formattedTax = df.format(tax);
        String qtyPurchased = "1"; // Since Customer Can Only Buy 1 at a Time
        String warehouseAddress = dbHelper.getRandomWarehouseId(); // Use Random Warehouse from DB
        String outpostAddress = "TBD"; // Since Order goes to Warehouse Before Outpost
        LocalDate estimatedDeliveryDate = LocalDate.now().plusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String estimatedDelivery = estimatedDeliveryDate.format(formatter);

        // Product Row UI
        JPanel orderProductPanel = new JPanel(new GridLayout(6, 4, 0, 0)); // Set horizontal and vertical gaps to 0

        // Row 1
        orderProductPanel.add(createLabel(name));
        orderProductPanel.add(createLabel("<html><b>Order Number:</b></html>"));
        orderProductPanel.add(createLabel("<html><b>Shipping Address:</b></html>"));
        orderProductPanel.add(createLabel("<html><b>Estimated Delivery Date:</b></html>"));

        // Row 2
        orderProductPanel.add(createLabel(category));
        orderProductPanel.add(createLabel(orderNo)); // Use orderNo here instead of id
        orderProductPanel.add(createLabel(customerAddress));
        orderProductPanel.add(createLabel(estimatedDelivery));

        // Row 3
        orderProductPanel.add(createLabel("<html><b>Total Price:</b> " + formattedTotalPrice + "</html>"));
        orderProductPanel.add(createLabel("<html><b>Supplier:</b></html>"));
        orderProductPanel.add(createLabel("<html><b>Warehouse Destination:</b></html>"));
        orderProductPanel.add(createLabel(""));

        // Row 4
        orderProductPanel.add(createLabel("<html><b>Tax:</b> " + formattedTax + "</html>"));
        orderProductPanel.add(createLabel(supplierName));
        orderProductPanel.add(createLabel(warehouseAddress));
        orderProductPanel.add(createLabel(""));

        // Row 5
        orderProductPanel.add(createLabel("<html><b>Qty:</b> " + qtyPurchased + "</html>"));
        orderProductPanel.add(createLabel(""));
        orderProductPanel.add(createLabel("<html><b>Outpost Address:</b></html>"));
        orderProductPanel.add(createLabel(""));

        // Row 6
        orderProductPanel.add(createLabel(""));
        orderProductPanel.add(createLabel(""));
        orderProductPanel.add(createLabel(outpostAddress));
        JLabel cancelOrderLabel = new JLabel("<html><font color='red'><u>Cancel Order</u></font></html>");
        cancelOrderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelOrderLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                orderPanel.remove(orderProductPanel);
                orderSet.remove(orderNo); // Use orderNo here
                deleteOrderItem(orderNo, currentEmail, homePanel, homeWrapperPanel, wishlistPanel, wishlistSet,
                        orderPanel,
                        orderSet, frame);
                orderPanel.revalidate();
                orderPanel.repaint();
            }
        });
        orderProductPanel.add(cancelOrderLabel);

        // Add a black line border to the bottom row
        orderProductPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));

        // Add the product row panel to the orders panel
        orderPanel.add(orderProductPanel);

        // Debug statement
        System.out.println("Added order: " + orderNo + " for customer: " + currentEmail);

        // Refresh the order panel
        orderPanel.revalidate();
        orderPanel.repaint();

        // Add the order to the database with the correct orderNo
        addOrderToDB(dbHelper, orderNo, id, name, category, price, tax, total, currentEmail);
    }
    // End of Add Order to DB Functionality

    private static boolean addOrderToDB(DatabaseHelper dbHelper, String orderNo, String productId, String name,
            String category, double price, double salesTax, double total, String currentEmail) {
        try (Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword)) {
            String insertOrderSql = "INSERT INTO ORDERS (order_no, item_id, total, sales_tax, total_pretax, customer_id) VALUES (?, ?, ?, ?, ?, (SELECT customer_id FROM CUSTOMER WHERE email = ?))";
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql)) {
                double totalPretax = price;
                stmt.setString(1, orderNo);
                stmt.setString(2, productId);
                stmt.setDouble(3, total);
                stmt.setDouble(4, salesTax);
                stmt.setDouble(5, totalPretax);
                stmt.setString(6, currentEmail);

                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Double calculateSalesTax(double price) {
        // Implement your sales tax calculation logic here
        return price * 0.1; // Example: 10% sales tax
    }

    private static String generateOrderNumber() {
        // Implement your order number generation logic here
        return UUID.randomUUID().toString(); // Example: Use UUID for order number
    }

    // =====================================================================
    // =============== DELETE ORDER FROM DATABASE ==========================
    // =====================================================================
    private static void deleteOrderItem(String orderId, String email, JPanel homePanel, JPanel homeWrapperPanel,
            JPanel wishlistPanel, Set<String> wishlistSet, JPanel ordersPanel, Set<String> orderSet, JFrame frame) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);

            // Set transaction isolation level
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            // Start transaction
            conn.setAutoCommit(false);

            // Debug statement: Before fetching product_id
            System.out.println("Starting deleteOrderItem...");
            System.out.println("Order ID: " + orderId); // Ensure this is the correct order number
            System.out.println("Customer Email: " + email);

            // First, get the product_id from the order
            String getProductSql = "SELECT item_id FROM ORDERS WHERE order_no = ? AND customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
            stmt = conn.prepareStatement(getProductSql);
            stmt.setString(1, orderId);
            stmt.setString(2, email);

            // Debug statement: Before executing query
            System.out.println("Executing query to fetch product_id...");
            rs = stmt.executeQuery();

            if (rs.next()) {
                String productId = rs.getString("item_id");

                // Debug statements: After fetching product_id
                System.out.println("Fetched product_id: " + productId);
                System.out.println("Attempting to delete order...");

                // Delete the order from the ORDERS table
                String deleteOrderSql = "DELETE FROM ORDERS WHERE order_no = ? AND customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteOrderSql);
                deleteStmt.setString(1, orderId);
                deleteStmt.setString(2, email);
                int rowsDeleted = deleteStmt.executeUpdate();

                // Debug statement: After deleting order
                System.out.println("Rows deleted: " + rowsDeleted);

                if (rowsDeleted > 0) {
                    // Increment the current_supply of the product
                    String incrementSupplySql = "UPDATE PRODUCT SET current_supply = current_supply + 1 WHERE product_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(incrementSupplySql);
                    updateStmt.setString(1, productId);
                    int rowsUpdated = updateStmt.executeUpdate();

                    // Debug statement: After incrementing supply
                    System.out.println("Rows updated (increment supply): " + rowsUpdated);

                    // Commit transaction
                    conn.commit();

                    // Debug statement: After committing transaction
                    System.out.println("Order " + orderId + " deleted and product " + productId
                            + " stock incremented for customer " + email);

                    // Refresh the products panel after the database update
                    Main.refreshProductsPanel(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, ordersPanel,
                            orderSet, frame);
                } else {
                    System.out.println("Order not found after attempting deletion.");
                    conn.rollback(); // Rollback transaction if order not found
                }
            } else {
                System.out.println("Order not found when fetching product_id.");
                conn.rollback(); // Rollback transaction if order not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit to true
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================================
    // ============== UPDATES PRE-EXISTING ORDERS TO ORDER PANEL ===========
    // =====================================================================
    public List<Order> getOrdersFromCustomer(String email) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT p.product_name, cat.category_type, o.total, o.sales_tax, 1 AS qty, o.order_no, sup.supplier_name, "
                +
                "cust.address AS customer_address, wh.address AS warehouse_address " +
                "FROM ORDERS o " +
                "JOIN PRODUCT p ON o.item_id = p.product_id " +
                "JOIN CATEGORY cat ON p.category_id = cat.category_id " +
                "JOIN SUPPLIER sup ON p.supplier_id = sup.supplier_id " +
                "JOIN CUSTOMER cust ON o.customer_id = cust.customer_id " +
                "LEFT JOIN WAREHOUSE wh ON o.item_id = wh.warehouse_id " +
                "WHERE o.customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";

        System.out.println("Executing query: " + query);
        System.out.println("With parameter email: " + email);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            System.out.println("Executing query for email: " + email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Record found in ResultSet.");
                    // Calculate estimated delivery date as 7 days from now
                    LocalDate estimatedDeliveryDate = LocalDate.now().plusDays(7);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String formattedEstimatedDeliveryDate = estimatedDeliveryDate.format(formatter);

                    // Set outpost address to "TBD"
                    String outpostAddress = "TBD";

                    // Use getRandomWarehouseId to get a random warehouse ID
                    String warehouseAddress = getRandomWarehouseId();

                    Order order = new Order(
                            rs.getString("product_name"),
                            rs.getString("category_type"),
                            rs.getDouble("total"),
                            rs.getDouble("sales_tax"),
                            rs.getInt("qty"),
                            rs.getString("order_no"),
                            rs.getString("supplier_name"),
                            rs.getString("customer_address"),
                            warehouseAddress,
                            outpostAddress,
                            formattedEstimatedDeliveryDate);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // =====================================================================
    // =============== DECREMENT CURRENT SUPPLY FUNCTION ===================
    // =====================================================================
    public static void decrementProductSupply(String productId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
            String sql = "UPDATE PRODUCT SET current_supply = current_supply - 1 WHERE product_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.executeUpdate();

            // Debug statement
            System.out.println("Decremented current supply for product_id: " + productId);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================================
    // ====== UPDATES PRE-EXISTING WISHLIST ITEMS TO WISHLIST PANEL ========
    // =====================================================================
    public static void loadWishlist(JPanel wishlistPanel, Set<String> wishlistSet, String currentEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);

            // Corrected SQL query to match the new schema
            String sql = "SELECT p.product_name, p.price, c.category_type, p.product_id, p.current_supply " +
                    "FROM WISHLIST w " +
                    "JOIN PRODUCT p ON w.product_id = p.product_id " +
                    "LEFT JOIN CATEGORY c ON p.category_id = c.category_id " + // Updated join condition
                    "WHERE w.customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentEmail);
            rs = stmt.executeQuery();

            wishlistPanel.removeAll();
            wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

            while (rs.next()) {
                String productName = rs.getString("product_name");
                String category = rs.getString("category_type");
                double price = rs.getDouble("price");
                String productId = rs.getString("product_id");
                int currentSupply = rs.getInt("current_supply"); // Fetch current supply
                System.out.println(currentSupply);

                // Add product to the wishlist panel
                addToWishlist(wishlistPanel, wishlistSet, productId, productName, category, price, currentSupply,
                        currentEmail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        wishlistPanel.revalidate();
        wishlistPanel.repaint();
    }

    // =====================================================================
    // =============== VERIFIES FOR NO DUPLICATES WISHLIST ITEMS ===========
    // =====================================================================
    public boolean isProductInWishlist(String productId, String email) {
        boolean exists = false;
        String query = "SELECT 1 FROM WISHLIST w " +
                "JOIN CUSTOMER c ON w.customer_id = c.customer_id " +
                "WHERE w.product_id = ? AND c.email = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productId);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    // =====================================================================
    // =============== CUSTOMER ADDS PRODUCT TO WISHLIST ===================
    // =====================================================================
    public static void addToWishlist(JPanel wishlistPanel, Set<String> wishlistSet, String id, String name,
            String category, double price, int stockQty, String email) {
        // Existing code to add product to the panel
        JPanel wishlistProductPanel = new JPanel(new GridLayout(1, 5)); // Adjusted to 5 columns
        wishlistProductPanel.setPreferredSize(new Dimension(800, 30)); // Set fixed height for product panels
        wishlistProductPanel.add(new JLabel("  " + name));
        wishlistProductPanel.add(new JLabel(category));
        wishlistProductPanel.add(new JLabel(String.valueOf(stockQty))); // Display stock quantity
        wishlistProductPanel.add(new JLabel("$" + price));

        // Remove Product from Wishlist Functionality
        JLabel removeLabel = new JLabel("<html><font color='red'><u>Remove</u></font></html>");
        removeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wishlistPanel.remove(wishlistProductPanel);
                wishlistSet.remove(id);
                deleteWishlistItem(id, email); // Call function to remove from database
                wishlistPanel.revalidate();
                wishlistPanel.repaint();
            }
        });

        wishlistProductPanel.add(removeLabel);
        wishlistPanel.add(wishlistProductPanel);

        // Add to wishlist set and database
        wishlistSet.add(id);
        insertWishlistItem(id, name, category, price, stockQty, email);

        wishlistPanel.revalidate();
        wishlistPanel.repaint();
    }

    // =====================================================================
    // =============== INSERTS WISHLIST INTO DATABASE =====================
    // =====================================================================
    private static void insertWishlistItem(String productId, String name, String category,
            double price, int stockQty, String email) {
        try (Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO WISHLIST (wishlist_id, customer_id, product_id) VALUES (?, ?, ?)")) { // Excluded
                                                                                                           // stock_qty
            String wishlistId = UUID.randomUUID().toString(); // Generate a unique wishlist ID
            String customerId = getCustomerIdByEmail(email); // Implement this method to get current customer ID based
                                                             // on the session or email

            stmt.setString(1, wishlistId);
            stmt.setString(2, customerId);
            stmt.setString(3, productId);
            // stmt.setInt(4, stockQty); // Removed setting stockQty since it's not in the
            // query
            stmt.executeUpdate();

            // Debug statement
            System.out.println("Wishlist item inserted for product ID: " + productId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // =============== DELETES WISHLIST FROM THE DATABASE ==================
    // =====================================================================
    private static void deleteWishlistItem(String productId, String email) {
        try (Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
                PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM WISHLIST WHERE product_id = ? AND customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)")) {
            stmt.setString(1, productId);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // =============== LOG-IN AND REGISTER FUNCTIONALITY ===================
    // =====================================================================
    public static class LoginDialog extends JDialog {

        private JTextField emailField;
        private JButton loginButton;
        private JButton registerButton;
        private Connection conn;
        private Main.AuthCallback authCallback; // Add callback

        // =============== LOG-IN FUNCTIONALITY ===================
        public LoginDialog(Frame parent, Main.AuthCallback callback) {
            super(parent, "Login/Register", true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevent closing
            setLayout(new BorderLayout());
            this.authCallback = callback; // Initialize callback

            // Database Connection
            try {
                conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Initialize login panel
            initializeLoginPanel(parent);

            // Sets dimensions of the Log-In window
            setPreferredSize(new Dimension(300, 125));

            setLocationRelativeTo(parent);
            pack();
            setVisible(true);
        }

        // =============== DEFINES LOG-IN WINDOW ===================
        private void initializeLoginPanel(Frame parent) {
            getContentPane().removeAll();
            setLayout(new BorderLayout());

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Sign in using your Email:"));
            emailField = new JTextField();
            panel.add(emailField);

            loginButton = new JButton("Login");
            registerButton = new JButton("Register");

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
            buttonPanel.add(loginButton);
            buttonPanel.add(registerButton);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // LOG-IN BUTTON LISTENER
            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleLogin();
                }
            });

            // REGISTER BUTTON LISTENER
            // Switches to Register Window
            registerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchToRegister(parent);
                }
            });
            pack();
            revalidate();
            repaint();
        } // END OF LOG-IN WINDOW DEFINITION

        // =============== LOG-IN VALIDATION ===================
        private void handleLogin() {
            String email = emailField.getText();

            try {
                String sql = "SELECT * FROM CUSTOMER WHERE email = ?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, email);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    authCallback.onAuthSuccess(email); // Use callback
                    System.out.println("Login successful for email: " + email); // Debug statement
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose(); // Close the dialog
                } else {
                    System.out.println("Invalid email: " + email); // Debug statement
                    JOptionPane.showMessageDialog(this, "Invalid email.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // =============== DEFINES REGISTER WINDOW ===================
        private void switchToRegister(Frame parent) {
            getContentPane().removeAll();
            setLayout(new BorderLayout());

            JPanel panel = new JPanel(new GridLayout(9, 2));
            panel.add(new JLabel("  First Name:"));
            JTextField firstNameField = new JTextField();
            panel.add(firstNameField);

            panel.add(new JLabel("  Last Name:"));
            JTextField lastNameField = new JTextField();
            panel.add(lastNameField);

            panel.add(new JLabel("  Phone No.:"));
            JTextField phoneField = new JTextField();
            panel.add(phoneField);

            panel.add(new JLabel("  City:"));
            JTextField cityField = new JTextField();
            panel.add(cityField);

            panel.add(new JLabel("  State:"));
            JTextField stateField = new JTextField();
            panel.add(stateField);

            panel.add(new JLabel("  Street Address:"));
            JTextField streetField = new JTextField();
            panel.add(streetField);

            panel.add(new JLabel("  Zip Code:"));
            JTextField zipField = new JTextField();
            panel.add(zipField);

            panel.add(new JLabel("  Email:"));
            JTextField registerEmailField = new JTextField();
            panel.add(registerEmailField);

            JButton backButton = new JButton("Back");
            JButton submitButton = new JButton("Submit");

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
            buttonPanel.add(backButton);
            buttonPanel.add(submitButton);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // BACK BUTTON LISTENER
            // Navigate back to Register
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // Sets Dimension of the Register Window
                    setPreferredSize(new Dimension(300, 125));
                    initializeLoginPanel(parent);
                }
            });

            // SUBMIT BUTTON LISTENER
            // Registration Functionality
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String customerName = firstName + " " + lastName;
                    String phone = phoneField.getText();
                    String city = cityField.getText();
                    String state = stateField.getText();
                    String street = streetField.getText();
                    String zip = zipField.getText();
                    String email = registerEmailField.getText();
                    String address = street + ", " + city + ", " + state + " " + zip;

                    try {
                        String sql = "INSERT INTO CUSTOMER (customer_id, email, customer_name, fname, lname, phone_no, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement statement = conn.prepareStatement(sql);
                        statement.setString(1, java.util.UUID.randomUUID().toString());
                        statement.setString(2, email);
                        statement.setString(3, customerName);
                        statement.setString(4, firstName);
                        statement.setString(5, lastName);
                        statement.setString(6, phone);
                        statement.setString(7, address);
                        statement.executeUpdate();

                        authCallback.onAuthSuccess(email); // Use callback
                        System.out.println("Registration successful for email: " + email); // Debug statement
                        JOptionPane.showMessageDialog(LoginDialog.this, "Registration successful!");
                        dispose(); // Close the dialog
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(LoginDialog.this, "Error registering user.");
                    }
                }
            });

            // Sets Dimension of the Register Window
            setPreferredSize(new Dimension(500, 400));

            pack();
            revalidate();
            repaint();
        }// END OF REGISTER WINDOW
    } // END OF LOG-IN AND REGISTER CLASS

    // =====================================================================
    // ====================== CUSTOMER DATA GETTER =========================
    // =====================================================================
    public Customer getCustomerInfo(String email) {
        Customer customer = null;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CUSTOMER WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customer = new Customer(
                            rs.getString("fname"),
                            rs.getString("lname"),
                            rs.getString("address"),
                            rs.getString("email"),
                            rs.getString("phone_no"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }

    // =====================================================================
    // ================= ACCOUNT DELETION FUNCTIONALITY ====================
    // =====================================================================
    public void deleteAccount(String email) {
        String getOrdersQuery = "SELECT item_id FROM ORDERS WHERE customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
        String deleteWishlistQuery = "DELETE FROM WISHLIST WHERE customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
        String deleteOrdersQuery = "DELETE FROM ORDERS WHERE customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
        String deleteCustomerQuery = "DELETE FROM CUSTOMER WHERE email = ?";
        String incrementProductSupplyQuery = "UPDATE PRODUCT SET current_supply = current_supply + 1 WHERE product_id = ?";
    
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            conn.setAutoCommit(false); // Start transaction
    
            try (PreparedStatement getOrdersStmt = conn.prepareStatement(getOrdersQuery);
                 PreparedStatement deleteWishlistStmt = conn.prepareStatement(deleteWishlistQuery);
                 PreparedStatement deleteOrdersStmt = conn.prepareStatement(deleteOrdersQuery);
                 PreparedStatement incrementProductSupplyStmt = conn.prepareStatement(incrementProductSupplyQuery);
                 PreparedStatement deleteCustomerStmt = conn.prepareStatement(deleteCustomerQuery)) {
    
                // Get the customer's orders
                getOrdersStmt.setString(1, email);
                try (ResultSet rs = getOrdersStmt.executeQuery()) {
                    while (rs.next()) {
                        String productId = rs.getString("item_id");
                        // Increment the current_supply of each product in the orders
                        incrementProductSupplyStmt.setString(1, productId);
                        incrementProductSupplyStmt.executeUpdate();
                    }
                }
    
                // Delete related wishlist entries
                deleteWishlistStmt.setString(1, email);
                deleteWishlistStmt.executeUpdate();
    
                // Delete related orders
                deleteOrdersStmt.setString(1, email);
                deleteOrdersStmt.executeUpdate();
    
                // Delete the customer record
                deleteCustomerStmt.setString(1, email);
                int affectedRows = deleteCustomerStmt.executeUpdate();
    
                conn.commit(); // Commit transaction
    
                if (affectedRows > 0) {
                    System.out.println("Account deleted successfully.");
                } else {
                    System.out.println("No account found with the given email.");
                }
    
                // Exit the application
                System.out.println("Closing application...");
                System.exit(0);
    
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction if any error occurs
                e.printStackTrace();
                System.out.println("An error occurred while deleting the account.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while establishing the database connection.");
        }
    }
    
    

    // =====================================================================
    // ============== DEFINES PRODUCT ROW UI FOR ORDER PAGE ================
    // =====================================================================
    public static JPanel createProductRowPanel(
            String productName, String categoryType, String totalPrice, String tax, String qtyPurchased,
            String orderNo, String supplierName, String customerAddress, String warehouseAddress,
            String outpostAddress, String estimatedDelivery,
            JPanel ordersPanel, Set<String> orderSet, JPanel homePanel, JPanel homeWrapperPanel, JPanel wishlistPanel,
            Set<String> wishlistSet, JFrame frame, String email) {

        // Format the totalPrice and tax to 2 decimal places
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedTotalPrice = df.format(Double.parseDouble(totalPrice));
        String formattedTax = df.format(Double.parseDouble(tax));

        JPanel productRowPanel = new JPanel();
        productRowPanel.setLayout(new GridLayout(6, 4, 0, 0)); // Set horizontal and vertical gaps to 0

        // Row 1
        productRowPanel.add(createLabel(productName));
        productRowPanel.add(createLabel("<html><b>Order Number:</b></html>"));
        productRowPanel.add(createLabel("<html><b>Shipping Address:</b></html>"));
        productRowPanel.add(createLabel("<html><b>Estimated Delivery Date:</b></html>"));

        // Row 2
        productRowPanel.add(createLabel(categoryType));
        productRowPanel.add(createLabel(orderNo));
        productRowPanel.add(createLabel(customerAddress));
        productRowPanel.add(createLabel(estimatedDelivery));

        // Row 3
        productRowPanel.add(createLabel("<html><b>Total Price:</b> " + formattedTotalPrice + "</html>"));
        productRowPanel.add(createLabel("<html><b>Supplier:</b></html>"));
        productRowPanel.add(createLabel("<html><b>Warehouse Destination:</b></html>"));
        productRowPanel.add(createLabel(""));

        // Row 4
        productRowPanel.add(createLabel("<html><b>Tax:</b> " + formattedTax + "</html>"));
        productRowPanel.add(createLabel(supplierName));
        productRowPanel.add(createLabel(warehouseAddress));
        productRowPanel.add(createLabel(""));

        // Row 5
        productRowPanel.add(createLabel("<html><b>Qty:</b> " + qtyPurchased + "</html>"));
        productRowPanel.add(createLabel(""));
        productRowPanel.add(createLabel("<html><b>Outpost Address:</b></html>"));
        productRowPanel.add(createLabel(""));

        // Row 6
        productRowPanel.add(createLabel(""));
        productRowPanel.add(createLabel(""));
        productRowPanel.add(createLabel(outpostAddress));
        JLabel cancelOrderLabel = new JLabel("<html><font color='red'><u>Cancel Order</u></font></html>");
        cancelOrderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelOrderLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ordersPanel.remove(productRowPanel);
                orderSet.remove(orderNo);
                DatabaseHelper.deleteOrderItem(orderNo, email, homePanel, homeWrapperPanel, wishlistPanel, wishlistSet,
                        ordersPanel, orderSet, frame);
                ordersPanel.revalidate();
                ordersPanel.repaint();
            }
        });
        productRowPanel.add(cancelOrderLabel);

        // Add a black line border to the bottom
        productRowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));

        return productRowPanel;
    }

    // =====================================================================
    // ============= RANDOM WAREHOUSE GETTER FUNCTIONALITY =================
    // =====================================================================
    public String getRandomWarehouseId() {
        List<String> warehouseIds = new ArrayList<>();
        String query = "SELECT warehouse_id FROM WAREHOUSE";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                warehouseIds.add(rs.getString("warehouse_id"));
            }

            if (!warehouseIds.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(warehouseIds.size());
                return warehouseIds.get(randomIndex);
            } else {
                System.out.println("No warehouse IDs found in the database.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =====================================================================
    // ===================== PRODUCT SUPPLIER GETTER =======================
    // =====================================================================
    public String getSupplierNameByProductId(String productId) {
        String supplierName = "";
        String query = "SELECT s.supplier_name FROM SUPPLIER s " +
                "JOIN PRODUCT p ON s.supplier_id = p.supplier_id " +
                "WHERE p.product_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    supplierName = rs.getString("supplier_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return supplierName;
    }

    // =====================================================================
    // ===================== CUSTOMER ADDRESS GETTER =======================
    // =====================================================================
    public String getCustomerAddressByEmail(String email) {
        String address = "";
        String query = "SELECT address FROM CUSTOMER WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    address = rs.getString("address");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static String getCustomerIdByEmail(String email) {
        String customerId = "";
        String query = "SELECT customer_id FROM CUSTOMER WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getString("customer_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerId;
    }

    public static String getCustomerEmailById(String customerId) {
        String email = "";
        String query = "SELECT email FROM CUSTOMER WHERE customer_id = ?";

        try (Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return email;
    }

    // =====================================================================
    // =============== CHECK IF PRODUCT IS SOLD OUT =======================
    // =====================================================================
    public static boolean isProductSoldOut(String productId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isSoldOut = false;

        try {
            conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
            String sql = "SELECT current_supply FROM PRODUCT WHERE product_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int currentSupply = rs.getInt("current_supply");
                isSoldOut = (currentSupply == 0); // Check if current supply is zero
            }

            // Debug statement
            System.out.println("Product ID: " + productId + " is sold out: " + isSoldOut);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return isSoldOut;
    }

    // Creates Label for Product UIs
    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return label;
    }

    // =============== DEFINES CUSTOMER ===================
    public class Customer {
        private String firstName;
        private String lastName;
        private String address;
        private String email;
        private String phoneNo;

        // Customer Initializer
        public Customer(String firstName, String lastName, String address, String email, String phoneNo) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.email = email;
            this.phoneNo = phoneNo;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getAddress() {
            return address;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNo() {
            return phoneNo;
        }
    } // END OF CUSTOMER CLASS

    // =============== DEFINES ORDER ===================
    public class Order {
        private String productName;
        private String categoryType;
        private double totalPrice;
        private double salesTax;
        private int quantityPurchased;
        private String orderNumber;
        private String supplierName;
        private String customerAddress;
        private String warehouseAddress;
        private String outpostAddress;
        private String estimatedDeliveryDate;

        // Constructor
        public Order(String productName, String categoryType, double totalPrice, double salesTax, int quantityPurchased,
                String orderNumber, String supplierName, String customerAddress, String warehouseAddress,
                String outpostAddress, String estimatedDeliveryDate) {
            this.productName = productName;
            this.categoryType = categoryType;
            this.totalPrice = totalPrice;
            this.salesTax = salesTax;
            this.quantityPurchased = quantityPurchased;
            this.orderNumber = orderNumber;
            this.supplierName = supplierName;
            this.customerAddress = customerAddress;
            this.warehouseAddress = warehouseAddress;
            this.outpostAddress = outpostAddress;
            this.estimatedDeliveryDate = estimatedDeliveryDate;
        }

        // Getters
        public String getProductName() {
            return productName;
        }

        public String getCategoryType() {
            return categoryType;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public double getSalesTax() {
            return salesTax;
        }

        public int getQuantityPurchased() {
            return quantityPurchased;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public String getCustomerAddress() {
            return customerAddress;
        }

        public String getWarehouseAddress() {
            return warehouseAddress;
        }

        public String getOutpostAddress() {
            return outpostAddress;
        }

        public String getEstimatedDeliveryDate() {
            return estimatedDeliveryDate;
        }
    } // END OF ORDERS CLASS
}
