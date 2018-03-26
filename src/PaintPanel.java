import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Created by Niebisch Markus on 23.02.2018.
 */
public class PaintPanel<E extends Visualizable> extends JPanel {
static final Logger logger = Logger.getLogger(PaintPanel.class.getName());
    private final E object;

    //public PaintPanel(Class<?> wClass, E object, String className) {
    public PaintPanel(E object) {
        this.object = object;
    }
    @Override
    protected void paintComponent(Graphics g) {
        object.paint(g);
    }

}
