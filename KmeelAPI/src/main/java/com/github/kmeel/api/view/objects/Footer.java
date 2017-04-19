package com.github.kmeel.api.view.objects;

import com.github.kmeel.api.KmeelAPI;
import javafx.scene.control.TableView;

/**
 * @author Marten4n6
 *         Footer used by the MessagePane
 */
public class Footer {

    private KmeelAPI kmeelAPI;
    private TableView tableView;

    private String footer;

    public Footer(String footer, KmeelAPI kmeelAPI, TableView tableView) {
        this.footer = footer;
        this.kmeelAPI = kmeelAPI;
        this.tableView = tableView;
    }

    @Override
    public String toString() {
        if (footer != null && !footer.isEmpty()) {
            String defaultFooter = "Selected: " + tableView.getSelectionModel().getSelectedItems().size() + "/"  + tableView.getItems().size() + " | ";
            defaultFooter += "Bookmarks: " + kmeelAPI.bookmarks().get().size();

            return footer + " | " + defaultFooter;
        } else {
            return footer;
        }
    }
}
