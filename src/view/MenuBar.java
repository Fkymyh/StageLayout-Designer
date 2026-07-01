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
	private JMenuItem deleteItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem rotateItem;
	private JMenuItem saveAsItem;
	private JMenuItem projectInfoItem;
	private JMenuItem previewItem;
	private JMenuItem firstClassroomItem;
	private JMenuItem clearTemplateItem;
	
	
	public MenuBar() {
		
		//ファイル
		JMenu fileMenu = new JMenu("ファイル");
		
		newItem = new JMenuItem("新規");
		fileMenu.add(newItem);
		
		openItem = new JMenuItem("開く");
		fileMenu.add(openItem);
		
		saveItem = new JMenuItem("保存");
		fileMenu.add(saveItem);
		
		saveAsItem = new JMenuItem("名前を付けて保存");
		fileMenu.add(saveAsItem);
		
		previewItem = new JMenuItem("提出用プレビュー");
		fileMenu.add(previewItem);
		
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem("印刷"));
		fileMenu.addSeparator();
		exitItem = new JMenuItem("終了");
		fileMenu.add(exitItem);
		
		add(fileMenu);
		//テンプレートメニュー
		JMenu templateMenu = new JMenu("会場テンプレート");

		firstClassroomItem = new JMenuItem("第一教室");
		clearTemplateItem = new JMenuItem("テンプレートなし");

		templateMenu.add(firstClassroomItem);
		templateMenu.add(clearTemplateItem);

		add(templateMenu);
		
		//編集
		JMenu editMenu = new JMenu("編集");
		
		deleteItem = new JMenuItem("削除");
		copyItem = new JMenuItem("コピー");
		pasteItem = new JMenuItem("貼り付け");
		rotateItem = new JMenuItem("回転");
		
		editMenu.add(deleteItem);
		editMenu.add(copyItem);
		editMenu.add(pasteItem);
		editMenu.addSeparator();
		editMenu.add(rotateItem);
		
		add(editMenu);
		
		//プロジェクト
		JMenu projectMenu = new JMenu("プロジェクト");

		projectInfoItem = new JMenuItem("案件情報");

		projectMenu.add(projectInfoItem);

		add(projectMenu);
		
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
	
	public JMenuItem getDeleteItem() {
	    return deleteItem;
	}

	public JMenuItem getCopyItem() {
	    return copyItem;
	}

	public JMenuItem getPasteItem() {
	    return pasteItem;
	}
	
	public JMenuItem getRotateItem() {
	    return rotateItem;
	}
	
	public JMenuItem getSaveAsItem() {
	    return saveAsItem;
	}
	
	public JMenuItem getProjectInfoItem() {
	    return projectInfoItem;
	}
	
	public JMenuItem getPreviewItem() {
	    return previewItem;
	}
	
	public JMenuItem getFirstClassroomItem() {
	    return firstClassroomItem;
	}

	public JMenuItem getClearTemplateItem() {
	    return clearTemplateItem;
	}

}
