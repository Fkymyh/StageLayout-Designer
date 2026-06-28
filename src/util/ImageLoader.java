package util;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class ImageLoader {
	
	public static Image load(String path) {
		
		URL url = ImageLoader.class.getResource(path);
		
		if (url == null) {
			System.out.println("画像が見つかりません" + path);
			return null;
		}
		
		return new ImageIcon(url).getImage();
	}

}
