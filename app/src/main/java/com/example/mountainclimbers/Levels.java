package com.example.mountainclimbers;

public final class Levels {

    public static final String PACK_POS = "packpos";
    public static final String LEVEL_POS = "levelpos";

    public static final Pack[] packs = new Pack[]{
            new Pack("The beginning", new Integer[]{
                    R.raw.lvl_00, R.raw.lvl_01, R.raw.lvl_02, R.raw.lvl_03, R.raw.lvl_04,
                    R.raw.lvl_05, R.raw.lvl_06, R.raw.lvl_07, R.raw.lvl_08, R.raw.lvl_09,
                    R.raw.lvl_10, R.raw.lvl_11, R.raw.lvl_12, R.raw.lvl_13, R.raw.lvl_14,
                    R.raw.lvl_15, R.raw.lvl_16, R.raw.lvl_17, R.raw.lvl_18, R.raw.lvl_19,
                    R.raw.lvl_20}),
            new Pack("Test", new Integer[] {R.raw.test_00})
    };

    public static class Pack {

        private String name;
        private Integer[] levelIDs;

        private Pack(String name, Integer[] levelIDs){
            this.name = name;
            this.levelIDs = levelIDs;
        }

        public String getName(){
            return name;
        }

        public Integer[] getLevelIDs(){
            return levelIDs;
        }
    }

}
