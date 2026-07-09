package model;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import util.ImageLoader;

public class EquipmentFactory {

    private static Map<String, EquipmentDefinition> definitions =
            new LinkedHashMap<>();

    static {

        // ここに登録したものが、左側の機材パレットと保存ファイル復元の元データになる。
        register(
                "マイク",
                "音響 > マイク・スタンド",
                Color.BLUE,
                30,
                15,
                "/resources/images/mic_A.png");

        register(
                "スピーカー",
                "音響 > スピーカー",
                Color.BLUE,
                35,
                35,
                "/resources/images/speaker_E_G.png");

        register(
                "ミキサー",
                "音響 > その他音響",
                Color.BLUE,
                45,
                30,
                "/resources/images/mixer_A.png");
        
        register(
                "ブームスタンド",
                "音響 > マイク・スタンド",
                Color.BLUE,
                45,
                20,
                "/resources/images/boom_stand.png");

        register(
                "CD_Player",
                "音響 > その他音響",
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
                "四角形",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                200,
                140,
                "/resources/images/venue_parts/rectangle.png");

        register(
                "角丸四角形",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                200,
                140,
                "/resources/images/venue_parts/rounded_rectangle.png");

        register(
                "円形",
                "舞台 > 会場パーツ",
                Color.DARK_GRAY,
                140,
                140,
                "/resources/images/venue_parts/circle.png");

        register(
                "三角形",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                160,
                140,
                "/resources/images/venue_parts/triangle.png");

        register(
                "ひし形",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                150,
                150,
                "/resources/images/venue_parts/diamond.png");

        register(
                "六角形",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                170,
                150,
                "/resources/images/venue_parts/hexagon.png");

        register(
                "矢印",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                180,
                110,
                "/resources/images/venue_parts/arrow_right.png");

        register(
                "円柱",
                "舞台 > 会場パーツ",
                Color.LIGHT_GRAY,
                120,
                160,
                "/resources/images/venue_parts/cylinder.png");

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

        loadStageSymbols();
        
        
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

    private static void loadStageSymbols() {

        String manifestPath =
                "/resources/images/stage_symbols/manifest.csv";

        try (InputStream input =
                EquipmentFactory.class.getResourceAsStream(manifestPath)) {

            if (input == null) {
                System.out.println(
                        "素材一覧が見つかりません: " + manifestPath);
                return;
            }

            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    input,
                                    StandardCharsets.UTF_8))) {

                String line = reader.readLine();

                while ((line = reader.readLine()) != null) {

                    List<String> columns = parseCsvLine(line);

                    if (columns.size() < 4) {
                        continue;
                    }

                    String rawCategory = columns.get(1);
                    String rawLabel = columns.get(2);
                    String file = columns.get(3);

                    if (file == null || file.isBlank()) {
                        continue;
                    }

                    String imagePath =
                            "/resources/images/stage_symbols/" + file;

                    if (!resourceExists(imagePath)) {
                        continue;
                    }

                    String name = createUniqueName(
                            cleanupLabel(rawLabel),
                            file);

                    String category =
                            refineDisplayCategory(
                                    toDisplayCategory(rawCategory),
                                    rawLabel);

                    int[] size =
                            getDefaultSize(category, name, imagePath);

                    register(
                            name,
                            category,
                            getDefaultColor(category),
                            size[0],
                            size[1],
                            imagePath);
                }
            }

        } catch (Exception ex) {

            System.out.println(
                    "素材一覧の読み込みに失敗しました: "
                    + ex.getMessage());
        }
    }

    private static List<String> parseCsvLine(String line) {

        List<String> columns = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        columns.add(current.toString());

        return columns;
    }

    private static boolean resourceExists(String path) {

        return EquipmentFactory.class.getResource(path) != null;
    }

    private static String cleanupLabel(String label) {

        if (label == null || label.isBlank()) {
            return "素材";
        }

        return label
                .replace('\u2003', ' ')
                .replace('\u3000', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String createUniqueName(
            String label,
            String file) {

        String name = label;

        if (!definitions.containsKey(name)) {
            return name;
        }

        String code = file;

        int slashIndex = code.lastIndexOf('/');

        if (slashIndex >= 0) {
            code = code.substring(slashIndex + 1);
        }

        int dotIndex = code.lastIndexOf('.');

        if (dotIndex > 0) {
            code = code.substring(0, dotIndex);
        }

        int underscoreIndex = code.indexOf('_', 4);

        if (underscoreIndex > 0) {
            code = code.substring(0, underscoreIndex);
        }

        name = label + " " + code;

        int count = 2;

        while (definitions.containsKey(name)) {
            name = label + " " + code + "-" + count;
            count++;
        }

        return name;
    }

    private static String toDisplayCategory(String rawCategory) {

        if ("01_stage_deck_platform_panels".equals(rawCategory)) {
            return "舞台 > 平台・パネル";
        }

        if ("02_furniture_fx".equals(rawCategory)) {
            return "舞台 > 家具・特効";
        }

        if ("03_lighting_par_fresnel_ellipsoidal".equals(rawCategory)) {
            return "照明 > PAR・フレネル";
        }

        if ("04_lighting_led".equals(rawCategory)) {
            return "照明 > LED";
        }

        if ("05_lighting_effect_moving_clip".equals(rawCategory)) {
            return "照明 > 効果・ムービング";
        }

        if ("06_lighting_follow_stands_accessories".equals(rawCategory)) {
            return "照明 > フォロー・スタンド";
        }

        if ("07_audio_speakers_mics".equals(rawCategory)) {
            return "音響 > スピーカー・マイク";
        }

        if ("08_audio_players_stands".equals(rawCategory)) {
            return "音響 > 再生機・スタンド";
        }

        if ("09_audio_mixers_processors".equals(rawCategory)) {
            return "音響 > ミキサー・周辺";
        }

        if ("10_video_camera_pc".equals(rawCategory)) {
            return "映像 > カメラ・PC";
        }

        if ("11_people_music_stands_chairs".equals(rawCategory)) {
            return "人物 > 人・譜面台・椅子";
        }

        if ("12_instruments_guitars_drums_mallets".equals(rawCategory)) {
            return "楽器 > ギター・ドラム";
        }

        if ("13_instruments_amps_percussion".equals(rawCategory)) {
            return "楽器 > ピアノ・アンプ";
        }

        if ("14_safety_guide_misc".equals(rawCategory)) {
            return "案内 > 安全・受付";
        }

        return rawCategory;
    }

    private static String refineDisplayCategory(
            String category,
            String label) {

        if (category == null || label == null) {
            return category;
        }

        if (!category.startsWith("音響")) {
            return category;
        }

        if (label.contains("スピーカー")
                || label.contains("ウーハー")
                || label.contains("サブロー")
                || label.contains("ロー")
                || label.contains("ミッド")
                || label.contains("ハイ")) {
            return "音響 > スピーカー";
        }

        if (label.contains("マイク")
                || label.contains("スタンド")
                || label.contains("三脚")) {
            return "音響 > マイク・スタンド";
        }

        return "音響 > その他音響";
    }

    private static Color getDefaultColor(String category) {

        if (category.startsWith("音響")) {
            return Color.BLUE;
        }

        if (category.startsWith("照明")) {
            return Color.YELLOW;
        }

        if (category.startsWith("人物")) {
            return Color.GREEN;
        }

        if (category.startsWith("映像")) {
            return Color.CYAN;
        }

        if (category.startsWith("案内")) {
            return Color.ORANGE;
        }

        return Color.LIGHT_GRAY;
    }

    private static int[] getDefaultSize(
            String category,
            String name,
            String imagePath) {

        int[] imageBasedSize = getImageBasedSize(imagePath);

        if (imageBasedSize != null) {
            return imageBasedSize;
        }

        if (category.startsWith("照明")) {
            return new int[] {48, 48};
        }

        if (category.startsWith("音響")) {
            return new int[] {70, 50};
        }

        if (category.startsWith("人物")) {
            return new int[] {44, 64};
        }

        if (category.startsWith("映像")) {
            return new int[] {70, 50};
        }

        if (name.contains("平台")
                || name.contains("パネル")
                || name.contains("幕")) {
            return new int[] {80, 45};
        }

        if (name.contains("椅子")
                || name.contains("箱馬")
                || name.contains("木台")) {
            return new int[] {50, 50};
        }

        return new int[] {70, 55};
    }

    private static int[] getImageBasedSize(String imagePath) {

        if (imagePath == null) {
            return null;
        }

        try (InputStream input =
                EquipmentFactory.class.getResourceAsStream(imagePath)) {

            if (input == null) {
                return null;
            }

            BufferedImage image = ImageIO.read(input);

            if (image == null
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {
                return null;
            }

            int targetLongSide = 110;

            double scale =
                    targetLongSide
                    / (double) Math.max(
                            image.getWidth(),
                            image.getHeight());

            int width =
                    Math.max(44, (int) Math.round(image.getWidth() * scale));

            int height =
                    Math.max(44, (int) Math.round(image.getHeight() * scale));

            if (width > 180) {
                double adjust = 180 / (double) width;
                width = 180;
                height = Math.max(44, (int) Math.round(height * adjust));
            }

            if (height > 180) {
                double adjust = 180 / (double) height;
                height = 180;
                width = Math.max(44, (int) Math.round(width * adjust));
            }

            return new int[] {width, height};

        } catch (Exception ex) {
            return null;
        }
    }
}
