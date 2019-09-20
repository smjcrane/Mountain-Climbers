# Adding your own levels

If you would like to contribute some levels of your own, please fork this repository and make a pull request.

Inside `app/src/main/res/levels`, make a new directory with the name of your pack.  Inside that, make a directory called `raw`.  Inside that, you can put your level files (see below).

In `app/build.gradle` (NOT the `build.gradle` file in the project root), add the path to your pack directory to `android.sourceSets.main.res.srcDirs`.

In `app/src/main/java/com/example/mountainclimbers/Levels.java`, add a new Pack object to `packs` with a name for your level pack and the resource IDs (`R.raw.filename`) for your levels.  The list should be in the order you want them to be displayed.

## Level files

Level files have the extension `.txt`.  

The first line describes the mountain.  This is a list of heights of the peaks and valleys of the mountain. This line should be a list of integers separated by single spaces, with minimum 0 and maximum 100.  You can include numbers larger than 100 or have all numbers smaller than 100 but this will affect the speed of the climbers, so for consistency I suggest having 100 as the maximum.  The mountain should alternate between peaks and valleys, eg. 0 50 25 75 50 100 0.  It should not have corners that are not peaks or valleys, eg. the 40 in the sequence 0 40 70 0 should be removed.

The second line describes the initial positions of the climbers.  This is a list of x positions separated by single spaces.  All sections of mountain have gradient 1 or -1, so to calculate the x position of a peak or valley, go along the mountain adding up the absolute values of the differences.  For example, if the mountain is 0 50 25 75 50 100 0 and I want 2 climbers, both starting at height 50, the 1st one has x position 50 (first peak).  The 2nd climber has x position 50+25+50+25=150 (second valley).

Check your levels before you submit a pull request
- Do the climbers all start at the same height?
- Are all features large enough to be seen clearly even when a climber is nearby?
- Is the level possible? (this is guaranteed if the mountain starts and ends with 0)
