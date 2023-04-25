package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

// Custom JPanel class for displaying an image with a fog of war overlay
 public class FogPanel extends JPanel {
    // Declare required attributes
    private BufferedImage image;
    private BufferedImage fogLayer;
    private final boolean isDmPanel;
    private FogPanel playerPanel;
    private double scaleFactor = 1.0;
    private static int fogRadius = 5;
    private int rotation = 0;
    private Point lastMousePosition;
    private Point translation;

    // Enum for different mouse modes (removing fog or dragging the image)
    public enum MouseMode {
        REMOVE_FOG,
        DRAG_IMAGE
    }

    // Declare and initialize the mouseMode attribute
    private MouseMode mouseMode = MouseMode.REMOVE_FOG;

    // Set and get the mouse mode
    public void setMouseMode(MouseMode mode) {
        this.mouseMode = mode;
        updateCursor();
    }
    public MouseMode getMouseMode() {
        return mouseMode;
    }

    // Constructor for the FogPanel class
    public FogPanel(BufferedImage image, BufferedImage fogLayer, boolean isDmPanel) {
        this.image = image;
        this.fogLayer = fogLayer;
        this.isDmPanel = isDmPanel;
        setLayout(new GridBagLayout()); // Set the layout manager to GridBagLayout

        // Initialize the lastMousePosition and translation
        lastMousePosition = new Point(0, 0);
        translation = new Point(0, 0);

        // Create and add a custom MouseAdapter for handling mouse events
        MouseAdapter mouseAdapter = new MouseAdapter() {
            // ... event handlers for mouse actions
            @Override
            public void mouseEntered(MouseEvent e) {
                updateCursor();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isDmPanel && mouseMode == MouseMode.REMOVE_FOG) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        clearFogAtPoint(e.getX(), e.getY());
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        addFogAtPoint(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isDmPanel && mouseMode == MouseMode.DRAG_IMAGE) {
                    lastMousePosition = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("Mouse position: (" + e.getX() + ", " + e.getY() + ")");
                if (isDmPanel) {
                    if (mouseMode == MouseMode.REMOVE_FOG) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            clearFogAtPoint(e.getX(), e.getY());
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            addFogAtPoint(e.getX(), e.getY());
                        }
                    } else if (mouseMode == MouseMode.DRAG_IMAGE) {
                        int dx = e.getX() - lastMousePosition.x;
                        int dy = e.getY() - lastMousePosition.y;
                        addTranslation(dx, dy, true);
                        lastMousePosition = e.getPoint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDmPanel && mouseMode == MouseMode.DRAG_IMAGE) {
                    int dx = e.getX() - lastMousePosition.x;
                    int dy = e.getY() - lastMousePosition.y;
                    addTranslation(dx, dy, true);
                    lastMousePosition = e.getPoint();
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isDmPanel && mouseMode == MouseMode.REMOVE_FOG) {
                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    // Update the cursor based on the current mouse mode
    private void updateCursor() {
        if (mouseMode == MouseMode.DRAG_IMAGE) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if (mouseMode == MouseMode.REMOVE_FOG) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }
    // Override the paint method to draw a buffered image
    @Override
    public void paint(Graphics g) {
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bufferGraphics = buffer.createGraphics();
        paintComponent(bufferGraphics);
        g.drawImage(buffer, 0, 0, null);
        bufferGraphics.dispose();
    }
    // Override the paintComponent method to draw the image, fog layer, and other elements
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int x = (getWidth() - (int) (image.getWidth() * scaleFactor)) / 2 + translation.x;
        int y = (getHeight() - (int) (image.getHeight() * scaleFactor)) / 2 + translation.y;
        g2d.drawImage(image, x, y, (int) (image.getWidth() * scaleFactor), (int) (image.getHeight() * scaleFactor), null);

        if (isDmPanel) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        g2d.drawImage(fogLayer, x, y, (int) (fogLayer.getWidth() * scaleFactor), (int) (fogLayer.getHeight() * scaleFactor), null);

        Point mousePosition = getMousePosition();
        if (isDmPanel && mouseMode == MouseMode.REMOVE_FOG && mousePosition != null) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            int scaledFogRadius = (int) (fogRadius * scaleFactor);
            int squareX = mousePosition.x - scaledFogRadius / 2;
            int squareY = mousePosition.y - scaledFogRadius / 2;
            g2d.drawRect(squareX, squareY, scaledFogRadius, scaledFogRadius);
        }

        g2d.dispose();
    }
    // Set and get the associated player panel
    public void setPlayerPanel(FogPanel playerPanel) {
        this.playerPanel = playerPanel;
    }

    public FogPanel getPlayerPanel() {
        return playerPanel;
    }
    // Method to zoom the image and fog layer based on a scaling factor
    public void zoom(double newScaleFactor) {
        scaleFactor *= newScaleFactor;
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);
        setPreferredSize(new Dimension(newWidth, newHeight));
        revalidate();
        repaint();
        if (!isDmPanel) {
            return;
        }
        if (playerPanel != null) {
            playerPanel.zoom(newScaleFactor);
        }
    }
    // Set the radius of the fog clearing/adding area
    public void setFogRadius(int amount) {
        fogRadius = amount;
        if (fogRadius < 1) {
            fogRadius = 1;
        }
    }
    // Clear fog at the specified point
    private void clearFogAtPoint(int x, int y) {
        int offsetX = (getWidth() - (int) (image.getWidth() * scaleFactor)) / 2 + translation.x;
        int offsetY = (getHeight() - (int) (image.getHeight() * scaleFactor)) / 2 + translation.y;

        int fogX = (int) ((x - offsetX) / scaleFactor);
        int fogY = (int) ((y - offsetY) / scaleFactor);

        System.out.println("Clearing fog at point (" + fogX + ", " + fogY + ")");
        Graphics2D g = fogLayer.createGraphics();
        g.setComposite(AlphaComposite.Clear);
//        int scaledFogRadius = (int) (fogRadius * scaleFactor);
        int scaledFogRadius = fogRadius;
        g.fillRect(fogX - scaledFogRadius / 2, fogY - scaledFogRadius / 2, scaledFogRadius, scaledFogRadius);
        g.dispose();
        repaint();
        if (playerPanel != null) {
            System.out.println("Repainting player panel");
            playerPanel.repaint();
        }
    }
    // Add fog at the specified point
    private void addFogAtPoint(int x, int y) {
        int offsetX = (getWidth() - (int) (image.getWidth() * scaleFactor)) / 2 + translation.x;
        int offsetY = (getHeight() - (int) (image.getHeight() * scaleFactor)) / 2 + translation.y;

        int fogX = (int) ((x - offsetX) / scaleFactor);
        int fogY = (int) ((y - offsetY) / scaleFactor);

        System.out.println("Adding fog at point (" + fogX + ", " + fogY + ")");
        Graphics2D g = fogLayer.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Color.BLACK);
//        int scaledFogRadius = (int) (fogRadius * scaleFactor);
        int scaledFogRadius = fogRadius;
        g.fillRect(fogX - scaledFogRadius / 2, fogY - scaledFogRadius / 2, scaledFogRadius, scaledFogRadius);
        g.dispose();
        repaint();
        if (playerPanel != null) {
            System.out.println("Repainting player panel");
            playerPanel.repaint();
        }
    }


    // Reveal all fog on the image
    public void revealFog() {
        Graphics2D g = fogLayer.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, fogLayer.getWidth(), fogLayer.getHeight());
        g.dispose();
        repaint();
        if (playerPanel != null) {
            playerPanel.repaint();
        }
    }
    // Reset the fog layer to cover the entire image
    public void resetFog() {
        Graphics2D g = fogLayer.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, fogLayer.getWidth(), fogLayer.getHeight());
        g.dispose();
        repaint();
        if (playerPanel != null) {
            playerPanel.repaint();
        }
    }
    // Rotate the image and fog layer by 90 degrees
    public void rotateImage() {
        rotation = (rotation + 90) % 360; // Add this line
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage rotatedImage = new BufferedImage(h, w, image.getType());

        AffineTransform at = new AffineTransform();
        at.translate(h / 2.0, w / 2.0);
        at.rotate(Math.toRadians(90), 0, 0);
        at.translate(-w / 2.0, -h / 2.0);

        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.drawImage(image, at, null);
        g2d.dispose();

        // Rotate the fog layer as well
        BufferedImage rotatedFogLayer = new BufferedImage(h, w, fogLayer.getType());
        g2d = rotatedFogLayer.createGraphics();
        g2d.drawImage(fogLayer, at, null);
        g2d.dispose();

        image = rotatedImage;
        fogLayer = rotatedFogLayer; // Update the fog layer
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        revalidate();
        repaint();

        if (!isDmPanel) {
            return;
        }
        if (playerPanel != null) {
            playerPanel.rotateImage();
            playerPanel.fogLayer = this.fogLayer; // Update the player panel's fog layer
            playerPanel.repaint(); // Repaint the player panel
        }
    }
    // Apply a specific rotation to the image and fog layer
    public void applyRotation(int rotation) {
        AffineTransform transform = new AffineTransform();
        double centerX = image.getWidth() / 2.0;
        double centerY = image.getHeight() / 2.0;
        transform.rotate(Math.toRadians(rotation), centerX, centerY);
        this.rotation += rotation;
        this.rotation %= 360;
        repaint();
    }
    // Set the translation of the image and fog layer
    public void setTranslation(Point translation) {
        if (!this.translation.equals(translation)) {
            this.translation = translation;
            if (playerPanel != null) {
                playerPanel.setTranslation(translation);
            }
            repaint();
        }
    }
    // Add a translation to the current image and fog layer translation
    public void addTranslation(int dx, int dy, boolean updatePlayerPanel) {
        translation.x += dx;
        translation.y += dy;
        if (updatePlayerPanel && playerPanel != null) {
            playerPanel.setTranslation(new Point(translation.x, translation.y));
        }
        repaint();
    }
    // Getters for image, fog layer, scale factor, and rotation
    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getFogLayer() {
        return fogLayer;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public int getRotation() {
        return rotation;
    }

}
