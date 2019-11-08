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
            R.string.achievement_easy_pack,
            R.string.achievement_hard_pack,
            R.string.achievement_computer_generated_pack,
            R.string.achievement_wolf_pack,
            R.string.achievement_big_pack,
            R.string.achievement_in_the_valley,
            R.string.achievement_five_peaks,
            R.string.achievement_six_peaks
    };

    public static final int ACHIEVEMENT_CUSTOMISE = 0;
}
