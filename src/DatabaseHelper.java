
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseHelper {

    // Variables to Connect to MySQL Server
    private String dbUrl; // DB URL
    private String dbUsername; // MySQL Username
    private String dbPassword; // MySQL Password

    private static String currentUrl = "jdbc:mysql://localhost:3306/csaaved1db";
    private static String currentUser = "root";
    private static String currentPassword = "passord_here";

    // Intiializer
    public DatabaseHelper(String dbUrl, String dbUsername, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    // ==============================================
    // ===== POPULATE HOME PAGE W/ PRODUCTS =========
    // ==============================================
    public void loadProducts(JPanel homePanel, JPanel homeWrapperPanel, JPanel wishlistPanel, Set<String> wishlistSet,
            JPanel ordersPanel, Set<String> orderSet, JFrame frame) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connect to your database
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            // SQL query to get product details
            String sql = "SELECT p.product_name, p.price, c.category_type, p.product_id FROM PRODUCT p LEFT JOIN CATEGORY c ON p.product_id = c.item_id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            homePanel.removeAll();
            homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS)); // Use BoxLayout for fixed height rows

            // Add headers
            JPanel homeHeaderPanel = new JPanel(new GridLayout(1, 6));
            homeHeaderPanel.add(new JLabel("<html><b>Product Name</b></html>"));
            homeHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
            homeHeaderPanel.add(new JLabel("<html><b>Description</b></html>")); // Placeholder for product description
            homeHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
            homeHeaderPanel.add(new JLabel("")); // Placeholder for buy button column
            homeHeaderPanel.add(new JLabel("")); // Placeholder for wishlist button column
            homeHeaderPanel.setPreferredSize(new Dimension(800, 60)); // Set fixed height for header
            homeHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

            // Add headers to the home wrapper panel
            homeWrapperPanel.setLayout(new BorderLayout());
            homeWrapperPanel.add(homeHeaderPanel, BorderLayout.NORTH);
            homeWrapperPanel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

            // Add products dynamically
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String category = rs.getString("category_type");
                String description = "Sample Description"; // Placeholder for actual product description if available
                double price = rs.getDouble("price");
                String productId = rs.getString("product_id");

                JPanel productPanel = new JPanel(new GridLayout(1, 6));
                productPanel.setPreferredSize(new Dimension(800, 30)); // Set fixed height for product panels
                JLabel productNameLabel = new JLabel("  " + productName);
                productPanel.add(productNameLabel);
                productPanel.add(new JLabel(category));
                productPanel.add(new JLabel(description));
                productPanel.add(new JLabel("$" + price));
                JButton buyButton = new JButton("Buy");
                buyButton.setPreferredSize(new Dimension(80, 30));
                JButton wishlistButton = new JButton("Wishlist");
                wishlistButton.setPreferredSize(new Dimension(80, 30));

                // Wishlist button action
                wishlistButton.addActionListener(e -> {
                    if (wishlistSet.contains(productId)) {
                        JOptionPane.showMessageDialog(frame, "Already in Wishlist");
                    } else {
                        wishlistSet.add(productId);
                        // Add the product to the wishlist panel
                        addToWishlist(wishlistPanel, wishlistSet, productId, productName, category, description, price);
                        JOptionPane.showMessageDialog(frame, "Added to Wishlist");
                    }
                });

                // Buy button action
                buyButton.addActionListener(e -> {
                    if (orderSet.contains(productId)) {
                        JOptionPane.showMessageDialog(frame, "Already purchased");
                    } else {
                        orderSet.add(productId);
                        // Add the product to the order panel
                        addToOrders(ordersPanel, orderSet, productId, productName, category, description, price);
                        JOptionPane.showMessageDialog(frame, "Added to Orders");
                    }
                });
                productPanel.add(buyButton);
                productPanel.add(wishlistButton);
                homePanel.add(productPanel);
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
    } // End of Home Product Population Function


    // ==============================================
    // ===== CUSTOMER ADDS PRODUCT TO WISHLIST  =====
    // ==============================================
    public static void addToWishlist(JPanel wishlistPanel, Set<String> wishlistSet, String id, String name,
            String category, String description, double price) {

        JPanel wishlistProductPanel = new JPanel(new GridLayout(1, 5));
        wishlistProductPanel.setPreferredSize(new Dimension(800, 30)); // Set fixed height for product panels
        wishlistProductPanel.add(new JLabel("  " + name));
        wishlistProductPanel.add(new JLabel(category));
        wishlistProductPanel.add(new JLabel(description));
        wishlistProductPanel.add(new JLabel("$" + price));

        // Remove Product from Wishlist Functionality
        JLabel removeLabel = new JLabel("<html><font color='red'><u>Remove</u></font></html>");
        removeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wishlistPanel.remove(wishlistProductPanel);
                wishlistSet.remove(id);
                wishlistPanel.revalidate();
                wishlistPanel.repaint();
            }
        });

        wishlistProductPanel.add(removeLabel);
        wishlistPanel.add(wishlistProductPanel);

        wishlistPanel.revalidate();
        wishlistPanel.repaint();
    } // End of Customer Wishlist Functionality

    // ==============================================
    // ===== CUSTOMER ADDS PRODUCT TO ORDERS ========
    // ==============================================
    public static void addToOrders(JPanel orderPanel, Set<String> orderSet, String id, String name, String category,
            String description, double price) {

        JPanel orderProductPanel = new JPanel(new GridLayout(1, 5));
        orderProductPanel.setPreferredSize(new Dimension(800, 30)); // Set fixed height for product panels
        orderProductPanel.add(new JLabel("  " + name));
        orderProductPanel.add(new JLabel(category));
        orderProductPanel.add(new JLabel(description));
        orderProductPanel.add(new JLabel("$" + price));

        // Remove Product From Orders Functionality
        JLabel removeLabel = new JLabel("<html><font color='red'><u>Remove</u></font></html>");
        removeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand
        removeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                orderPanel.remove(orderProductPanel);
                orderSet.remove(id);
                orderPanel.revalidate();
                orderPanel.repaint();
            }
        });
        orderProductPanel.add(removeLabel);

        orderPanel.add(orderProductPanel);

        // Refresh the order panel
        orderPanel.revalidate();
        orderPanel.repaint();
    } // End of Orders Functionality

    // ==============================================
    // ========== LOG-IN AND REGISTER CLASS =========
    // ==============================================
    public static class LoginDialog extends JDialog {

        private JTextField emailField;
        private JButton loginButton;
        private JButton registerButton;
        private Connection conn;
        private Main.AuthCallback authCallback; // Add callback

        // Log-In Functionality
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

        // Defines Log-In Panel and holds all of it's Button's functionalities
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

            // Validates Log-In
            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleLogin();
                }
            });

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
        }

        // Validates Log-In
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
        
        // Registration Window and Functionality
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

            // Navigate back to Register
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // Sets Dimension of the Register Window
                    setPreferredSize(new Dimension(300,  125));
                    initializeLoginPanel(parent);
                }
            });

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
        }
    }

    // Getter for the Customer currently using the Application
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
    }

    // Delete Account Functionality
    public void deleteAccount(String email) {
        String deleteWishlistQuery = "DELETE FROM WISHLIST WHERE customer_id = (SELECT customer_id FROM CUSTOMER WHERE email = ?)";
        String deleteCustomerQuery = "DELETE FROM CUSTOMER WHERE email = ?";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            conn.setAutoCommit(false); // Start transaction
    
            try (PreparedStatement deleteWishlistStmt = conn.prepareStatement(deleteWishlistQuery);
                 PreparedStatement deleteCustomerStmt = conn.prepareStatement(deleteCustomerQuery)) {
    
                // Delete related wishlist entries
                deleteWishlistStmt.setString(1, email);
                deleteWishlistStmt.executeUpdate();
    
                // Delete the customer record
                deleteCustomerStmt.setString(1, email);
                int affectedRows = deleteCustomerStmt.executeUpdate();
    
                conn.commit(); // Commit transaction
    
                if (affectedRows > 0) {
                    System.out.println("Account deleted successfully.");
                } else {
                    System.out.println("No account found with the given email.");
                }
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
}
