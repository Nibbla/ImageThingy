import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Niebisch Markus on 14.03.2018.
 */
public class Utils {
    static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static String textConvertForJLabel(String s) {

        return  "<html>" + s.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>";
    }

    public static BufferedImage concatImagesHorizontal(BufferedImage b1_3, BufferedImage bi) {
        int maximum = Math.max(b1_3.getHeight(),bi.getHeight());
        BufferedImage bi1_3 = new BufferedImage(b1_3.getWidth()+bi.getWidth(), maximum, b1_3.getType());
        Graphics g = bi1_3.createGraphics();
        g.drawImage(b1_3, 0, 0, null);
        g.drawImage(bi, b1_3.getWidth(), 0, null);
        g.dispose();
        return bi1_3;

    }
    public static BufferedImage concatImagesVertical(BufferedImage b1_3, BufferedImage bi) {
        int maximum = Math.max(b1_3.getWidth(),bi.getWidth());
        BufferedImage bi1_3 = new BufferedImage(maximum, b1_3.getHeight()+bi.getHeight(), b1_3.getType());
        Graphics g = bi1_3.createGraphics();
        g.drawImage(b1_3, 0, 0, null);
        g.drawImage(bi, 0, b1_3.getHeight(), null);
        g.dispose();
        return bi1_3;
    }

    public static BufferedImage copyImageHull(BufferedImage bi1) {
        BufferedImage bi1_2 = new BufferedImage(bi1.getWidth(), bi1.getHeight(), bi1.getType());
        return bi1_2;
    }
    public static BufferedImage copyImage(BufferedImage bi1) {
        BufferedImage bi1_2 = copyImageHull(bi1);
        Graphics g = bi1_2.createGraphics();
        g.drawImage(bi1, 0, 0, null);
        g.dispose();
        return bi1_2;
    }

    public static BufferedImage loadBufferedImageWithDialog(){

        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        String currentDir = "";
        fileChooser.setCurrentDirectory(new File(currentDir));
        fileChooser.setDialogTitle("Choose Image");
        if(fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
            {
            path = fileChooser.getSelectedFile().getAbsolutePath();
            //s = file.getName();
            }
        if(path ==null)return null;
        return loadBufferedImage(path);
}

    public static BufferedImage loadBufferedImage(String path) {
        if (path==null)return loadBufferedImageWithDialog();
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }



}
