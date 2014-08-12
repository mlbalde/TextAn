package cz.cuni.mff.ufal.textan.gui.settings;

import cz.cuni.mff.ufal.textan.gui.InnerWindow;
import cz.cuni.mff.ufal.textan.gui.TextAnController;
import cz.cuni.mff.ufal.textan.gui.Utils;
import static cz.cuni.mff.ufal.textan.gui.settings.SettingsController.MIN_HEIGHT;
import static cz.cuni.mff.ufal.textan.gui.settings.SettingsController.MIN_WIDTH;
import static cz.cuni.mff.ufal.textan.gui.settings.SettingsController.PROPERTY_ID;
import static cz.cuni.mff.ufal.textan.gui.settings.SettingsController.TITLE;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.controlsfx.dialog.Dialogs;

/**
 * Class for displaying Settings in inner window.
 */
public class SettingsWindow extends InnerWindow {

    /**
     * Only constructor.
     * @param textAnController application controller
     * @param settings properties with settings
     */
    public SettingsWindow(final TextAnController textAnController,
            final Properties settings) {
        super(TITLE, PROPERTY_ID, settings);
        setMinWidth(MIN_WIDTH);
        setMinHeight(MIN_HEIGHT);
        setResizableWindow(false);
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle("cz.cuni.mff.ufal.textan.gui.settings.Settings");
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"), resourceBundle);
            final Parent loadedRoot = (Parent) loader.load();
            getContentPane().getChildren().add(loadedRoot);
            final SettingsController controller = loader.getController();
            controller.setWindow(this);
            controller.setSettings(settings);
            controller.setTextAnController(textAnController);
            setTitle(Utils.localize(resourceBundle, PROPERTY_ID));
        } catch (Exception e) {
            e.printStackTrace();
            Dialogs.create()
                    .title(Utils.localize(resourceBundle, "page.load.error"))
                    .showException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}