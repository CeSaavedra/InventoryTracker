
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
public class Main {
    private static JPanel settingsPanel;
    private static JPanel homeWrapperPanel;
    private static boolean isUserAuthenticated = false;
    private static String authenticatedUserEmail = null;


    /**     [1/2] SQL DATABASE CONNECTIVITY
     * 
     *      The following 3 String variables NEED to be modified in order 
     *      for you to connect to an SQL Database Schema.
     * 
     *      Use the first line to connect to the URL of your DB Schema
     *      Use the second line to input your MySQL Username
     *      Use the third line to input your MySQL Password
     */

    private static String currentUrl = "jdbc:mysql://localhost:3306/csaaved1db";
    private static String currentUser = "root";
    private static String currentPassword = "password_here";




    // =============== MAIN FUNC OF APPLICATION ===============
    public static void main(String[] args) {

        homeWrapperPanel = new JPanel(new BorderLayout());

        // Tests whether or not the Java executable was able to connect to DB
        try {
            DatabaseConnection.getConnection();
            System.out.println("Connection to the database established successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to establish connection to the database: " + e.getMessage());
        }

        // Sets up the Main Frame of the Executable
        SwingUtilities.invokeLater(() -> {

            // =============== FRAME SET-UP ===============
            JFrame frame = new JFrame("Inventory Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 1.0;

            // =============== TITLE CONTAINER ===============
            JPanel titleRow = new JPanel(new GridBagLayout());
            titleRow.setBackground(Color.decode("#2C68B3"));
            JLabel titleLabel = new JLabel("Inventory Tracker");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleRow.add(titleLabel, new GridBagConstraints()); // Center the titleLabel

            // =============== NAVIGATION BAR CONTAINER ===============
            JPanel navBar = new JPanel(new GridLayout(1, 5));
            navBar.setBackground(Color.GRAY);
            // Navigation Bar Items
            String[] navItems = { "Home", "Wishlist", "Orders", "Inventory", "Settings" };

            // =============== PANEL DECLARATIONS ===============
            JPanel mainContent = new JPanel(new CardLayout()); // Initializes Main Page
            settingsPanel = new JPanel(); // Initialize Settings Panel
            settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

            final JPanel wishlistPanel = new JPanel(); // Initialize Wishlist Panel
            wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

            final JPanel ordersPanel = new JPanel(); // Initialize Orders Panel
            ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));

            final JPanel homePanel = new JPanel(); // Initialize Home Panel
            homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));

            Set<String> wishlistSet = new HashSet<>(); // Wishlist tracking
            Set<String> orderSet = new HashSet<>(); // Order tracking

