package ai;

public class MinimaxTree {

    public MinimaxTree() {
        this.root = new TreeNode(0);
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    private TreeNode root;
}
