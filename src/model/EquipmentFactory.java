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
                "音響",
                Color.BLUE,
                70,
                30,
                "/resources/images/p08_020_mic_A.png");

        register(
                "スピーカー",
                "音響",
                Color.BLUE,
                70,
                30,
                "/resources/images/p07_024_speaker_E_G.png");

        register(
                "ミキサー",
                "音響",
                Color.BLUE,
                70,
                30,
                "/resources/images/p09_001_mixer_A.png");
        
        register(
                "ブームスタンド",
                "音響",
                Color.BLUE,
                60,
                40,
                "/resources/images/boom_stand.png");

        register(
                "CD_player",
                "音響",
                Color.BLUE,
                40,
                30,
                "/resources/images/CD_player.png");

        register(
                "PARライト",
                "照明",
                Color.YELLOW,
                50,
                50,
                "/resources/images/p03_026_PAR_B_M.png");

        register(
                "バミリ",
                "舞台",
                Color.LIGHT_GRAY,
                20,
                20,
                "/resources/images/p01_004_3syakuhaba_panel_6syakuhaba_pane_ Bamiri_kari.png");

        register(
                "平台",
                "舞台",
                Color.LIGHT_GRAY,
                80,
                50,
                "/resources/images/p01_002_1monn_ hiradai_3x6.png");

        register(
                "箱馬",
                "舞台",
                Color.LIGHT_GRAY,
                50,
                40,
                "/resources/images/p01_024_hakouma_syou2_front_1_syaku_x_1syaku_B.png");

       
        register(
                "人間A 上",
                "人物",
                Color.GREEN,
                40,
                60,
                "/resources/images/humanA_up.png");

        register(
                "人間A 横",
                "人物",
                Color.GREEN,
                60,
                40,
                "/resources/images/humanA_side.png");

        register(
                "人間A 正面",
                "人物",
                Color.GREEN,
                40,
                60,
                "/resources/images/humanA_front.png");
    }

    private static void register(
            String name,
            String category,
            Color color,
            int width,
            int height,
            String imagePath) {

        EquipmentDefinition definition =
                new EquipmentDefinition(
                        name,
                        category,
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
    
    public static Map<String, EquipmentDefinition> getDefinitions() {

        return definitions;
    }
}
