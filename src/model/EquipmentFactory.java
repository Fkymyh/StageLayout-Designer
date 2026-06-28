package model;

import java.awt.Color;
import java.awt.Image;

import util.ImageLoader;

public class EquipmentFactory {
	
	public static Equipment create(String name) {
		
		switch(name) {
		
		case "マイク":
			return createEquipment(
					name, Color.BLUE, 70, 30,
					"/resources/images/p08_020_mic_A.png");
		
		case "スピーカー":
			return createEquipment(
					name, Color.BLUE, 70, 30,
					"/resources/images/p07_024_speaker_E_G.png");
		
		case "ミキサー":
            return createEquipment(
            		name, Color.BLUE, 70, 30,
            		"/resources/images/p09_001_mixer_A.png");

        case "PARライト":
            return createEquipment(
            		name, Color.YELLOW, 50, 50,
            		"/resources/images/p03_026_PAR_B_M.png");

        case "バミリ":
        		return createEquipment(
        				name, Color.LIGHT_GRAY, 20, 20,
        				"/resources/images/p01_004_3syakuhaba_panel_6syakuhaba_pane_ Bamiri_kari.png");
        case "平台":
        		return createEquipment(
        				name, Color.LIGHT_GRAY, 20, 20,
        				"/resources/images/p01_002_1monn_ hiradai_3x6.png");
        case "箱馬":
            return createEquipment(
            			name, Color.LIGHT_GRAY, 20, 20,
            			"/resources/images/p01_024_hakouma_syou2_front_1_syaku_x_1syaku_B.png");

        case "棒人間":
            return createEquipment(
            		name, Color.GREEN, 30, 60,
            		"/resources/images/p11_001_person_A_up.png");

        default:
            return createEquipment(name, Color.GRAY, 70, 30, null);
		
		}
	}
	
	private static Equipment createEquipment(
			String name,
			Color color,
			int width,
			int height,
			String imagePath) {
		
		Image image = null;
		
		if(imagePath != null) {
			image = ImageLoader.load(imagePath);
		}
		
		return new Equipment(name, color, width, height, image);
	}

}
