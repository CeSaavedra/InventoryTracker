
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;


public class Main {

    // TESTING PURPOSES ONLY: Checks if properly connected to DB
    public class DatabaseConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/csaaved1db"; // DB NAME
        private static final String USER = "root"; // MySQL USERNAME
        private static final String PASSWORD = "Sql_rootpass22"; // MySQL PASSWORD

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    } // End of Test Function

    //=============== MAIN FUNC OF APPLICATION ===============
    public static void main(String[] args) {

        // Tests whether or not the Java executable was able to connect to DB
        try {
            DatabaseConnection.getConnection();
            System.out.println("Connection to the database established successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to establish connection to the database: " + e.getMessage());
        }

        // Sets up the Main Frame of the Executable
        SwingUtilities.invokeLater(() -> {

            //=============== FRAME SET-UP ===============
            JFrame frame = new JFrame("Inventory Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 1.0; 


            //=============== TITLE CONTAINER ===============
            JPanel titleRow = new JPanel(new GridBagLayout());
            titleRow.setBackground(Color.decode("#2C68B3"));
            JLabel titleLabel = new JLabel("Inventory Tracker");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleRow.add(titleLabel, new GridBagConstraints()); // Center the titleLabel

            //=============== NAVIGATION BAR CONTAINER ===============
            JPanel navBar = new JPanel(new GridLayout(1, 5));
            navBar.setBackground(Color.GRAY);
            // Navigation Bar Items
            String[] navItems = {"Home", "Wishlist", "Orders", "Inventory", "Settings"};

            //=============== PANEL DECLARATIONS ===============
            JPanel mainContent = new JPanel(new CardLayout()); // Initializes Main Page
            final JPanel wishlistPanel = new JPanel();  // Initialize Wishlist Panel
            wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));
            final JPanel ordersPanel = new JPanel();    // Initialize Orders Panel
            ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
            final JPanel homePanel = new JPanel();      // Initialize Home Panel
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

                //==============================================
                //=============== INVENTORY PAGE ===============
                //==============================================

                if (item.equals("Inventory")) {
                    JPanel inventoryPanel = new JPanel(new GridLayout(3, 1));
                    String[] sampleProductNames = {"Product 1", "Product 2", "Product 3"};
                    String[] sampleCategories = {"Category 1", "Category 2", "Category 3"};
                    int[] stockQty = {10, 20, 30};

                    for (int i = 0; i < 3; i++) {
                        JPanel productPanel = new JPanel(new GridLayout(1, 4));
                        JLabel productName = new JLabel("  " + sampleProductNames[i]);
                        productPanel.add(productName);
                        productPanel.add(new JLabel(sampleCategories[i]));
                        productPanel.add(new JLabel("Stock Qty: " + stockQty[i]));
                        inventoryPanel.add(productPanel);
                    }
                    page.add(inventoryPanel, BorderLayout.CENTER);
                    // End of Inventory Page


                //==============================================
                //=============== HOME PAGE ====================
                //==============================================

                } else if (item.equals("Home")) {

                    // Declares DatabaseHelper Instance
                    DatabaseHelper dbHelper = new DatabaseHelper("jdbc:mysql://localhost:3306/csaaved1db", "root", "Sql_rootpass22");

                    // Replaces current Panel with Home Panel
                    homePanel.removeAll();
                    homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));  // Use BoxLayout for fixed height rows

                    // Sets the Column Headers
                    JPanel homeHeaderPanel = new JPanel(new GridLayout(1, 6));
                    homeHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Description</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    homeHeaderPanel.add(new JLabel(""));  // Placeholder for buy button column
                    homeHeaderPanel.add(new JLabel(""));  // Placeholder for wishlist button column
                    homeHeaderPanel.setPreferredSize(new Dimension(800, 60));  // Set fixed height for header
                    homeHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper Panel
                    JPanel homeWrapperPanel = new JPanel();
                    homeWrapperPanel.setLayout(new BorderLayout());
                    homeWrapperPanel.add(homeHeaderPanel, BorderLayout.NORTH);
                    homeWrapperPanel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

                    // Load Products from DB
                    dbHelper.loadProducts(homePanel, homeWrapperPanel, wishlistPanel, wishlistSet, ordersPanel, orderSet, frame);
                    page.add(homeWrapperPanel, BorderLayout.CENTER);
                    // End of Home Page
                

                //==============================================
                //=============== WISHLIST PAGE ================
                //==============================================
                } else if (item.equals("Wishlist")) {

                    // Replaces current Panel with Wishlist Panel
                    wishlistPanel.removeAll();
                    wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

                    // Sets the Column Headers
                    JPanel wishlistHeaderPanel = new JPanel(new GridLayout(1, 5));
                    wishlistHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Description</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    wishlistHeaderPanel.add(new JLabel("")); // Placeholder for remove button column
                    wishlistHeaderPanel.setPreferredSize(new Dimension(800, 60));  // Set fixed height for header
                    wishlistHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper panel to hold both header and content
                    JPanel wishlistWrapperPanel = new JPanel();
                    wishlistWrapperPanel.setLayout(new BorderLayout());
                    wishlistWrapperPanel.add(wishlistHeaderPanel, BorderLayout.NORTH);
                    wishlistWrapperPanel.add(new JScrollPane(wishlistPanel), BorderLayout.CENTER);

                    page.add(wishlistWrapperPanel, BorderLayout.CENTER);
                    // End of Wishlist Page

                //==============================================
                //=============== ORDERS PAGE ==================
                //==============================================
                } else if (item.equals("Orders")) {
                    ordersPanel.removeAll();
                    ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));

                    // Add headers
                    JPanel ordersHeaderPanel = new JPanel(new GridLayout(1, 5));
                    ordersHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    ordersHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    ordersHeaderPanel.add(new JLabel("<html><b>Description</b></html>"));
                    ordersHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    ordersHeaderPanel.add(new JLabel("")); // Placeholder for remove button column
                    ordersHeaderPanel.setPreferredSize(new Dimension(800, 60));  // Set fixed height for header
                    ordersHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper panel to hold both header and content
                    JPanel ordersWrapperPanel = new JPanel();
                    ordersWrapperPanel.setLayout(new BorderLayout());
                    ordersWrapperPanel.add(ordersHeaderPanel, BorderLayout.NORTH);
                    ordersWrapperPanel.add(new JScrollPane(ordersPanel), BorderLayout.CENTER);

                    page.add(ordersWrapperPanel, BorderLayout.CENTER);
                }

                mainContent.add(page, item);

                navButton.addActionListener(e -> {
                    CardLayout cl = (CardLayout) (mainContent.getLayout());
                    cl.show(mainContent, item);
                });
            }

            //=============== CONTAINER DIMENSIONS ================
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

            // Displays the Login Page at the Beginning
            new DatabaseHelper.LoginDialog(frame);
            frame.setVisible(true);

        }
        );
    }
}
