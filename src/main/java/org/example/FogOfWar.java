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
        setLookAndFeel();
        int tabWidth = 150;
        int tabHeight = 20;
        // Create the main frames
        JFrame dmFrame = new JFrame("Dungeon Master");
        dmFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane dmTabbedPane = new JTabbedPane();
        dmTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        dmTabbedPane.setUI(new FixedSizeTabbedPaneUI(tabWidth, tabHeight)); // Set fixed size for the tabs

        // Initialize playerTabbedPane before creating the toolbar
        JFrame playerFrame = new JFrame("Players");
        playerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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


        dmTabbedPane.addChangeListener(e -> {
            int selectedIndex = dmTabbedPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < dmTabbedPane.getTabCount() && playerTabbedPane.getTabCount() > 0) {
                playerTabbedPane.setSelectedIndex(selectedIndex);
            }
        });

        dmFrame.pack();
        dmFrame.setLocationRelativeTo(null);
        dmFrame.setSize(800, 800);
        dmFrame.setVisible(true);

        playerFrame.add(playerTabbedPane);

        playerFrame.pack();
        playerFrame.setLocationRelativeTo(dmFrame);
        playerFrame.setSize(800, 800);
        playerFrame.setVisible(true);

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

        playerFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(escapeKeyStroke, "ESCAPE");
        playerFrame.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }


    private static BufferedImage createFogLayer(int width, int height) {
        BufferedImage fogLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fogLayer.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return fogLayer;
    }

    private static JToolBar createToolbar(JFrame dmFrame, JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane) {
        JToolBar toolbar = new JToolBar();
        toolbar.setPreferredSize(new Dimension(toolbar.getWidth(), 30));
//        toolbar.setBackground(new Color(54, 54, 54));
        toolbar.setFloatable(false);


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
                saveAllTabsState(dmTabbedPane);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Open button
        JButton openButton = new JButton(new ImageIcon(Objects.requireNonNull(FogOfWar.class.getResource("/images/folder.png"))));
        openButton.addActionListener(e -> {
            try {
                openAllTabsState(dmTabbedPane, playerTabbedPane);
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
        fogRadiusSlider.setPreferredSize(new Dimension(150,30));
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
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to set Look and Feel. Default Look and Feel will be used.");
        }
    }


    private static void loadMorePictures(JFrame dmFrame, JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane, JToggleButton toggleFogDragButton) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(dmFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                BufferedImage image = ImageIO.read(file);
                BufferedImage fogLayer = createFogLayer(image.getWidth(), image.getHeight());
                FogPanel dmFogPanel = new FogPanel(image, fogLayer, true);
                FogPanel playerFogPanel = new FogPanel(image, fogLayer, false);

                // Set the mouse mode for the new panels based on the toggleFogDragButton status
                FogPanel.MouseMode mouseMode = toggleFogDragButton.isSelected() ? FogPanel.MouseMode.DRAG_IMAGE : FogPanel.MouseMode.REMOVE_FOG;
                dmFogPanel.setMouseMode(mouseMode);
                playerFogPanel.setMouseMode(mouseMode);

                dmFogPanel.setPlayerPanel(playerFogPanel);
                dmTabbedPane.addTab(file.getName(), dmFogPanel);
                int tabIndex = dmTabbedPane.indexOfComponent(dmFogPanel);
                FontMetrics fontMetrics = dmTabbedPane.getFontMetrics(dmTabbedPane.getFont());
                FixedSizeTabLabel tabLabel = new FixedSizeTabLabel(file.getName(), 140, fontMetrics);
                dmTabbedPane.setTabComponentAt(tabIndex, tabLabel);

                playerTabbedPane.addTab(file.getName(), playerFogPanel);
            }
        }
    }
    private static void saveAllTabsState(JTabbedPane dmTabbedPane) throws IOException {
        AllTabsState allTabsState = new AllTabsState();
        for (int i = 0; i < dmTabbedPane.getTabCount(); i++) {
            FogPanel panel = (FogPanel) dmTabbedPane.getComponentAt(i);
            String tabName = dmTabbedPane.getTitleAt(i);
            ImageState imageState = new ImageState(panel.getImage(), panel.getFogLayer(), panel.getScaleFactor(), panel.getRotation(), tabName);
            allTabsState.getImageStates().add(imageState);
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save All Tabs State");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".fow")) {
                fileToSave = new File(filePath + ".fow");
            }
            try (FileOutputStream fos = new FileOutputStream(fileToSave);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(allTabsState);
            }
        }
    }
    private static void openAllTabsState(JTabbedPane dmTabbedPane, JTabbedPane playerTabbedPane)
            throws IOException, ClassNotFoundException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open All Tabs State");
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
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

                for (ImageState imageState : allTabsState.getImageStates()) {
                    BufferedImage image = imageState.getImage();
                    BufferedImage fogLayer = imageState.getFogLayer();
                    double scaleFactor = imageState.getScaleFactor();
                    int rotation = imageState.getRotation();
                    // Create new FogPanels with the loaded image state

                    FogPanel dmPanel = new FogPanel(image, fogLayer, true);
                    FogPanel playerPanel = new FogPanel(image, fogLayer, false);

                    // Apply scaleFactor and rotation
                    dmPanel.applyRotation(rotation);  // Updated method
                    playerPanel.applyRotation(rotation);  // Updated method
                    dmPanel.zoom(scaleFactor / dmPanel.getScaleFactor());
                    playerPanel.zoom(scaleFactor / playerPanel.getScaleFactor());

                    // Add panels to the tabbed panes
                    dmPanel.setPlayerPanel(playerPanel);
                    playerPanel.setPlayerPanel(dmPanel);

                    dmTabbedPane.addTab(imageState.getTabName(), dmPanel);
                    int tabIndex = dmTabbedPane.indexOfComponent(dmPanel);
                    FontMetrics fontMetrics = dmTabbedPane.getFontMetrics(dmTabbedPane.getFont());
                    FixedSizeTabLabel tabLabel = new FixedSizeTabLabel(imageState.getTabName(), 140, fontMetrics);
                    dmTabbedPane.setTabComponentAt(tabIndex, tabLabel);

                    playerTabbedPane.addTab(imageState.getTabName(), playerPanel);

                }
            }
        }
    }
}