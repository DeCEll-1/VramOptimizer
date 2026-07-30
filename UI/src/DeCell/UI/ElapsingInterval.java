package DeCell.UI;


public class ElapsingInterval {

    private float minInterval;
    private float maxInterval;

    private float elapsedAmount = 0;
    private boolean intervalElapsed = false;

    public ElapsingInterval(float minInterval, float maxInterval) {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
    }

    public void setInterval(float _minInterval, float _maxInterval) {
        this.minInterval = _minInterval;
        this.maxInterval = _maxInterval;
    }

    public void setElapsed(float elapsedAmount) {
        this.elapsedAmount = elapsedAmount;
    }

    public boolean isElapsed() {
        if (intervalElapsed) {
            intervalElapsed = false;
            return true;
        }
        return false;
    }

    public void advance(float amount) {
        elapsedAmount += amount;

        while (elapsedAmount > maxInterval || elapsedAmount < minInterval) {
            intervalElapsed = true;

            if (elapsedAmount > maxInterval) {
                float extra = elapsedAmount - maxInterval;
                elapsedAmount = minInterval + extra;
            } else { // elapsed is less
                float extra = elapsedAmount - minInterval;
                elapsedAmount = maxInterval + extra;
            }
        }
    }

}
