package ai;

public class TreeNode {
    private int ambo;

    public TreeNode(int ambo) {
        this.score = 1337;
        this.firstChild = null;
        this.nextSibling = null;
        this.ambo = ambo;
    }

    public TreeNode getFirstChild() {
        return firstChild;
    }
    public TreeNode getNextSibling() {
        return nextSibling;
    }

    public void setFirstChild(TreeNode firstChild) {
        this.firstChild = firstChild;
    }

    public void setNextSibling(TreeNode nextSibling) {
        this.nextSibling = nextSibling;
    }

    private TreeNode firstChild;
    private TreeNode nextSibling;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private int score;

    public int getAmbo() {
        return ambo;
    }

    public void setAmbo(int ambo) {
        this.ambo = ambo;
    }
}
