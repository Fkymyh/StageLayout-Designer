package model;

import java.awt.Color;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.Map;

import util.ImageLoader;

public class EquipmentFactory {

    private static Map<String, EquipmentDefinition> definitions =
            new LinkedHashMap<>();

    static {

        register(
                "マイク",
                Color.BLUE,
                70,
                30,
                "/resources/images/p08_020_mic_A.png");

        register(
                "スピーカー",
                Color.BLUE,
                70,
                30,
                "/resources/images/p07_024_speaker_E_G.png");

        register(
                "ミキサー",
                Color.BLUE,
                70,
                30,
                "/resources/images/p09_001_mixer_A.png");

        register(
                "PARライト",
                Color.YELLOW,
                50,
                50,
                "/resources/images/p03_026_PAR_B_M.png");

        register(
                "バミリ",
                Color.LIGHT_GRAY,
                20,
                20,
                "/resources/images/p01_004_3syakuhaba_panel_6syakuhaba_pane_ Bamiri_kari.png");

        register(
                "平台",
                Color.LIGHT_GRAY,
                80,
                50,
                "/resources/images/p01_002_1monn_ hiradai_3x6.png");

        register(
                "箱馬",
                Color.LIGHT_GRAY,
                50,
                40,
                "/resources/images/p01_024_hakouma_syou2_front_1_syaku_x_1syaku_B.png");

        register(
                "棒人間",
                Color.GREEN,
                30,
                60,
                "/resources/images/p11_001_person_A_up.png");
    }

    private static void register(
            String name,
            Color color,
            int width,
            int height,
            String imagePath) {

        EquipmentDefinition definition =
                new EquipmentDefinition(
                        name,
                        color,
                        width,
                        height,
                        imagePath);

        definitions.put(name, definition);
    }

    public static Equipment create(String name) {

        EquipmentDefinition definition = definitions.get(name);

        if (definition == null) {

            return createEquipment(
                    name,
                    Color.GRAY,
                    70,
                    30,
                    null);
        }

        return createEquipment(
                definition.getName(),
                definition.getColor(),
                definition.getWidth(),
                definition.getHeight(),
                definition.getImagePath());
    }

    private static Equipment createEquipment(
            String name,
            Color color,
            int width,
            int height,
            String imagePath) {

        Image image = null;

        if (imagePath != null) {
            image = ImageLoader.load(imagePath);
        }

        return new Equipment(
                name,
                color,
                width,
                height,
                image);
    }
}
