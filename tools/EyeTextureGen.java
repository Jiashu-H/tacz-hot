import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * One-shot generator for assets/deadeye/textures/gui/deadeye_eye.png.
 *
 * The icon is drawn in pure white with varying alpha so the mod can tint it
 * at render time with the configured vignette color (RenderSystem shader
 * color multiplies the texture).
 *
 * Run from the project root:
 *   javac tools/EyeTextureGen.java -d build/tmp/eyegen
 *   java -cp build/tmp/eyegen EyeTextureGen
 */
public final class EyeTextureGen {
    public static void main(String[] args) throws Exception {
        int size = 128;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        double cx = size / 2.0, cy = size / 2.0;
        double cornerL = 8, cornerR = size - 8;     // almond corners
        double lidCtrl = 62;                        // control-point offset -> apex ~31px

        // Almond (eye outline): two quadratic arcs meeting at pointed corners.
        Path2D almond = new Path2D.Double();
        almond.moveTo(cornerL, cy);
        almond.quadTo(cx, cy - lidCtrl, cornerR, cy);
        almond.quadTo(cx, cy + lidCtrl, cornerL, cy);
        almond.closePath();

        // Faint sclera fill, strong outline.
        g.setColor(new Color(255, 255, 255, 64));
        g.fill(almond);
        g.setColor(new Color(255, 255, 255, 255));
        g.setStroke(new BasicStroke(7.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(almond);

        // Iris: translucent disc with a solid ring, clipped to the almond.
        g.setClip(almond);
        double irisR = 26;
        Ellipse2D iris = new Ellipse2D.Double(cx - irisR, cy - irisR, irisR * 2, irisR * 2);
        g.setColor(new Color(255, 255, 255, 96));
        g.fill(iris);
        g.setStroke(new BasicStroke(5.5f));
        g.setColor(new Color(255, 255, 255, 255));
        g.draw(iris);

        // Pupil: solid disc with a small transparent highlight punched out.
        double pupilR = 12;
        g.fill(new Ellipse2D.Double(cx - pupilR, cy - pupilR, pupilR * 2, pupilR * 2));
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fill(new Ellipse2D.Double(cx + 2, cy - 9, 7, 7));
        g.dispose();

        File out = new File("src/main/resources/assets/deadeye/textures/gui/deadeye_eye.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("written: " + out.getAbsolutePath());
    }
}
