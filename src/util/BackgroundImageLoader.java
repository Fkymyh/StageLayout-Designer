package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.imageio.ImageIO;

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

        File pdfBoxJar = findPdfBoxJar();

        if (pdfBoxJar == null) {
            throw new IOException(
                    "PDFBox is required. Put pdfbox-app-3.x.x.jar in the lib folder.");
        }

        try (URLClassLoader loader =
                new URLClassLoader(
                        new URL[] {pdfBoxJar.toURI().toURL()},
                        BackgroundImageLoader.class.getClassLoader())) {

            Class<?> loaderClass = Class.forName("org.apache.pdfbox.Loader", true, loader);
            Method loadPdfMethod = loaderClass.getMethod("loadPDF", File.class);
            Object document = loadPdfMethod.invoke(null, file);

            try {
                Class<?> rendererClass =
                        Class.forName("org.apache.pdfbox.rendering.PDFRenderer", true, loader);
                Object renderer =
                        rendererClass.getConstructor(document.getClass()).newInstance(document);

                Class<?> imageTypeClass =
                        Class.forName("org.apache.pdfbox.rendering.ImageType", true, loader);
                Object rgb = enumValue(imageTypeClass, "RGB");

                Method renderMethod =
                        rendererClass.getMethod(
                                "renderImageWithDPI",
                                int.class,
                                float.class,
                                imageTypeClass);

                return (BufferedImage) renderMethod.invoke(renderer, 0, PDF_DPI, rgb);

            } finally {
                if (document instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            }

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("PDF could not be rendered: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumValue(Class<?> enumClass, String name) {

        return Enum.valueOf((Class<Enum>) enumClass.asSubclass(Enum.class), name);
    }

    private static File findPdfBoxJar() {

        File[] candidates = {
                new File("lib"),
                new File(System.getProperty("user.dir"), "lib")
        };

        for (File folder : candidates) {

            File[] jars =
                    folder.listFiles(
                            file -> file.isFile()
                                    && file.getName().toLowerCase().startsWith("pdfbox-app")
                                    && file.getName().toLowerCase().endsWith(".jar"));

            if (jars != null && jars.length > 0) {
                return jars[0];
            }
        }

        return null;
    }
}
