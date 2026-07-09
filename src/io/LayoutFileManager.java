package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.DrawLine;
import model.Equipment;
import model.EquipmentFactory;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;

public class LayoutFileManager {

    // .stage はテキスト形式。人間も読めるように、PROJECT / ITEMS などの区切りで保存する。
    public static void save(
    				List<LayoutItem> items,
    				List<RoomObject> customRoomObjects,
    		        List<DrawLine> drawLines,
    				ProjectInfo projectInfo,
    				String fileName)
    				throws IOException {

        FileWriter writer = new FileWriter(fileName);
        
        writer.write("#PROJECT\n");

        writer.write("title=" + escape(projectInfo.getTitle()) + "\n");
        writer.write("date=" + escape(projectInfo.getDate()) + "\n");
        writer.write("place=" + escape(projectInfo.getPlace()) + "\n");
        writer.write("planner=" + escape(projectInfo.getPlanner()) + "\n");
        writer.write("note=" + escape(projectInfo.getNote()) + "\n");
        writer.write("template=" + escape(projectInfo.getTemplateName()) + "\n");

        writer.write("#ITEMS\n");

        for (LayoutItem item : items) {

        	writer.write(
        	        item.getEquipment().getName() + "," +
        	        item.getX() + "," +
        	        item.getY() + "," +
        	        item.getWidth() + "," +
        	        item.getHeight() + "," +
        	        item.getRotation() + "," +
        	        item.getQuantity() + "," +
        	        item.getMemo());

            writer.write("\n");
        }
        
        writer.write("#ROOM_OBJECTS\n");

        for (RoomObject object : customRoomObjects) {

            writer.write(
                    escape(object.getType()) + "," +
                    escape(object.getName()) + "," +
                    object.getX() + "," +
                    object.getY() + "," +
                    object.getWidth() + "," +
                    object.getHeight() + "," +
                    object.getEndX() + "," +
                    object.getEndY() + "," +
                    escape(object.getImagePath()));

            writer.write("\n");
        }

        writer.write("#LINES\n");

        for (DrawLine line : drawLines) {

            writer.write(
                    line.getStartX() + "," +
                    line.getStartY() + "," +
                    line.getEndX() + "," +
                    line.getEndY() + "," +
                    line.getColor().getRed() + "," +
                    line.getColor().getGreen() + "," +
                    line.getColor().getBlue() + "," +
                    line.getStrokeWidth());

            writer.write("\n");
        }

        writer.close();
    }
    
    private static String escape(String text) {

        if (text == null) {
            return "";
        }

        // メモ欄の改行やカンマで保存形式が崩れないように最低限エスケープする。
        return text
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace(",", "\\,");
    }

