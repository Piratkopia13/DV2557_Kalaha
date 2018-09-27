package ai;

import java.io.*;
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

    public void add(HashMap<Integer, Integer> otherMap) {
        book.putAll(otherMap);
    }

    public void saveToFile() {

        try {
            FileOutputStream fileOut = new FileOutputStream("opening.book");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(book);
            out.close();
            fileOut.close();
            System.out.printf("Book saved to file");
        } catch (IOException i) {
            i.printStackTrace();
        }

    }

    public void readFromFile() {

        try {
            FileInputStream fileIn = new FileInputStream("opening.book");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            book = (HashMap<Integer, Integer>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }

    }

}
