
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Main {

    public class DatabaseConnection {

        private static final String URL = "jdbc:mysql://localhost:3306/jsykes9db";
        private static final String USER = "yourUsername";
        private static final String PASSWORD = "yourPassword";

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    public class ProductDAO {

        public List<Product> getAllProducts() throws SQLException {
            List<Product> products = new ArrayList<>();
            String sql = "SELECT product_id, supplier_id, price, product_name, product_description FROM PRODUCT";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    String supplierId = rs.getString("supplier_id");
                    float price = rs.getFloat("price");
                    String productName = rs.getString("product_name");
                    String productDescription = rs.getString("product_description");
                    products.add(new Product(productId, supplierId, price, productName, productDescription));
                }
            }
            return products;
        }
    }

    public class Product {

        private String productId;
        private String supplierId;
        private float price;
        private String productName;
        private String productDescription;

        public Product(String productId, String supplierId, float price, String productName, String productDescription) {
            this.productId = productId;
            this.supplierId = supplierId;
            this.price = price;
            this.productName = productName;
            this.productDescription = productDescription;
        }

        // Getters and Setters
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }
    }

    public static void addToWishlist(JPanel wishlistPanel, Set<Integer> wishlistSet, int id, String name, String category, String description, double price) {
        JPanel wishlistProductPanel = new JPanel(new GridLayout(1, 5));
        wishlistProductPanel.setPreferredSize(new Dimension(800, 30));  // Set fixed height for product panels
        wishlistProductPanel.add(new JLabel("  " + name));
        wishlistProductPanel.add(new JLabel(category));
        wishlistProductPanel.add(new JLabel(description));
        wishlistProductPanel.add(new JLabel("$" + price));

        JLabel removeLabel = new JLabel("<html><font color='red'><u>Remove</u></font></html>");
        removeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Change cursor to hand
        removeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wishlistPanel.remove(wishlistProductPanel);
                wishlistSet.remove(id);  // Remove the product ID from the set
                wishlistPanel.revalidate();
                wishlistPanel.repaint();
            }
        });
        wishlistProductPanel.add(removeLabel);

        wishlistPanel.add(wishlistProductPanel);

        // Refresh the wishlist panel to show the new product
        wishlistPanel.revalidate();
        wishlistPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 1.0;  // Ensure containers span 100% of the width

            // Title Row Container: "Inventory Tracker"
            JPanel titleRow = new JPanel(new GridBagLayout());
            titleRow.setBackground(Color.decode("#2C68B3"));
            JLabel titleLabel = new JLabel("Inventory Tracker");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleRow.add(titleLabel, new GridBagConstraints()); // Center the titleLabel

            // Navigation Bar
            JPanel navBar = new JPanel(new GridLayout(1, 5));
            navBar.setBackground(Color.GRAY);
            String[] navItems = {"Home", "Wishlist", "Orders", "Inventory", "Settings"};

            // CardLayout
            JPanel mainContent = new JPanel(new CardLayout());

            // Initialize wishlist panel here to ensure it's accessible
            final JPanel wishlistPanel = new JPanel();
            wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

            // Initialize home panel here to ensure it's accessible
            final JPanel homePanel = new JPanel();
            homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));

            // Wishlist tracking
            Set<Integer> wishlistSet = new HashSet<>();

            for (String item : navItems) {
                JButton navButton = new JButton(item);
                navButton.setBackground(Color.decode("#2C68B3"));
                navButton.setForeground(Color.WHITE);
                navButton.setOpaque(true);
                navButton.setBorderPainted(false);
                navBar.add(navButton);

                JPanel page = new JPanel();
                page.setLayout(new BorderLayout());

                JPanel titlePanel = new JPanel();
                page.add(titlePanel, BorderLayout.NORTH);

                //=============== INVENTORY PAGE ===============
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

                    //=============== HOME PAGE ===============
                } else if (item.equals("Home")) {
                    homePanel.removeAll();
                    homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));  // Use BoxLayout for fixed height rows
                    String[] sampleProductNames = {"Product 1", "Product 2", "Product 3", "Product 4"};
                    String[] sampleCategories = {"Category 1", "Category 2", "Category 3", "Category 4"};
                    String[] sampleProductDescriptions = {"Description 1", "Description 2", "Description 3", "Description 4"};
                    double[] sampleProductPrices = {19.99, 29.99, 39.99, 49.99};
                    int[] sampleProductIds = {300, 301, 302, 303};

                    // Add headers
                    JPanel homeHeaderPanel = new JPanel(new GridLayout(1, 6));
                    homeHeaderPanel.add(new JLabel("<html><b>  Product Name</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Category</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Description</b></html>"));
                    homeHeaderPanel.add(new JLabel("<html><b>Price</b></html>"));
                    homeHeaderPanel.add(new JLabel(""));  // Placeholder for buy button column
                    homeHeaderPanel.add(new JLabel(""));  // Placeholder for wishlist button column
                    homeHeaderPanel.setPreferredSize(new Dimension(800, 60));  // Set fixed height for header
                    homeHeaderPanel.setBorder(BorderFactory.createEmptyBorder());

                    // Wrapper panel to hold both header and content
                    JPanel homeWrapperPanel = new JPanel();
                    homeWrapperPanel.setLayout(new BorderLayout());
                    homeWrapperPanel.add(homeHeaderPanel, BorderLayout.NORTH);
                    homeWrapperPanel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

                    // Add products
                    for (int i = 0; i < 4; i++) {
                        JPanel productPanel = new JPanel(new GridLayout(1, 6));
                        productPanel.setPreferredSize(new Dimension(800, 30));  // Set fixed height for product panels
                        JLabel productName = new JLabel("  " + sampleProductNames[i]);
                        productPanel.add(productName);
                        productPanel.add(new JLabel(sampleCategories[i]));
                        productPanel.add(new JLabel(sampleProductDescriptions[i]));
                        productPanel.add(new JLabel("$" + sampleProductPrices[i]));
                        JButton buyButton = new JButton("Buy");
                        buyButton.setPreferredSize(new Dimension(80, 30));
                        JButton wishlistButton = new JButton("Wishlist");
                        wishlistButton.setPreferredSize(new Dimension(80, 30));

                        // Workaround for final/effectively final variable requirement
                        final int finalProductId = sampleProductIds[i];
                        final String finalProductName = sampleProductNames[i];
                        final String finalCategory = sampleCategories[i];
                        final String finalDescription = sampleProductDescriptions[i];
                        final double finalPrice = sampleProductPrices[i];

                        // Wishlist button action
                        wishlistButton.addActionListener(e -> {
                            if (wishlistSet.contains(finalProductId)) {
                                JOptionPane.showMessageDialog(frame, "Already in Wishlist");
                            } else {
                                wishlistSet.add(finalProductId);
                                // Add the product to the wishlist panel
                                addToWishlist(wishlistPanel, wishlistSet, finalProductId, finalProductName, finalCategory, finalDescription, finalPrice);
                                JOptionPane.showMessageDialog(frame, "Added to Wishlist");
                            }
                        });

                        productPanel.add(buyButton);
                        productPanel.add(wishlistButton);
                        homePanel.add(productPanel);
                    }

                    page.add(homeWrapperPanel, BorderLayout.CENTER);

                    //=============== WISHLIST PAGE ===============
                } else if (item.equals("Wishlist")) {
                    wishlistPanel.removeAll();
                    wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));

                    // Add headers
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
                }

                mainContent.add(page, item);

                navButton.addActionListener(e -> {
                    CardLayout cl = (CardLayout) (mainContent.getLayout());
                    cl.show(mainContent, item);
                });
            }

            // Title is 20% Height
            gbc.gridy = 0;
            gbc.weighty = 0.2;
            frame.add(titleRow, gbc);

            // Navbar is 10% Height
            gbc.gridy = 1;
            gbc.weighty = 0.1;
            frame.add(navBar, gbc);

            // Page Content is 70%
            gbc.gridy = 2;
            gbc.weighty = 0.7;
            frame.add(mainContent, gbc);

            frame.setVisible(true);
        });
    }
}
