import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;

public interface Visualizable {
    JFrame getPaintWindow();
    void setPaintWindow(JFrame paintWindow);
    void paint(Graphics g);
    void setPaintPanel(PaintPanel<Visualizable> pp);

    /**
     * creates the panel or if it exists just makes the window visible
     * @param visible
     */
    default void setVisible(boolean visible) {
        if (this.getPaintWindow() == null) this.setPaintWindow(this.createPaintWindow(this));
        this.getPaintWindow().setVisible(visible);
    }

    /**
     * supposed to give resources free
     */
    default void close() {
        JFrame jf = this.getPaintWindow();
        if (this.getPaintWindow() == null) return;
            jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
            this.setPaintWindow(null);
            this.setPaintPanel(null);
    }


    default JFrame createPaintWindow(Visualizable v) {

        JFrame Jf = new JFrame();

        PaintPanel<Visualizable> pp = new PaintPanel<>(v);
        this.setPaintPanel(pp);
        Jf.add(pp);
        return Jf;
    }


    default void addMouseLocationListener() {
        JPanel jp = this.getPaintPanel();
        jp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("MouseX: " + e.getX() + " MouseY: " + e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    JPanel getPaintPanel();
}