            for (String item : navItems) {

                // Sets Navigation Buttons
                JButton navButton = new JButton(item);
                navButton.setBackground(Color.decode("#2C68B3"));
                navButton.setForeground(Color.WHITE);
                navButton.setOpaque(true);
                navButton.setBorderPainted(false);
                navBar.add(navButton);

                // Sets Panels of Each Nav Item
                JPanel page = new JPanel();
                page.setLayout(new BorderLayout());
                JPanel titlePanel = new JPanel();
                page.add(titlePanel, BorderLayout.NORTH);

                // ==============================================
                // =============== INVENTORY PAGE ===============
                // ==============================================

                if (item.equals("Inventory")) {
                    
                    DatabaseHelper.populateInventoryPage(page);
                    System.out.println("Navigated to Inventory Page.");
                    // End of Inventory Page

                    // ==============================================
                    // =============== HOME PAGE ====================
                    // ==============================================

                } else if (item.equals("Home")) {

                    // Declares DatabaseHelper Instance
                    DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);

                    // Replaces current Panel with Home Panel
                    homePanel.removeAll();
                    homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS)); // Use BoxLayout for fixed height rows

                    // Sets the Column Headers
                    JPanel homeHeaderPanel = new JPanel(new GridLayout(1, 6));
                    homeHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Description</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Stock Qty</b></html>")); // New column header for stock quantity
                    homeHeaderPanel.add(new JLabel("")); // Placeholder for buy button column
                    homeHeaderPanel.add(new JLabel("")); // Placeholder for wishlist button column
                    homeHeaderPanel.setPreferredSize(new Dimension(800, 60)); // Set fixed height for header
                    homeHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper Panel
                    homeWrapperPanel.removeAll(); // Ensure wrapper panel is cleared
                    homeWrapperPanel.setLayout(new BorderLayout());
                    homeWrapperPanel.add(homeHeaderPanel, BorderLayout.NORTH);
                    homeWrapperPanel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

                    // Load Products from DB
                    dbHelper.loadProducts(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, ordersPanel,
                            orderSet, frame, authenticatedUserEmail);

                    // Add homeWrapperPanel to the page
                    page.add(homeWrapperPanel, BorderLayout.CENTER);

                    // Revalidate and repaint the homePanel to ensure proper rendering
                    homePanel.revalidate();
                    homePanel.repaint();

                    // ==============================================
                    // =============== WISHLIST PAGE ================
                    // ==============================================
                } else if (item.equals("Wishlist")) {

                    // Replaces current Panel with Wishlist Panel
                    wishlistPanel.removeAll();
                    wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

                    // Sets the Column Headers
                    JPanel wishlistHeaderPanel = new JPanel(new GridLayout(1, 5));
                    wishlistHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Stock Quant.</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("")); // Empty Placeholder for Remove Button
                    // Set Dimensions and Border of Column Headers
                    wishlistHeaderPanel.setPreferredSize(new Dimension(800, 60));
                    wishlistHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper panel to hold both header and content
                    JPanel wishlistWrapperPanel = new JPanel();
                    wishlistWrapperPanel.setLayout(new BorderLayout());
                    wishlistWrapperPanel.add(wishlistHeaderPanel, BorderLayout.NORTH);
                    wishlistWrapperPanel.add(new JScrollPane(wishlistPanel), BorderLayout.CENTER);

                    page.add(wishlistWrapperPanel, BorderLayout.CENTER);
                    // End of Wishlist Page

                    // ==============================================
                    // =============== ORDERS PAGE ==================
                    // ==============================================
                } else if (item.equals("Orders")) {
                    DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);

                    // Replaces current Panel with Orders Panel
                    ordersPanel.removeAll();
                    ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for fixed
                                                                                         // height rows

                    // Sets the Column Headers
                    JPanel ordersHeaderPanel = new JPanel(new GridLayout(1, 4));
                    ordersHeaderPanel.add(new JLabel("")); // Placeholder for column 1
                    ordersHeaderPanel.add(new JLabel("")); // Placeholder for column 2
                    ordersHeaderPanel.add(new JLabel("")); // Placeholder for column 3
                    ordersHeaderPanel.add(new JLabel("")); // Placeholder for column 4
                    ordersHeaderPanel.setPreferredSize(new Dimension(800, 60)); // Set fixed height for header
                    ordersHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper Panel
                    JPanel ordersWrapperPanel = new JPanel();
                    ordersWrapperPanel.setLayout(new BorderLayout());
                    ordersWrapperPanel.add(ordersHeaderPanel, BorderLayout.NORTH);
                    ordersWrapperPanel.add(new JScrollPane(ordersPanel), BorderLayout.CENTER);

                    // Sample data, replace this with actual data retrieval
                    String productName = "Sample Product";
                    String categoryType = "Sample Category";
                    String totalPrice = "100.00";
                    String tax = "10.00";
                    String qtyPurchased = "1";
                    String orderNo = "ORD123";
                    String supplierName = "Supplier Inc.";
                    String customerAddress = "123 Customer St.";
                    String warehouseAddress = "456 Warehouse Rd.";
                    String outpostAddress = "789 Outpost Ave.";
                    String estimatedDelivery = "2023-12-12";

                    // Create and add the product row panel
                    JPanel productRowPanel = DatabaseHelper.createProductRowPanel(
                            productName, categoryType, totalPrice, tax, qtyPurchased, orderNo,
                            supplierName, customerAddress, warehouseAddress, outpostAddress, estimatedDelivery,
                            ordersPanel, orderSet, homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, frame,
                            authenticatedUserEmail);
                    ordersPanel.add(productRowPanel);

                    ordersPanel.revalidate();
                    ordersPanel.repaint();

                    page.add(ordersWrapperPanel, BorderLayout.CENTER);

                    // ==============================================
                    // =============== SETTINGS PAGE ================
                    // ==============================================
                } else if (item.equals("Settings")) {
                    settingsPanel.removeAll();
                    settingsPanel.setLayout(new GridLayout(5, 2)); // 5 rows and 1 column

                    // Add the content to settingsPanel
                    settingsPanel.add(new JLabel("First Name:"));
                    settingsPanel.add(new JLabel("John")); // Placeholder data
                    settingsPanel.add(new JLabel("Last Name:"));
                    settingsPanel.add(new JLabel("Doe")); // Placeholder data
                    settingsPanel.add(new JLabel("Address:"));
                    settingsPanel.add(new JLabel("123 Main St")); // Placeholder data
                    settingsPanel.add(new JLabel("Email:"));
                    settingsPanel.add(new JLabel("john.doe@example.com")); // Placeholder data
                    settingsPanel.add(new JLabel("Phone No:"));
                    settingsPanel.add(new JLabel("555-1234")); // Placeholder data

                    // Make the settings panel scrollable
                    JScrollPane scrollPane = new JScrollPane(settingsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                    // Wrapper panel to hold settingsPanel
                    JPanel settingsWrapperPanel = new JPanel(new BorderLayout());
                    settingsWrapperPanel.add(scrollPane, BorderLayout.CENTER);

                    page.add(settingsWrapperPanel, BorderLayout.CENTER);
                }

                mainContent.add(page, item);

                navButton.addActionListener(e -> {
                    CardLayout cl = (CardLayout) (mainContent.getLayout());
                    cl.show(mainContent, item);
                });
            }

            // =============== CONTAINER DIMENSIONS ================
            // Title Container is 20% Height
            gbc.gridy = 0;
            gbc.weighty = 0.2;
            frame.add(titleRow, gbc);

            // Navbar Container is 10% Height
            gbc.gridy = 1;
            gbc.weighty = 0.1;
            frame.add(navBar, gbc);

            // Page Content is 70%
            gbc.gridy = 2;
            gbc.weighty = 0.7;
            frame.add(mainContent, gbc);

            frame.setVisible(true);

            new DatabaseHelper.LoginDialog(frame, email -> {
                isUserAuthenticated = true;
                authenticatedUserEmail = email;
                System.out.println("Callback executed: " + email);
                refreshSettingsPanel();
                refreshOrdersPanel(ordersPanel, orderSet, homePanel, homeWrapperPanel, wishlistPanel, wishlistSet,
                        frame, email);
                refreshProductsPanel(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, ordersPanel, orderSet,
                        frame);
                refreshWishlistPanel(wishlistPanel, email);
            });

        });
    }

    public static void refreshSettingsPanel() {
        settingsPanel.removeAll();
        settingsPanel.setLayout(new GridLayout(6, 1)); // Adjust GridLayout for an additional row

        if (isUserAuthenticated && authenticatedUserEmail != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);
            DatabaseHelper.Customer customer = dbHelper.getCustomerInfo(authenticatedUserEmail);

            if (customer != null) {
                System.out.println("Customer details:");
                System.out.println("First Name: " + customer.getFirstName());
                System.out.println("Last Name: " + customer.getLastName());
                System.out.println("Address: " + customer.getAddress());
                System.out.println("Email: " + customer.getEmail());
                System.out.println("Phone No: " + customer.getPhoneNo());

                settingsPanel.add(createRowPanel(
                        "<html><div style='padding-left: 20px;'><b>First Name:</b>&nbsp;&nbsp;&nbsp;&nbsp;</div></html>",
                        customer.getFirstName()));
                settingsPanel.add(createRowPanel(
                        "<html><div style='padding-left: 20px;'><b>Last Name:</b>&nbsp;&nbsp;&nbsp;&nbsp;</div></html>",
                        customer.getLastName()));
                settingsPanel.add(createRowPanel(
                        "<html><div style='padding-left: 20px;'><b>Address:</b>&nbsp;&nbsp;&nbsp;&nbsp;</div></html>",
                        customer.getAddress()));
                settingsPanel.add(createRowPanel(
                        "<html><div style='padding-left: 20px;'><b>Email:</b>&nbsp;&nbsp;&nbsp;&nbsp;</div></html>",
                        customer.getEmail()));
                settingsPanel.add(createRowPanel(
                        "<html><div style='padding-left: 20px;'><b>Phone No:</b>&nbsp;&nbsp;&nbsp;&nbsp;</div></html>",
                        customer.getPhoneNo()));

                // Add Delete Account button with styling and centering
                JButton deleteAccountButton = new JButton("Delete Account");
                deleteAccountButton.setBackground(Color.decode("#C02424"));
                deleteAccountButton.setForeground(Color.WHITE); // Set text color to white
                deleteAccountButton.setOpaque(true); // Make the button opaque to show background color
                deleteAccountButton.setBorderPainted(false); // Remove the button border for custom styling
                deleteAccountButton.setPreferredSize(new Dimension(150, 30)); // Set button size

                deleteAccountButton.addActionListener(e -> {
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your account?",
                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        dbHelper.deleteAccount(authenticatedUserEmail);
                        JOptionPane.showMessageDialog(null, "Your account has been deleted.");
                    }
                });

                // Create a panel with FlowLayout to center the button
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.add(deleteAccountButton);
                settingsPanel.add(buttonPanel);

            } else {
                System.out.println("Error loading customer information.");
                settingsPanel.add(new JLabel("Error loading customer information."));
            }
        } else {
            System.out.println("User not authenticated or email is null.");
            settingsPanel.add(new JLabel("Please log in or register to view settings."));
        }

        settingsPanel.revalidate();
        settingsPanel.repaint();
        System.out.println("Settings panel refreshed.");
    }

    private static JPanel createRowPanel(String labelText, String valueText) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        JLabel value = new JLabel(valueText);
        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(value, BorderLayout.CENTER);
        return rowPanel;
    }

    public interface AuthCallback {
        void onAuthSuccess(String email);
    }

    // TESTING PURPOSES ONLY: Checks if properly connected to DB
    public class DatabaseConnection {
        private static final String URL = currentUrl; // DB NAME
        private static final String USER = currentUser; // MySQL USERNAME
        private static final String PASSWORD = currentPassword; // MySQL PASSWORD

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    } // End of Test Function

    public static void refreshOrdersPanel(JPanel ordersPanel, Set<String> orderSet, JPanel homePanel,
            JPanel homeWrapperPanel, JPanel wishlistPanel, Set<String> wishlistSet, JFrame frame, String email) {
        ordersPanel.removeAll();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical alignment

        if (email != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);
            List<DatabaseHelper.Order> orders = dbHelper.getOrdersFromCustomer(email);

            if (orders != null && !orders.isEmpty()) {
                for (DatabaseHelper.Order order : orders) {
                    JPanel orderProductPanel = DatabaseHelper.createProductRowPanel(
                            order.getProductName(), order.getCategoryType(), String.valueOf(order.getTotalPrice()),
                            String.valueOf(order.getSalesTax()), String.valueOf(order.getQuantityPurchased()),
                            order.getOrderNumber(), order.getSupplierName(), order.getCustomerAddress(),
                            order.getWarehouseAddress(), order.getOutpostAddress(), order.getEstimatedDeliveryDate(),
                            ordersPanel, orderSet, homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, frame,
                            email);
                    ordersPanel.add(orderProductPanel);
                    ordersPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between panels
                }
            } else {
                // No Orders Exist
            }
        } else {
            System.out.println("User email is null.");
            ordersPanel.add(new JLabel("Please log in to view orders."));
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
        System.out.println("Orders panel refreshed.");
    }

    public static void refreshProductsPanel(JPanel homePanel, JPanel homeWrapperPanel, JPanel wishlistPanel,
            Set<String> wishlistSet, JPanel ordersPanel, Set<String> orderSet, JFrame frame) {
        if (authenticatedUserEmail != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);
            dbHelper.loadProducts(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, ordersPanel, orderSet, frame,
                    authenticatedUserEmail);
        } else {
            System.out.println("User email is null.");
            homePanel.removeAll();
            homePanel.add(new JLabel("Please log in to view products."));
        }

        homePanel.revalidate();
        homePanel.repaint();
        System.out.println("Products panel refreshed.");
    }

    public static void refreshWishlistPanel(JPanel wishlistPanel, String email) {
        wishlistPanel.removeAll();
        wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical alignment

        if (email != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(currentUrl, currentUser, currentPassword);
            Set<String> wishlistSet = new HashSet<>();
            dbHelper.loadWishlist(wishlistPanel, wishlistSet, email);

        } else {
            System.out.println("User email is null.");
            wishlistPanel.add(new JLabel("Please log in to view wishlist."));
        }

        wishlistPanel.revalidate();
        wishlistPanel.repaint();
        System.out.println("Wishlist panel refreshed.");
    }

}
