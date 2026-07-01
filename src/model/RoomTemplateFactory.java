package model;

public class RoomTemplateFactory {
	
	private static final int PX_PER_GRID = 25;
	
	private static int grid(double value) {
        return (int) Math.round(value * PX_PER_GRID);
	}

	public static RoomTemplate createFirstClassroom() {
    	
    

        RoomTemplate room =
                new RoomTemplate(
                        "第一教室",
                        grid(20),
                        grid(14));

        room.addObject(
                new RoomObject(
                        "黒板",
                        grid(5),
                        grid(1),
                        grid(10),
                        grid(0.5)));

        room.addObject(
                new RoomObject(
                        "教卓",
                        grid(9),
                        grid(2),
                        grid(2),
                        grid(1)));

        room.addObject(
                new RoomObject(
                        "入口",
                        grid(18),
                        grid(10),
                        grid(1.5),
                        grid(2)));

        room.addObject(
                new RoomObject(
                        "机配置エリア",
                        grid(4),
                        grid(4),
                        grid(12),
                        grid(7)));

        return room;
    }
    
    public static RoomTemplate createByName(String name) {

        if ("第一教室".equals(name)) {
            return createFirstClassroom();
        }

        return null;
    }
    
    
}
