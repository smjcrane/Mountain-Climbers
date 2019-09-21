package com.example.mountainclimbers;

public final class Levels {

    public static final String PACK_POS = "packpos";
    public static final String LEVEL_POS = "levelpos";

    public static final Pack[] packs = new Pack[]{
            new Pack("The beginning", new Integer[]{
                    R.raw.lvl_00, R.raw.lvl_01, R.raw.lvl_02, R.raw.lvl_03, R.raw.lvl_04,
                    R.raw.lvl_05, R.raw.lvl_06, R.raw.lvl_07, R.raw.lvl_08, R.raw.lvl_09,
                    R.raw.lvl_10, R.raw.lvl_11, R.raw.lvl_12, R.raw.lvl_13, R.raw.lvl_14,
                    R.raw.lvl_15, R.raw.lvl_16, R.raw.lvl_17, R.raw.lvl_18, R.raw.lvl_19}),
            new Pack("Then there were 3", new Integer[] {
                    R.raw.ttw3_01, R.raw.ttw3_02, R.raw.ttw3_03, R.raw.ttw3_04, R.raw.ttw3_05,
                    R.raw.ttw3_06, R.raw.ttw3_07, R.raw.ttw3_08, R.raw.ttw3_09, R.raw.ttw3_10}),
            new Pack("Easy Pack", new Integer[] {
                    R.raw.easy_01, R.raw.easy_02, R.raw.easy_03, R.raw.easy_04, R.raw.easy_05}),
            new Pack("Hard Pack", new Integer[] {R.raw.hard_01})
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
