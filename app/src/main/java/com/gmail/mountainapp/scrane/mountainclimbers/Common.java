package com.gmail.mountainapp.scrane.mountainclimbers;

public class Common {

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_TIMED = 1;
    public static final int MODE_PUZZLE = 2;

    public static final float[] climberHues = new float[] {140, 330, 40, 260, 10, 180};

    static final MountainClimber.Direction[] DIRECTIONS =
            new MountainClimber.Direction[]{null, MountainClimber.Direction.LEFT, MountainClimber.Direction.RIGHT
            };

    public static final int[] packCompletedAchievementIDs = new int[] {
            R.string.achievement_getting_started,
            R.string.achievement_teamwork,
            R.string.achievement_this_is_easy,
            R.string.achievement_hard_worker,
            R.string.achievement_01101001,
            R.string.achievement_awoooo,
            R.string.achievement_the_big_one,
            R.string.achievement_upside_down
    };

    public static final int ACHIEVEMENT_TINKER = 0;
}
