package model;

public class RoomTemplateFactory {
	
	//アプリ上の1グリッド　= 35px
	//1グリッド　= 床1マス = 0.5m 想定
	private static final int PX_PER_GRID = 35;
	
	private static int grid(double value) {
        return (int) Math.round(value * PX_PER_GRID);
	}

	public static RoomTemplate createFirstClassroom() {
		
		int roomGridW = 14; //仮:横14マス
		int roomGridH = 24; //仮:縦24マス
		
    	
    

        RoomTemplate room =
                new RoomTemplate(
                        "第一教室",
                        grid(roomGridW),
                        grid(roomGridH));
     // 黒板 :上側中央
        room.addObject(
                new RoomObject(
                        "黒板",
                        grid(3), //左から3マス
                        grid(0.5), //上から0.5マス
                        grid(8), //横 8マス
                        grid(0.5))); //縦 0.5マス
     // 教卓
        room.addObject(
                new RoomObject(
                        "教卓",
                        grid(5.5),
                        grid(2),
                        grid(3),
                        grid(1)));
     // 入口：下辺左側、左から1m=2マス空ける想定
        room.addObject(
                new RoomObject(
                        "入口",
                        grid(2),//左から2マス
                        grid(roomGridH - 1), //下辺にくっつける
                        grid(3), // 入口の横幅 3マス
                        grid(1))); //入口の奥行1マス
     
        
        room.addObject(
                RoomObject.createLine(
                        "教壇上辺",
                        grid(2), //左から2
                        grid(1), //上から1
                        grid(11), //左から11
                        grid(1))); //上から1

        room.addObject(
                RoomObject.createLine(
                        "教壇右辺",
                        grid(11),
                        grid(1),
                        grid(10),
                        grid(3)));

        room.addObject(
                RoomObject.createLine(
                        "教壇下辺",
                        grid(10),
                        grid(3),
                        grid(3),
                        grid(3)));

        room.addObject(
                RoomObject.createLine(
                        "教壇左辺",
                        grid(3),
                        grid(3),
                        grid(2),
                        grid(1)));
        
     // 左右の壁出っ張り
     		int bumpW = grid(0.5);
     		int bumpH = grid(1.0);

     		int leftBumpX = 0;
     		int rightBumpX = grid(roomGridW) - bumpW;

     		int upperBumpY = grid(roomGridH * 0.30) - bumpH / 2;
     		int lowerBumpY = grid(roomGridH * 0.65) - bumpH / 2;
        
        room.addObject(
        		new RoomObject(
        				"壁出っ張り",
        				leftBumpX,
        				upperBumpY,
        				bumpW,
        				bumpH));
        
        room.addObject(
        		new RoomObject(
        				"壁出っ張り",
        				rightBumpX,
        				upperBumpY,
        				bumpW,
        				bumpH));
        
        room.addObject(
        		new RoomObject(
        				"壁出っ張り",
        				leftBumpX,
        				lowerBumpY,
        				bumpW,
        				bumpH));
        
        room.addObject(
        		new RoomObject(
        				"壁出っ張り",
        				rightBumpX,
        				lowerBumpY,
        				bumpW,
        				bumpH));

        return room;
    }
    
    public static RoomTemplate createByName(String name) {

        if ("第一教室".equals(name)) {
            return createFirstClassroom();
        }

        return null;
    }
    
    
}
