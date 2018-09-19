package ai;

public class TreeNode {
    public TreeNode(int score, TreeNode firstChild, TreeNode nextSibling) {
        this.score = score;
        this.firstChild = firstChild;
        this.nextSibling = nextSibling;
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

    public void setChildren(TreeNode[] children) {
        TreeNode lastNode = null;
        for (int i = 0; i < 6; i++) {

            if (i == 0) {
                setFirstChild(children[i]);
                lastNode = getFirstChild();
            } else {
                lastNode.setNextSibling(children[i]);
                lastNode = lastNode.getNextSibling();
            }

        }
    }
}
