package ai;

import java.util.HashMap;

public class OpeningBook {

    private HashMap<Integer, Integer> book;

    public OpeningBook() {
        this.book = new HashMap<>();
    }

    public int getMove(int hash) {
        return book.get(hash);
    }

    public void setMove(int hash, int ambo) {
        this.book.put(hash, ambo);
    }

    public void saveToFile(String filename) {
        // todo
    }

    public void readFromFile(String filename) {
        // todo
    }

}
