package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class BackgroundImageLoader {

    private static final float PDF_DPI = 150f;

    public static BufferedImage load(File file) throws IOException {

        if (file == null) {
            throw new IOException("file is null");
        }

        String lowerName = file.getName().toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return loadPdfFirstPage(file);
        }

        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new IOException("image file could not be read");
        }

        return image;
    }

    private static BufferedImage loadPdfFirstPage(File file) throws IOException {

        try (PDDocument document = Loader.loadPDF(file)) {

            if (document.getNumberOfPages() <= 0) {
                throw new IOException("PDFにページがありません。");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            return renderer.renderImageWithDPI(0, PDF_DPI, ImageType.RGB);

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("PDFを背景図面として読み込めませんでした。", ex);
        }
    }
}
