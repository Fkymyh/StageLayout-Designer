package view;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar{
	
	private JMenuItem saveItem;
	private JMenuItem openItem;
	private JMenuItem newItem;
	private JMenuItem exitItem;
	private JCheckBoxMenuItem gridVisibleItem;
	private JCheckBoxMenuItem snapToGridItem;
	
	
	public MenuBar() {
		
		//ファイル
		JMenu fileMenu = new JMenu("ファイル");
		
		newItem = new JMenuItem("新規");
		fileMenu.add(newItem);
		
		openItem = new JMenuItem("開く");
		fileMenu.add(openItem);
		
		saveItem = new JMenuItem("保存");
		fileMenu.add(saveItem);
		
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem("印刷"));
		fileMenu.addSeparator();
		exitItem = new JMenuItem("終了");
		fileMenu.add(exitItem);
		
		add(fileMenu);
		
		//編集
		JMenu editMenu = new JMenu("編集");
		
		editMenu.add(new JMenuItem("削除"));
		editMenu.add(new JMenuItem("コピー"));
		editMenu.add(new JMenuItem("貼り付け"));
		
		add(editMenu);
		
		//表示
		JMenu viewMenu = new JMenu("表示");
		
		gridVisibleItem = new JCheckBoxMenuItem("グリッド表示", true);
		snapToGridItem = new JCheckBoxMenuItem("グリッド吸着", true);
		
		viewMenu.add(gridVisibleItem);
		viewMenu.add(snapToGridItem);
		
		add(viewMenu);
	}
	
	public JMenuItem getSaveItem() {
		return saveItem;
	}
	
	public JMenuItem getOpenItem() {
	    return openItem;
	}
	
	public JMenuItem getNewItem() {
		return newItem;
	}
	
	public JMenuItem getExitItem() {
		return exitItem;
	}
	
	public JCheckBoxMenuItem getGridVisibleItem() {
	    return gridVisibleItem;
	}

	public JCheckBoxMenuItem getSnapToGridItem() {
	    return snapToGridItem;
	}

}
