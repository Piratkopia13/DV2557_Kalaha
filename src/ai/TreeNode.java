package ai;

public class TreeNode {
    public TreeNode(int score) {
        this.score = score;
        this.firstChild = null;
        this.nextSibling = null;
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

}
