package org.example;

import org.example.Utilities.AllTabsState;
import org.example.Utilities.FixedSizeTabLabel;
import org.example.Utilities.FixedSizeTabbedPaneUI;
import org.example.Utilities.ImageState;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import com.formdev.flatlaf.FlatLightLaf;

public class FogOfWar {
    private static JTabbedPane playerTabbedPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() throws IOException {
        // Set Look and Feel
        setLookAndFeel();

        // Tab dimensions
        int tabWidth = 150;
        int tabHeight = 20;

        // Create the main frames
        // Dungeon Master frame
        JFrame dmFrame = new JFrame("Dungeon Master");
        dmFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Dungeon Master tabbed pane
        JTabbedPane dmTabbedPane = new JTabbedPane();
        dmTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // Set fixed size for the tabs
        dmTabbedPane.setUI(new FixedSizeTabbedPaneUI(tabWidth, tabHeight));

        // Players frame
        JFrame playerFrame = new JFrame("Players");
        playerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Players tabbed pane
        playerTabbedPane = new JTabbedPane();
        playerTabbedPane.setUI(new FixedSizeTabbedPaneUI(tabWidth, tabHeight)); // Set fixed size for the tabs
        playerTabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0; // Hide the tabs in the player's screen
            }
        });

        // Create toolbar and add it to the dmFrame
        JToolBar toolbar = createToolbar(dmFrame, dmTabbedPane, playerTabbedPane);
        dmFrame.add(toolbar, BorderLayout.NORTH);
        dmFrame.add(dmTabbedPane);

        // Synchronize selected tab between Dungeon Master and Players tabbed panes
        dmTabbedPane.addChangeListener(e -> {
            int selectedIndex = dmTabbedPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < dmTabbedPane.getTabCount() && playerTabbedPane.getTabCount() > 0) {
                playerTabbedPane.setSelectedIndex(selectedIndex);
            }
        });

        // Configure and display Dungeon Master frame
        dmFrame.pack();
        dmFrame.setLocationRelativeTo(null);
        dmFrame.setSize(800, 800);
        dmFrame.setVisible(true);

        // Configure and display Players frame
        playerFrame.add(playerTabbedPane);
        playerFrame.pack();
        playerFrame.setLocationRelativeTo(dmFrame);
        playerFrame.setSize(800, 800);
        playerFrame.setVisible(true);

        // Fullscreen functionality for Players frame
        playerFrame.addWindowStateListener(e -> {
            if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                playerFrame.dispose();
                playerFrame.setUndecorated(true);
                playerFrame.setVisible(true);
            }
        });

        // Add the Escape key listener for exiting fullscreen
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                playerFrame.dispose();
                playerFrame.setUndecorated(false);
                playerFrame.setVisible(true);
            }
        };

        // Assign the escape key listener to the Players frame
        playerFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(escapeKeyStroke, "ESCAPE");
        playerFrame.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    public static Dimension getScreenResolution(int screenIndex) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if (screenIndex >= 0 && screenIndex < gd.length) {
            DisplayMode dm = gd[screenIndex].getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            return new Dimension(screenWidth, screenHeight);
        } else {
            // Return primary screen resolution if the provided index is invalid
            DisplayMode dm = gd[0].getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            return new Dimension(screenWidth, screenHeight);
        }
    }


    // Create a fog layer with the specified dimensions
    private static BufferedImage createFogLayer(int width, int height) {
        BufferedImage fogLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fogLayer.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return fogLayer;
    }

    // Create the toolbar with buttons and actions
    private static JToolBar createToolbar(JFrame dmFrame, JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane) {
        JToolBar toolbar = new JToolBar();
        toolbar.setPreferredSize(new Dimension(toolbar.getWidth(), 30));
        toolbar.setFloatable(false);

        // Toggle fog drag button
        JToggleButton toggleFogDragButton = new JToggleButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/move.png"))));
        toggleFogDragButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            boolean isSelected = toggleFogDragButton.isSelected();
            if (selectedPanel != null) {
                selectedPanel.setMouseMode(isSelected ? FogPanel.MouseMode.DRAG_IMAGE : FogPanel.MouseMode.REMOVE_FOG);
                for (Component c : dmTabbedPane.getComponents()) {
                    if (c instanceof FogPanel) {
                        ((FogPanel) c).setMouseMode(isSelected ? FogPanel.MouseMode.DRAG_IMAGE : FogPanel.MouseMode.REMOVE_FOG);
                    }
                }
            }
        });

        // Load more pictures button
        JButton loadPicturesButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/add-image.png"))));
        loadPicturesButton.addActionListener(e -> {
            try {
                loadMorePictures(dmFrame, dmTabbedPane, playerTabbedPane, toggleFogDragButton);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Zoom in button
        JButton zoomInButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/plus.png"))));
        zoomInButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.zoom(1.1);
            }
        });

        // Zoom out button
        JButton zoomOutButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/minus.png"))));
        zoomOutButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.zoom(0.9);
            }
        });

        // Rotate button
        JButton rotateButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/rotate-right.png"))));
        rotateButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.rotateImage();
            }
        });

        // Reveal fog button
        JButton revealFogButton = new JButton("Reveal Fog");
        revealFogButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.revealFog();
            }
        });

        // Reset fog button
        JButton resetFogButton = new JButton("Reset Fog");
        resetFogButton.addActionListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.resetFog();
            }
        });

        // Save button
        JButton saveButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/diskette.png"))));
        saveButton.addActionListener(e -> {
            try {
                saveAllTabsState(dmFrame, dmTabbedPane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Open button
        JButton openButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/folder.png"))));
        openButton.addActionListener(e -> {
            try {
                openAllTabsState(dmFrame, dmTabbedPane, playerTabbedPane);
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // Fog radius slider
        JSlider fogRadiusSlider = new JSlider(JSlider.HORIZONTAL, 5, 400, 10);
        fogRadiusSlider.addChangeListener(e -> {
            FogPanel selectedPanel = (FogPanel) dmTabbedPane.getSelectedComponent();
            if (selectedPanel != null) {
                selectedPanel.setFogRadius(fogRadiusSlider.getValue());
            }
        });

        fogRadiusSlider.setMajorTickSpacing(50);
        fogRadiusSlider.setPaintTicks(false);
        fogRadiusSlider.setPaintLabels(false);
        fogRadiusSlider.setPreferredSize(new Dimension(150, 30));
        // Create a JPanel with FlowLayout to wrap the slider
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        sliderPanel.add(fogRadiusSlider);

        // Set the preferred size for the slider panel
        int sliderWidth = 170;
        int sliderHeight = 30;
        sliderPanel.setPreferredSize(new Dimension(sliderWidth, sliderHeight));
        sliderPanel.setMaximumSize(new Dimension(sliderWidth, sliderHeight));

        toolbar.add(loadPicturesButton);
        toolbar.addSeparator();
        toolbar.add(saveButton);
        toolbar.add(openButton);
        toolbar.addSeparator();
        toolbar.add(revealFogButton);
        toolbar.addSeparator();
        toolbar.add(resetFogButton);
        toolbar.addSeparator();
        toolbar.add(zoomInButton);
        toolbar.add(zoomOutButton);
        toolbar.add(rotateButton);
        toolbar.addSeparator();
        toolbar.add(toggleFogDragButton);
        toolbar.addSeparator();
        toolbar.add(sliderPanel);
        toolbar.addSeparator();

        return toolbar;
    }

    // Set the Look and Feel of the application
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Add custom options for the tabs
            UIManager.put("TabbedPane.selectedBackground", Color.white);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Exception ex) {
            System.err.println("Failed to set Look and Feel. Default Look and Feel will be used.");
        }
    }


    // Load more pictures for the Dungeon Master and Player tabbed panes
    private static void loadMorePictures(JFrame dmFrame, JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane, JToggleButton toggleFogDragButton) throws IOException {
        // Configure file chooser for image files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);

        // Show the file chooser and store the user's selection
        int returnValue = fileChooser.showOpenDialog(dmFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                // Read the image from the file and create a fog layer for it
                BufferedImage image = ImageIO.read(file);
                BufferedImage fogLayer = createFogLayer(image.getWidth(), image.getHeight());

                // Create FogPanels for the Dungeon Master and Player tabs
                FogPanel dmFogPanel = new FogPanel(image, fogLayer, true);
                FogPanel playerFogPanel = new FogPanel(image, fogLayer, false);

                // Set the mouse mode for the new panels based on the toggleFogDragButton status
                FogPanel.MouseMode mouseMode = toggleFogDragButton.isSelected() ? FogPanel.MouseMode.DRAG_IMAGE : FogPanel.MouseMode.REMOVE_FOG;
                dmFogPanel.setMouseMode(mouseMode);
                playerFogPanel.setMouseMode(mouseMode);

                // Link the panels and add them to the tabbed panes
                dmFogPanel.setPlayerPanel(playerFogPanel);
                dmTabbedPane.addTab(file.getName(), dmFogPanel);
                int tabIndex = dmTabbedPane.indexOfComponent(dmFogPanel);
                FontMetrics fontMetrics = dmTabbedPane.getFontMetrics(dmTabbedPane.getFont());
                FixedSizeTabLabel tabLabel = new FixedSizeTabLabel(file.getName(), 120, fontMetrics);
                dmTabbedPane.setTabComponentAt(tabIndex, tabLabel);

                playerTabbedPane.addTab(file.getName(), playerFogPanel);

                // Get the current screen index
                GraphicsConfiguration gc = dmFrame.getGraphicsConfiguration();
                GraphicsDevice currentScreen = gc.getDevice();
                int currentScreenIndex = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()).indexOf(currentScreen);

                // Adjust scale factor
                Dimension dmScreenSize = getScreenResolution(currentScreenIndex);
                Dimension playerScreenSize = getScreenResolution(currentScreenIndex);
                dmFogPanel.adjustScaleFactor(dmScreenSize.getWidth(), dmScreenSize.getHeight());
                playerFogPanel.adjustScaleFactor(playerScreenSize.getWidth(), playerScreenSize.getHeight());
            }
        }
    }

    // Save the state of all tabs in the Dungeon Master tabbed pane
    private static void saveAllTabsState(JFrame dmFrame, JTabbedPane dmTabbedPane) throws IOException {
        // Create an AllTabsState object to store the state of all images
        AllTabsState allTabsState = new AllTabsState();
        for (int i = 0; i < dmTabbedPane.getTabCount(); i++) {
            // Retrieve the FogPanel and its state
            FogPanel panel = (FogPanel) dmTabbedPane.getComponentAt(i);
            String tabName = dmTabbedPane.getTitleAt(i);
            ImageState imageState = new ImageState(panel.getImage(), panel.getFogLayer(), panel.getScaleFactor(), panel.getRotation(), tabName);
            allTabsState.getImageStates().add(imageState);
        }

        // Configure and show a file chooser for saving the state
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save All Tabs State");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            // Save the AllTabsState object to the selected file
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".fow")) {
                fileToSave = new File(filePath + ".fow");
            }
            // Save the AllTabsState object to the selected file
            try (FileOutputStream fos = new FileOutputStream(fileToSave);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(allTabsState);
            }
        }
    }

    // Load and apply the state of all tabs from a file
    private static void openAllTabsState(JFrame dmFrame, JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane)            throws IOException, ClassNotFoundException {
        // Configure and show a file chooser for opening the state file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open All Tabs State");
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            // Read the AllTabsState object from the selected file
            File fileToOpen = fileChooser.getSelectedFile();
            String filePath = fileToOpen.getAbsolutePath();
            if (!filePath.endsWith(".fow")) {
                JOptionPane.showMessageDialog(null, "Invalid file extension. Please select a .fow file.");
                return;
            }
            try (FileInputStream fis = new FileInputStream(fileToOpen);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                AllTabsState allTabsState = (AllTabsState) ois.readObject();

                // Close all existing tabs
                dmTabbedPane.removeAll();
                playerTabbedPane.removeAll();

                // Get the current screen index
                GraphicsConfiguration gc = dmFrame.getGraphicsConfiguration();
                GraphicsDevice currentScreen = gc.getDevice();
                int currentScreenIndex = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()).indexOf(currentScreen);

                // Create new FogPanels with the loaded image states and add them to the tabbed panes
                for (ImageState imageState : allTabsState.getImageStates()) {
                    // Retrieve the image, fog layer, scale factor, and rotation from the image state
                    BufferedImage image = imageState.getImage();
                    BufferedImage fogLayer = imageState.getFogLayer();
                    double scaleFactor = imageState.getScaleFactor();
                    int rotation = imageState.getRotation();

                    // Create new FogPanels with the loaded image state
                    FogPanel dmFogPanel = new FogPanel(image, fogLayer, true);
                    FogPanel playerFogPanel = new FogPanel(image, fogLayer, false);

                    // Get the screen resolution
                    // Adjust scale factor
                    Dimension dmScreenSize = getScreenResolution(0);
                    Dimension playerScreenSize = getScreenResolution(1);
                    dmFogPanel.adjustScaleFactor(dmScreenSize.getWidth(), dmScreenSize.getHeight());
                    playerFogPanel.adjustScaleFactor(playerScreenSize.getWidth(), playerScreenSize.getHeight());

                    // Apply scaleFactor and rotation
                    dmFogPanel.applyRotation(rotation);
                    playerFogPanel.applyRotation(rotation);
                    dmFogPanel.zoom(scaleFactor / dmFogPanel.getScaleFactor());
                    playerFogPanel.zoom(scaleFactor / playerFogPanel.getScaleFactor());

                    // Add panels to the tabbed panes and link them
                    dmFogPanel.setPlayerPanel(playerFogPanel);
                    playerFogPanel.setPlayerPanel(dmFogPanel);

                    dmTabbedPane.addTab(imageState.getTabName(), dmFogPanel);
                    int tabIndex = dmTabbedPane.indexOfComponent(dmFogPanel);
                    FontMetrics fontMetrics = dmTabbedPane.getFontMetrics(dmTabbedPane.getFont());
                    FixedSizeTabLabel tabLabel = new FixedSizeTabLabel(imageState.getTabName(), 120, fontMetrics);
                    dmTabbedPane.setTabComponentAt(tabIndex, tabLabel);

                    playerTabbedPane.addTab(imageState.getTabName(), playerFogPanel);
                }
            }
        }
    }
}
