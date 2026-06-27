package view;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar{
	
	public MenuBar() {
		
		//ファイル
		JMenu fileMenu = new JMenu("ファイル");
		
		fileMenu.add(new JMenuItem("新規"));
		fileMenu.add(new JMenuItem("開く"));
		fileMenu.add(new JMenuItem("保存"));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem("印刷"));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem("終了"));
		
		add(fileMenu);
		
		//編集
		JMenu editMenu = new JMenu("編集");
		
		editMenu.add(new JMenuItem("削除"));
		editMenu.add(new JMenuItem("コピー"));
		editMenu.add(new JMenuItem("貼り付け"));
		
		add(editMenu);
		
		//表示
		JMenu viewMenu = new JMenu("表示");
		
		viewMenu.add(new JMenuItem("グリッド表示"));
		
		add(viewMenu);
	}

}
