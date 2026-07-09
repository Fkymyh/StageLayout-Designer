package model;

public class RoomTemplateFactory {
	
	//アプリ上の1グリッド　= 20px
	//1グリッド　= 床1マス = 30cm 想定
	private static final int PX_PER_GRID = 20;
	
	private static int grid(double value) {
        return (int) Math.round(value * PX_PER_GRID);
	}

	public static RoomTemplate createFirstClassroom() {
		
		// =========================
	    // 214教室 実測値
	    // 床1マス = 30cm
	    // =========================
		
		double roomGridW = 42.0; //仮:横42マス
		double roomGridH = 51.7; //仮:縦51.7マス
		
    	
    

        RoomTemplate room =
                new RoomTemplate(
                        "214教室",
                        grid(roomGridW),
                        grid(roomGridH));
     // =========================
     // 黒板
     // 平面図では壁面の高さ126cmは使わず、薄い横長として表示
     // =========================
     double blackboardX = 10 + 16.0 / 30.0;
     double blackboardY = 0;
     double blackboardW = 554.0 / 30.0;
     double blackboardH = 0.5;

     room.addObject(
             new RoomObject(
                     "黒板",
                     grid(blackboardX),
                     grid(blackboardY),
                     grid(blackboardW),
                     grid(blackboardH)));

     // =========================
     // 教壇
     // 黒板より少し広め・奥行きありで仮表示
     // =========================
     double podiumX = blackboardX - 1.0;
     double podiumY = 0.5;
     double podiumW = blackboardW + 2.0;
     double podiumH = 2 + 22.0 / 30.0;

     room.addObject(
             new RoomObject(
                     "教壇",
                     grid(podiumX),
                     grid(podiumY),
                     grid(podiumW),
                     grid(podiumH)));

     // =========================
     // 教卓
     // 黒板の中心に合わせる
     // =========================
     double lecternW = 270.0 / 30.0;
     double lecternH = 90.0 / 30.0;

     double blackboardCenterX = blackboardX + blackboardW / 2.0;
     double lecternX = blackboardCenterX - lecternW / 2.0;
     double lecternY = 3.0;

     room.addObject(
             new RoomObject(
                     "教卓",
                     grid(lecternX),
                     grid(lecternY),
                     grid(lecternW),
                     grid(lecternH)));
     // ==========================================
     //入口
     //下側の壁、左から2マス17cm, 幅180cm
     //下辺ぴったりではなく、少し内側に描く
     //===========================================
        
        double entranceX = 2 + 17.0 / 30.0;
        double entranceW = 180.0 / 30.0;
        double entranceY = roomGridH - 0.3;
        
       
        
        
        room.addObject(
                RoomObject.createLine(
                        "入口",
                        grid(entranceX),
                        grid(roomGridH),
                        grid(entranceX + entranceW),
                        grid(entranceY))); 
     //==========================================
     //教壇エリア　仮
     //メモの４点が左右同じになっているので四角形で
     //ステージ：縦12マス５㎝ / 横42マス程度
     //==========================================
        
        double stageH = 12 + 5.0 / 30.0;
        
        room.addObject(
        		new RoomObject(
    					"ステージ想定エリア",
    					grid(0),
    					grid(roomGridH - stageH),
    					grid(roomGridW),
    					grid(stageH)));
        
     //==========================================
     //壁出っ張り
     //左右同じ位置
     //w 33cm / h 56cm
     //==========================================  
        
     double bumpW = 33.0 / 30.0;
     double bumpH = 56.0 / 30.0;
     
     double bumpY1 = 11 + 8.0 / 30.0;
     double bumpY2 = 24 + 21.0 / 30.0;
     double bumpY3 = 37 + 22.0 / 30.0;
     
     addWallBumpPair(room, roomGridW, 0, bumpY1, bumpW, bumpH);
     addWallBumpPair(room, roomGridW, 0, bumpY2, bumpW, bumpH);
     addWallBumpPair(room, roomGridW, 0, bumpY3, bumpW, bumpH);
         

        return room;
    }
	
	public static RoomTemplate createOutdoorStage() {

	    RoomTemplate room =
	            new RoomTemplate(
	                    "大学野外ステージ",
	                    grid(45),
	                    grid(30));

	    // 芝生・ステージ範囲の目安
	    room.addObject(
	            new RoomObject(
	                    "ステージ床",
	                    grid(12),
	                    grid(15),
	                    grid(21),
	                    grid(6)));

	    // 弧状の厚い壁
	    room.addObject(
	            RoomObject.createArc(
	                    "弧状壁",
	                    grid(8),
	                    grid(3),
	                    grid(29),
	                    grid(18)));

	    // 円柱4本 左右対称
	    room.addObject(
	            RoomObject.createCircle(
	                    "柱L1",
	                    grid(11),
	                    grid(11),
	                    grid(1.5),
	                    grid(1.5)));

	    room.addObject(
	            RoomObject.createCircle(
	                    "柱L2",
	                    grid(15),
	                    grid(8),
	                    grid(1.5),
	                    grid(1.5)));

	    room.addObject(
	            RoomObject.createCircle(
	                    "柱R1",
	                    grid(32.5),
	                    grid(11),
	                    grid(1.5),
	                    grid(1.5)));

	    room.addObject(
	            RoomObject.createCircle(
	                    "柱R2",
	                    grid(28.5),
	                    grid(8),
	                    grid(1.5),
	                    grid(1.5)));

	    // 名前
	    room.addObject(
	            RoomObject.createText(
	                    "大学野外ステージ",
	                    grid(17),
	                    grid(24)));

	    return room;
	}
    
    public static RoomTemplate createByName(String name) {

        if ("第一教室".equals(name)
                || "214教室".equals(name)) {
            return createFirstClassroom();
        }
        
        if ("大学野外ステージ".equals(name)) {
            return createOutdoorStage();
        }

        return null;
    }
    
    

    private static void addWallBumpPair(
            RoomTemplate room,
            double roomGridW,
            double x,
            double y,
            double w,
            double h) {

        // 左側
        room.addObject(
                new RoomObject(
                        "",
                        grid(x),
                        grid(y),
                        grid(w),
                        grid(h)));

        // 右側
        room.addObject(
                new RoomObject(
                        "",
                        grid(roomGridW - w),
                        grid(y),
                        grid(w),
                        grid(h)));
    }
}
