package org.example.Utilities;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class FixedSizeTabbedPaneUI extends BasicTabbedPaneUI {
    private final int tabWidth;
    private final int tabHeight;

    public FixedSizeTabbedPaneUI(int tabWidth, int tabHeight) {
        this.tabWidth = tabWidth;
        this.tabHeight = tabHeight;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return tabWidth;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return tabHeight;
    }
}

