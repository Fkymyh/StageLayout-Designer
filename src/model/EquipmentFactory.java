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
                30,
                15,
                "/resources/images/mic_A.png");

        register(
                "スピーカー",
                "音響",
                Color.BLUE,
                35,
                35,
                "/resources/images/speaker_E_G.png");

        register(
                "ミキサー",
                "音響",
                Color.BLUE,
                45,
                30,
                "/resources/images/mixer_A.png");
        
        register(
                "ブームスタンド",
                "音響",
                Color.BLUE,
                45,
                20,
                "/resources/images/boom_stand.png");

        register(
                "CD_Player",
                "音響",
                Color.BLUE,
                45,
                30,
                "/resources/images/CD_player.png");

        register(
                "PARライト",
                "照明",
                Color.YELLOW,
                25,
                25,
                "/resources/images/PAR_B_M.png");

        register(
                "バミリ 横",
                "舞台",
                Color.RED,
                25,
                4,
                null);

        register(
                "バミリ 縦",
                "舞台",
                Color.RED,
                4,
                25,
                null);

        register(
                "バミリ X",
                "舞台",
                Color.RED,
                18,
                18,
                null);
        
        register(
                "バミリ ＋",
                "舞台",
                Color.RED,
                22,
                22,
                null);

        register(
                "平台",
                "舞台",
                Color.LIGHT_GRAY,
                80,
                50,
                "/resources/images/hiradai_3x6.png");

        register(
                "箱馬",
                "舞台",
                Color.LIGHT_GRAY,
                50,
                40,
                "/resources/images/hakouma.png");
        
        register(
                "長机",
                "舞台",
                Color.LIGHT_GRAY,
                60,
                20,
                "/resources/images/desk.png");
        
        

       
        register(
                "人間A 上",
                "人物",
                Color.GREEN,
                25,
                40,
                "/resources/images/humanA_up.png");

        register(
                "人間A 横",
                "人物",
                Color.GREEN,
                25,
                40,
                "/resources/images/humanA_side.png");

        register(
                "人間A 正面",
                "人物",
                Color.GREEN,
                25,
                40,
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
