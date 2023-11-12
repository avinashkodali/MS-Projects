package com.example.Context_Monitoring_Application.database;

public class RatingOfSymptom {
    private Double nausea;
    private Double headache;
    private Double diarrhea;
    private Double soreThroat;
    private Double fever;
    private Double muscleAche;
    private Double lossOfSmellTaste;
    private Double cough;
    private Double breathDifficulty;
    private Double feelingTired;

    public RatingOfSymptom() {
    }

    public Double getNausea() {
        return nausea;
    }

    public void setNausea(Double nausea) {
        this.nausea = nausea;
    }

    public Double getHeadache() {
        return headache;
    }

    public void setHeadache(Double headache) {
        this.headache = headache;
    }

    public Double getDiarrhea() {
        return diarrhea;
    }

    public void setDiarrhea(Double diarrhea) {
        this.diarrhea = diarrhea;
    }

    public Double getSoreThroat() {
        return soreThroat;
    }

    public void setSoreThroat(Double soreThroat) {
        this.soreThroat = soreThroat;
    }

    public Double getFever() {
        return fever;
    }

    public void setFever(Double fever) {
        this.fever = fever;
    }

    public Double getMuscleAche() {
        return muscleAche;
    }

    public void setMuscleAche(Double muscleAche) {
        this.muscleAche = muscleAche;
    }

    public Double getLossOfSmellTaste() {
        return lossOfSmellTaste;
    }

    public void setLossOfSmellTaste(Double lossOfSmellTaste) {
        this.lossOfSmellTaste = lossOfSmellTaste;
    }

    public Double getCough() {
        return cough;
    }

    public void setCough(Double cough) {
        this.cough = cough;
    }

    public Double getBreathDifficulty() {
        return breathDifficulty;
    }

    public void setBreathDifficulty(Double breathDifficulty) {
        this.breathDifficulty = breathDifficulty;
    }

    public Double getFeelingTired() {
        return feelingTired;
    }

    public void setFeelingTired(Double feelingTired) {
        this.feelingTired = feelingTired;
    }

    @Override
    public String toString() {
        return "SymptomsRating{" +
                "nausea=" + nausea +
                ", headache=" + headache +
                ", diarrhea=" + diarrhea +
                ", soreThroat=" + soreThroat +
                ", fever=" + fever +
                ", muscleAche=" + muscleAche +
                ", lossOfSmellTaste=" + lossOfSmellTaste +
                ", cough=" + cough +
                ", breathDifficulty=" + breathDifficulty +
                ", feelingTired=" + feelingTired +
                '}';
    }

}


