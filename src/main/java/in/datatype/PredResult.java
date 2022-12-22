package in.datatype;

import java.io.Serializable;

public class PredResult implements Serializable{
    private String scenario;
    private String result;
    private float accuracy;
    private float error;

    public PredResult(String prediction, String accScore) {
        String[] pr = prediction.split(":");
        scenario = pr[0];
        result = pr[1];
        String[] ar = accScore.split(":");
        accuracy = Float.parseFloat(ar[0]);
        error = Float.parseFloat(ar[1]);
    }

    public String getScenario() {
        return scenario;
    }

    public String getResult() {
        return result;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getError() {
        return error;
    }

    @Override
    public String toString() {
        return "{" +
                "scenario='" + scenario + '\'' +
                ", result='" + result + '\'' +
                ", accuracy=" + accuracy +
                ", error=" + error +
                '}';
    }
}
