package model;

import java.awt.Color;

public class EquipmentFactory {
	
	public static Equipment create(String name) {
		
		switch(name) {
		
		case "マイク":
			return new Equipment(name, Color.BLUE, 70, 30);
		
		case "スピーカー":
			return new Equipment(name, Color.BLUE, 70, 30);
		
		case "ミキサー":
            return new Equipment(name, Color.BLUE, 70, 30);

        case "PARライト":
            return new Equipment(name, Color.YELLOW, 50, 50);

        case "バミリ":
        case "平台":
        case "箱馬":
            return new Equipment(name, Color.LIGHT_GRAY, 20, 20);

        case "棒人間":
            return new Equipment(name, Color.GREEN, 30, 60);

        default:
            return new Equipment(name, Color.GRAY, 70, 30);
		
		}
	}

}