    private static String unescape(String text) {

        if (text == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        boolean escaping = false;

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (escaping) {

                if (c == 'n') {
                    sb.append('\n');
                } else {
                    sb.append(c);
                }

                escaping = false;

            } else {

                if (c == '\\') {
                    escaping = true;
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }
    

    public static LayoutData load(String fileName)
            throws IOException {

        ProjectInfo projectInfo = new ProjectInfo();

        List<LayoutItem> items = new ArrayList<>();
        List<RoomObject> customRoomObjects = new ArrayList<>();
        List<DrawLine> drawLines = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(new FileReader(fileName));

        String line;

        String mode = "";

        while ((line = reader.readLine()) != null) {

            if (line.isBlank()) {
                continue;
            }

            if (line.equals("#PROJECT")) {
                mode = "PROJECT";
                continue;
            }

            if (line.equals("#ITEMS")) {
                mode = "ITEMS";
                continue;
            }
            
            if (line.equals("#ROOM_OBJECTS")) {
                mode = "ROOM_OBJECTS";
                continue;
            }

            if (line.equals("#LINES")) {
                mode = "LINES";
                continue;
            }

            if ("PROJECT".equals(mode) && line.contains("=")) {

                String[] parts = line.split("=", 2);

                String key = parts[0];
                String value = "";

                if (parts.length >= 2) {
                    value = unescape(parts[1]);
                }

                switch (key) {

                case "title":
                    projectInfo.setTitle(value);
                    break;

                case "date":
                    projectInfo.setDate(value);
                    break;

                case "place":
                    projectInfo.setPlace(value);
                    break;

                case "planner":
                    projectInfo.setPlanner(value);
                    break;

                case "note":
                    projectInfo.setNote(value);
                    break;
                    
                case "template":
                    projectInfo.setTemplateName(value);
                    break;
                }

                continue;
            }
            
            if ("ITEMS".equals(mode)) {

                String[] data = splitEscapedCsv(line);

                String name = data[0];
                int x = Integer.parseInt(data[1]);
                int y = Integer.parseInt(data[2]);

                Equipment equipment = EquipmentFactory.create(name);

                LayoutItem item = new LayoutItem(equipment, x, y);

                // 古い保存ファイルも読めるように、列数の違いを見て復元する。
                if (data.length >= 8) {

                    int width = Integer.parseInt(data[3]);
                    int height = Integer.parseInt(data[4]);
                    int rotation = Integer.parseInt(data[5]);
                    int quantity = Integer.parseInt(data[6]);

                    String memo = "";

                    if (data.length >= 8) {
                        memo = unescape(data[7]);
                    }

                    item.setSize(width, height);
                    item.setRotation(rotation);
                    item.setQuantity(quantity);
                    item.setMemo(memo);

                } else if (data.length >= 6) {

                    int width = Integer.parseInt(data[3]);
                    int height = Integer.parseInt(data[4]);
                    int quantity = Integer.parseInt(data[5]);

                    String memo = "";

                    if (data.length >= 7) {
                        memo = unescape(data[6]);
                    }

                    item.setSize(width, height);
                    item.setQuantity(quantity);
                    item.setMemo(memo);

                } else {

                    int quantity = Integer.parseInt(data[3]);

                    String memo = "";

                    if (data.length >= 5) {
                        memo = unescape(data[4]);
                    }

                    item.setQuantity(quantity);
                    item.setMemo(memo);
                }

                items.add(item);

                continue;
            }

            if ("ROOM_OBJECTS".equals(mode)) {

                String[] data = splitEscapedCsv(line);

                String type = unescape(data[0]);
                String name = unescape(data[1]);

                int x = Integer.parseInt(data[2]);
                int y = Integer.parseInt(data[3]);
                int width = Integer.parseInt(data[4]);
                int height = Integer.parseInt(data[5]);

                RoomObject object;

                if (RoomObject.TYPE_IMAGE.equals(type)) {

                    String imagePath = "";

                    if (data.length >= 9) {
                        imagePath = unescape(data[8]);
                    }

                    object =
                            RoomObject.createImage(
                                    name,
                                    x,
                                    y,
                                    width,
                                    height,
                                    imagePath);

                } else if (RoomObject.TYPE_CIRCLE.equals(type)) {

                    object =
                            RoomObject.createCircle(
                                    name,
                                    x,
                                    y,
                                    width,
                                    height);

                } else {

                    object =
                            new RoomObject(
                                    name,
                                    x,
                                    y,
                                    width,
                                    height);
                }

                customRoomObjects.add(object);

                continue;
            }

            if ("LINES".equals(mode)) {

                String[] data = splitEscapedCsv(line);

                int startX = Integer.parseInt(data[0]);
                int startY = Integer.parseInt(data[1]);
                int endX = Integer.parseInt(data[2]);
                int endY = Integer.parseInt(data[3]);

                int red = Integer.parseInt(data[4]);
                int green = Integer.parseInt(data[5]);
                int blue = Integer.parseInt(data[6]);

                int strokeWidth = Integer.parseInt(data[7]);

                DrawLine drawLine =
                        new DrawLine(
                                startX,
                                startY,
                                endX,
                                endY);

                drawLine.setColor(new java.awt.Color(red, green, blue));
                drawLine.setStrokeWidth(strokeWidth);

                drawLines.add(drawLine);

                continue;
            }
        }

        reader.close();

        return new LayoutData(
                projectInfo,
                items,
                customRoomObjects,
                drawLines);
    }
    
    private static String[] splitEscapedCsv(String line) {

        List<String> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        boolean escaping = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (escaping) {

                sb.append('\\');
                sb.append(c);
                escaping = false;

            } else {

                if (c == '\\') {
                    escaping = true;
                } else if (c == ',') {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }

        result.add(sb.toString());

        return result.toArray(new String[0]);
    }
}
