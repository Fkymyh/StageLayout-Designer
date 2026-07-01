package model;

public class RoomTemplateFactory {

    public static RoomTemplate createFirstClassroom() {

        RoomTemplate room =
                new RoomTemplate(
                        "第一教室",
                        1000,
                        700);

        room.addObject(
                new RoomObject(
                        "黒板",
                        250,
                        40,
                        500,
                        30));

        room.addObject(
                new RoomObject(
                        "教卓",
                        450,
                        100,
                        100,
                        50));

        room.addObject(
                new RoomObject(
                        "入口",
                        850,
                        500,
                        80,
                        120));

        room.addObject(
                new RoomObject(
                        "机配置エリア",
                        200,
                        180,
                        600,
                        350));

        return room;
    }
}
