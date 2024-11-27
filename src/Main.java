import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 1.0;  // Ensure containers span 100% of the width

            // Title Row Container: "Inventory Tracker"
            JPanel titleRow = new JPanel(new GridBagLayout());
            titleRow.setBackground(Color.GRAY);
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

            for (String item : navItems) {
                JButton navButton = new JButton(item);
                navButton.setBackground(Color.GRAY);
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
                    String[] productNames = {"Product 1", "Product 2", "Product 3"};
                    String[] publishers = {"Publisher 1", "Publisher 2", "Publisher 3"};
                    int[] stockQty = {10, 20, 30};

                    for (int i = 0; i < 3; i++) {
                        JPanel productPanel = new JPanel(new GridLayout(1, 4));
                        JLabel productName = new JLabel("  " + productNames[i]);
                        productPanel.add(productName);
                        productPanel.add(new JLabel(publishers[i]));
                        productPanel.add(new JLabel("Stock Qty: " + stockQty[i]));
                        inventoryPanel.add(productPanel);
                    }

                    page.add(inventoryPanel, BorderLayout.CENTER);

                //=============== HOME PAGE ===============
                } else if (item.equals("Home")) {
                    JPanel homePanel = new JPanel(new GridLayout(3, 1));
                    String[] productNames = {"Product 1", "Product 2", "Product 3"};
                    String[] publishers = {"Publisher 1", "Publisher 2", "Publisher 3"};
                    String[] productDescriptions = {"Description 1", "Description 2", "Description 3"};
                    double[] productPrices = {19.99, 29.99, 39.99};

                    for (int i = 0; i < 3; i++) {
                        JPanel productPanel = new JPanel(new GridLayout(1, 7));
                        JLabel productName = new JLabel("  " + productNames[i]);
                        productPanel.add(productName);
                        productPanel.add(new JLabel(publishers[i]));
                        productPanel.add(new JLabel(productDescriptions[i]));
                        productPanel.add(new JLabel("$" + productPrices[i]));
                        JButton buyButton = new JButton("Buy");
                        buyButton.setPreferredSize(new Dimension(80, 30));
                        JButton wishlistButton = new JButton("Wishlist");
                        wishlistButton.setPreferredSize(new Dimension(80, 30));
                        productPanel.add(buyButton);
                        productPanel.add(wishlistButton);
                        homePanel.add(productPanel);
                    }

                    page.add(homePanel, BorderLayout.CENTER);
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

            // Page Content is 70% Height
            gbc.gridy = 2;
            gbc.weighty = 0.7;
            frame.add(mainContent, gbc);

            frame.setVisible(true);
        });
    }
}