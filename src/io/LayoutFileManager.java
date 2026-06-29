package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Equipment;
import model.EquipmentFactory;
import model.LayoutItem;

public class LayoutFileManager {

    public static void save(List<LayoutItem> items, String fileName)
            throws IOException {

        FileWriter writer = new FileWriter(fileName);

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

        writer.close();
    }

    public static List<LayoutItem> load(String fileName)
            throws IOException {

        List<LayoutItem> items = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(new FileReader(fileName));

        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isBlank()) {
                continue;
            }

            String[] data = line.split(",", -1);

            String name = data[0];
            int x = Integer.parseInt(data[1]);
            int y = Integer.parseInt(data[2]);

            Equipment equipment = EquipmentFactory.create(name);

            LayoutItem item = new LayoutItem(equipment, x, y);

            if (data.length >= 8) {

                int width = Integer.parseInt(data[3]);
                int height = Integer.parseInt(data[4]);
                int rotation = Integer.parseInt(data[5]);
                int quantity = Integer.parseInt(data[6]);

                String memo = "";

                if (data.length >= 8) {
                    memo = data[7];
                }

                item.setSize(width, height);
                item.setRotation(rotation);
                item.setQuantity(quantity);
                item.setMemo(memo);
                
            }else if (data.length >= 6) {
            	
            	int width = Integer.parseInt(data[3]);
            	int height = Integer.parseInt(data[4]);
            	int quantity = Integer.parseInt(data[5]);
            	
            	String memo = "";
            	
            	if (data.length >= 7) {
            		memo = data[6];
            	}
            	
            	item.setSize(width,  height);
            	item.setQuantity(quantity);
            	item.setMemo(memo);

            } else {

                int quantity = Integer.parseInt(data[3]);

                String memo = "";

                if (data.length >= 5) {
                    memo = data[4];
                }

                item.setQuantity(quantity);
                item.setMemo(memo);
            }

            items.add(item);
        }

        reader.close();

        return items;
    }
}
