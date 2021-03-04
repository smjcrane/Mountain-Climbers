package com.gmail.mountainapp.scrane.mountainclimbers;

import android.util.Log;

import java.util.List;

public class TutorialGame extends Game {

    List<TutorialInstruction> instructionList;
    int instructionIndex;

    public TutorialGame(Mountain mountain, List<TutorialInstruction> instructions){
        super(mountain);
        this.instructionList = instructions;
        this.instructionIndex = 0;
    }

    public TutorialInstruction getInstruction(){
        if (instructionIndex >= instructionList.size()){
            return instructionList.get(instructionList.size() - 1);
        }
        TutorialInstruction instruction = instructionList.get(instructionIndex);
        if (instruction.getObjectID() >= 0){
            instruction.setClimber(climbers.get(instruction.getObjectID()));
        }
        return instruction;
    }

    public void markAsDone(){
        updateVictory();
        if (!victory){
            instructionList.get(instructionIndex).markAsDone();
            Log.d("TUT", instructionList.get(instructionIndex).getText());
            instructionIndex = instructionIndex + 1;
            updateVictory();
        }
    }

    @Override
    public void updateVictory(){
        if (instructionIndex >= instructionList.size()){
            victory = true;
        }
    }

}
