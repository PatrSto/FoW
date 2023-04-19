package org.example.Utilities;

import javax.swing.*;
import java.awt.*;
// Custom JLabel with a fixed maximum width
public class FixedSizeTabLabel extends JLabel {

    // Constructor takes text, maximum width, and FontMetrics to calculate width
    public FixedSizeTabLabel(String text, int maxWidth, FontMetrics fontMetrics) {
        // Truncate the input text to fit within the maxWidth
        String truncatedText = truncateTextToFit(text, maxWidth, fontMetrics);
        // Set the JLabel's text to the truncated text
        setText(truncatedText);
        // Set the label's horizontal alignment to CENTER
        setHorizontalAlignment(SwingConstants.CENTER);
        // Set the preferred size of the JLabel with the maxWidth and the original height
        setPreferredSize(new Dimension(maxWidth, getPreferredSize().height));
    }

    // Method to truncate the input text to fit within the maxWidth
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


