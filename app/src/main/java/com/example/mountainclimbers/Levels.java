package com.example.mountainclimbers;

public final class Levels {

    public static final Pack[] packs = new Pack[]{
            new Pack("The beginning", new Integer[]{
                    R.raw.lvl_00, R.raw.lvl_01, R.raw.lvl_02, R.raw.lvl_03, R.raw.lvl_04,
                    R.raw.lvl_05, R.raw.lvl_06, R.raw.lvl_07, R.raw.lvl_08, R.raw.lvl_09,
                    R.raw.lvl_10, R.raw.lvl_11, R.raw.lvl_12, R.raw.lvl_13, R.raw.lvl_14,
                    R.raw.lvl_15, R.raw.lvl_16, R.raw.lvl_17, R.raw.lvl_18, R.raw.lvl_19},
                    new Integer[] {
                            R.raw.tutorial_0, R.raw.tutorial_1, R.raw.tutorial_2, R.raw.tutorial_3,
                            R.raw.tutorial_4, R.raw.tutorial_5}),
            new Pack("Then there were 3", new Integer[] {
                    R.raw.ttw3_01, R.raw.ttw3_02, R.raw.ttw3_03, R.raw.ttw3_04, R.raw.ttw3_05,
                    R.raw.ttw3_06, R.raw.ttw3_07, R.raw.ttw3_08, R.raw.ttw3_09, R.raw.ttw3_10},
                    new Integer[] {R.raw.tutorial_ttw3_1}),
            new Pack("Easy Pack", new Integer[] {
                    R.raw.easy_01, R.raw.easy_02, R.raw.easy_03, R.raw.easy_04, R.raw.easy_05}),
            new Pack("Hard Pack", new Integer[] {
                    R.raw.hard_01, R.raw.hard_02, R.raw.hard_03, R.raw.hard_04, R.raw.hard_05,
                    R.raw.hard_06, R.raw.hard_07, R.raw.hard_08, R.raw.hard_09, R.raw.hard_10,
                    R.raw.hard_11, R.raw.hard_12, R.raw.hard_13, R.raw.hard_14, R.raw.hard_15,
                    R.raw.hard_16, R.raw.hard_17, R.raw.hard_18, R.raw.hard_19, R.raw.hard_20}),
            new Pack("Computer Generated Pack", new Integer[]{
                    R.raw.gen_001, R.raw.gen_002, R.raw.gen_003, R.raw.gen_004, R.raw.gen_005,
                    R.raw.gen_006, R.raw.gen_007, R.raw.gen_008, R.raw.gen_009, R.raw.gen_010,
                    R.raw.gen_011, R.raw.gen_012, R.raw.gen_013, R.raw.gen_014, R.raw.gen_015,
                    R.raw.gen_016, R.raw.gen_017, R.raw.gen_018, R.raw.gen_019, R.raw.gen_020,
                    R.raw.gen_021, R.raw.gen_022, R.raw.gen_023, R.raw.gen_024, R.raw.gen_025,
                    R.raw.gen_026, R.raw.gen_027, R.raw.gen_028, R.raw.gen_029, R.raw.gen_030,
                    R.raw.gen_031, R.raw.gen_032, R.raw.gen_033, R.raw.gen_034, R.raw.gen_035,
                    R.raw.gen_036, R.raw.gen_037, R.raw.gen_038, R.raw.gen_039, R.raw.gen_040,
                    R.raw.gen_041, R.raw.gen_042, R.raw.gen_043, R.raw.gen_044, R.raw.gen_045,
                    R.raw.gen_046, R.raw.gen_047, R.raw.gen_048, R.raw.gen_049, R.raw.gen_050,
                    R.raw.gen_051, R.raw.gen_052, R.raw.gen_053, R.raw.gen_054, R.raw.gen_055,
                    R.raw.gen_056, R.raw.gen_057, R.raw.gen_058, R.raw.gen_059, R.raw.gen_060,
                    R.raw.gen_061, R.raw.gen_062, R.raw.gen_063, R.raw.gen_064, R.raw.gen_065,
                    R.raw.gen_066, R.raw.gen_067, R.raw.gen_068, R.raw.gen_069, R.raw.gen_070,
                    R.raw.gen_071, R.raw.gen_072, R.raw.gen_073, R.raw.gen_074, R.raw.gen_075,
                    R.raw.gen_076, R.raw.gen_077, R.raw.gen_078, R.raw.gen_079, R.raw.gen_080,
                    R.raw.gen_081, R.raw.gen_082, R.raw.gen_083, R.raw.gen_084, R.raw.gen_085,
                    R.raw.gen_086, R.raw.gen_087, R.raw.gen_088, R.raw.gen_089, R.raw.gen_090,
                    R.raw.gen_091, R.raw.gen_092, R.raw.gen_093, R.raw.gen_094, R.raw.gen_095,
                    R.raw.gen_096, R.raw.gen_097, R.raw.gen_098, R.raw.gen_099, R.raw.gen_100
            })
    };

    public static class Pack {

        private String name;
        private Integer[] levelIDs;
        private Integer[] tutorialLevelIDs;

        private Pack(String name, Integer[] levelIDs){
            this.name = name;
            this.levelIDs = levelIDs;
        }

        private Pack(String name, Integer[] levelIDs, Integer[] tutorialLevelIDs){
            this(name, levelIDs);
            this.tutorialLevelIDs = tutorialLevelIDs;
        }

        public String getName(){
            return name;
        }

        public int getLength(){
            return levelIDs.length;
        }

        public Integer[] getLevelIDs(){
            return levelIDs;
        }

        public Integer[] getTutorialLevelIDs() {
            return tutorialLevelIDs;
        }

        public int getNumTutorials(){
            if (tutorialLevelIDs == null){
                return 0;
            }
            return tutorialLevelIDs.length;
        }
    }

}
