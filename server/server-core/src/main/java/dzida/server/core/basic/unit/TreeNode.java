package dzida.server.core.basic.unit;

import com.google.common.base.Objects;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


public final class TreeNode<T> {
    private final T data;
    private final List<TreeNode<T>> children;

    public TreeNode(T data, List<TreeNode<T>> children) {
        this.data = data;
        this.children = children;
    }

    public static <T> TreeNode<T> of(T data, TreeNode<T> ...children) {
        return new TreeNode<>(data, newArrayList(children));
    }

    public T getData() {
        return data;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equal(data, treeNode.data) &&
                Objects.equal(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data, children);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "data=" + data +
                ", children=" + children +
                '}';
    }
}
