package plus.te4net.core.img;

import com.luciad.imageio.webp.WebPWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import static javax.imageio.ImageIO.createImageOutputStream;
import static plus.te4net.core.img.CompressorSettings.def;


public final class Compressor {
    public static byte[] press(CompressorSettings settings, String key, byte[] from, ImgSize size){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        press(settings, key, new ByteArrayInputStream(from), out, size);
        return out.toByteArray();
    }


    public static void press(CompressorSettings settings, String key, InputStream from, OutputStream out, ImgSize size){
        BufferedImage image = null;
        try {
            image = ImageIO.read(from);
            if(image==null){
                for(int attempt=0;attempt<3;attempt++){
                    if(image!=null)break;
                    System.out.println("ERROR encoding image ("+attempt+")["+key+"]");
                    Thread.sleep(10+attempt*2);
                    image = ImageIO.read(from);
                }
                if(image==null)throw new NullPointerException("Cant read image for name ["+key+"]");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        press(settings, key, image, out, size);
    }


    private static void press(CompressorSettings settings, String key, BufferedImage image, OutputStream out, ImgSize size){
        CompressorSettings.ImageInfo stn = settings.get(key);
        CompressorSettings.SNode node = def;
        if(stn==null) {
            image = resize(image, size.calc(image.getWidth()), size.calc(image.getHeight()));
        } else {
            node = stn.ofSize(size);
            image = resize(image, node.calc(image.getWidth()), node.calc(image.getHeight()));
        }
        try {
            write(node, image, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void write(CompressorSettings.SNode node, BufferedImage image, OutputStream out) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType(writeParam.getCompressionTypes()[node.lossLess?1:0]);
        writeParam.setCompressionQuality(node.quality());
        ImageOutputStream imgOut = createImageOutputStream(out);
        writer.setOutput(imgOut);
        writer.write(null, new IIOImage(image, null, null), writeParam);
        imgOut.close();
    }


    public static BufferedImage resize(BufferedImage im, int width, int height){
        if(im.getWidth()==width && im.getHeight()==height)return im;
        Image newResizedImage = im.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return toBufferedImage(newResizedImage);
    }


    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bi = new BufferedImage(
                img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();
        return bi;
    }
}