package org.example.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AllTabsState implements Serializable {
    private List<ImageState> imageStates;

    public AllTabsState() {
        imageStates = new ArrayList<>();
    }

    public List<ImageState> getImageStates() {
        return imageStates;
    }

    public void setImageStates(List<ImageState> imageStates) {
        this.imageStates = imageStates;
    }
}
