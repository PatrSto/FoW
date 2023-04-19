package org.example.Utilities;

import javax.swing.*;
import java.awt.*;

public class FixedSizeTabLabel extends JLabel {

    public FixedSizeTabLabel(String text, int maxWidth, FontMetrics fontMetrics) {
        String truncatedText = truncateTextToFit(text, maxWidth, fontMetrics);
        setText(truncatedText);
        setHorizontalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(maxWidth, getPreferredSize().height));
    }

    private String truncateTextToFit(String text, int maxWidth, FontMetrics fontMetrics) {
        int dotIndex = text.lastIndexOf(".");
        if (dotIndex >= 0) {
            // Cut the file extension if it exists
            text = text.substring(0, dotIndex);
        }
        int textWidth = fontMetrics.stringWidth(text);
        if (textWidth <= maxWidth) {
            return text;
        } else {
            StringBuilder truncatedText = new StringBuilder();
            int currentWidth = 0;
            for (char c : text.toCharArray()) {
                int charWidth = fontMetrics.charWidth(c);
                if (currentWidth + charWidth > maxWidth) {
                    break;
                }
                truncatedText.append(c);
                currentWidth += charWidth;
            }
            return truncatedText.toString();
        }
    }

}


