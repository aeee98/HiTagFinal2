package sg.rods.resources;

/**
 * Created by Lee on 3/8/2015.
 */
public class Command {
    public final static byte CREATE = 01;
    public final static byte JOIN = 02;
    public static final byte LEAVE = 03;
    public static final byte ROOM_REFRESH = 04;
    public static final byte ROOM_KICK = 05;
    public static final byte START = 06;
    public static final byte LIST = 99;
    public final static byte SETNAME = 100;
}
