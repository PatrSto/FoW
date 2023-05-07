package org.example.Utilities;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


public class FixedSizeTabbedPaneUI extends FlatTabbedPaneUI {
    private final int tabWidth;
    private final int tabHeight;
    private final Rectangle closeButtonRect = new Rectangle();
    private MouseAdapter mouseHandler;

    private Consumer<Integer> onTabClose;

    public FixedSizeTabbedPaneUI(int tabWidth, int tabHeight, Consumer<Integer> onTabClose) {
        this.tabWidth = tabWidth;
        this.tabHeight = tabHeight;
        this.onTabClose = onTabClose;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return tabWidth;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return tabHeight;
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

        // Draw close button
        int centerY = rects[tabIndex].y + (rects[tabIndex].height - 15) / 2;
        closeButtonRect.setBounds(rects[tabIndex].x + rects[tabIndex].width - 20, centerY, 15, 15);
            g.setColor(Color.BLACK);
            g.drawString("x", closeButtonRect.x, closeButtonRect.y + closeButtonRect.height - 3);
    }


    @Override
    protected void installListeners() {
        super.installListeners();

        if (mouseHandler == null) {
            mouseHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
                    if (tabIndex >= 0 && closeButtonRect.contains(e.getX(), e.getY())) {
                        tabPane.remove(tabIndex);
                        if (onTabClose != null) {
                            onTabClose.accept(tabIndex);
                        }
                    }
                }
            };
        }
        tabPane.addMouseListener(mouseHandler);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        tabPane.removeMouseListener(mouseHandler);
    }
}
