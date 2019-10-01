package com.example.mountainclimbers;

public class Common {

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_TIMED = 1;
    public static final int MODE_PUZZLE = 2;

    public static int MODE = MODE_DEFAULT;

    public static int PACK_POS = 0;
    public static int LEVEL_POS = 0;
    public static int TUTORIAL_POS = 0;

    public static boolean tutorial = true;

    public static float[] climberHues = new float[] {140, 330, 40, 260, 10, 180};

    static final MountainClimber.Direction[] DIRECTIONS =
            new MountainClimber.Direction[]{null, MountainClimber.Direction.LEFT, MountainClimber.Direction.RIGHT
            };
}
