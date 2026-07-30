package DeCell.UI.Backend;

public class DataPair<T> {
    public final String key;
    public final T value;

    public DataPair(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public static <T> DataPair<T> pair(String key, T value) {
        return new DataPair<T>(key, value);
    }
}
