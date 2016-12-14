package dzida.server.app.basic.unit;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Graph<T> {
    private final List<T> elements;
    private final int[][] ids;

    private Graph(List<T> elements, int[][] ids) {
        this.elements = elements;
        this.ids = ids;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(Graph<T> graph) {
        return new Builder<>(graph);
    }

    public int[] getNeighbourIds(int nodeId) {
        return ids[nodeId];
    }

    public int getNodeId(T node) {
        int indexOf = elements.indexOf(node);
        if (indexOf < 0) {
            throw new RuntimeException("Couldn't find node:" + node + " in the graph of: " + elements);
        }
        return indexOf;
    }

    public T getElement(int nodeId) {
        //noinspection unchecked
        return elements.get(nodeId);
    }

    public List<T> getNeighbours(T node) {
        int nodeId = getNodeId(node);
        int[] neighbourIds = getNeighbourIds(nodeId);

        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int id : neighbourIds) {
            builder.add(getElement(id));
        }

        return builder.build();
    }

    public int size() {
        return elements.size();
    }

    public List<T> getAllNodes() {
        //noinspection unchecked
        return elements;
    }

    public static final class Builder<T> {
        private List<T> elements;
        private List<List<Integer>> ids;

        public Builder() {
            elements = new ArrayList<>(10);
            ids = new ArrayList<>(10);
        }

        public Builder(Graph<T> graph) {
            List<T> allNodes = graph.getAllNodes();
            int size = allNodes.size();
            elements = new ArrayList<>(size + 2);
            elements.addAll(allNodes);
            ids = new ArrayList<>(size + 2);
            for (int i = 0; i < size; i++) {
                int[] nodeIds = graph.getNeighbourIds(i);
                List<Integer> childList = new ArrayList<>(nodeIds.length);
                for (int nodeId : nodeIds) {
                    childList.add(nodeId);
                }
                ids.add(childList);
            }
        }

        @SafeVarargs
        public final Builder<T> put(T node, T... children) {
            int nodeId = tryAddNode(node);
            for (T child : children) {
                int childId = tryAddNode(child);
                if (!ids.get(nodeId).contains(childId)) {
                    ids.get(nodeId).add(childId);
                }
            }
            return this;
        }

        public final Builder<T> put(T node, List<T> children) {
            //noinspection unchecked
            return put(node, (T[]) children.toArray());
        }

        private int tryAddNode(T node) {
            int index = elements.indexOf(node);
            if (index >= 0) {
                return index;
            }
            elements.add(node);
            ids.add(new ArrayList<>());
            return elements.size() - 1;
        }

        public Graph<T> build() {
            int[][] idsArray = new int[elements.size()][];
            for (int i = 0; i < idsArray.length; i++) {
                List<Integer> integers = ids.get(i);
                idsArray[i] = new int[integers.size()];
                for (int j = 0; j < integers.size(); j++) {
                    idsArray[i][j] = integers.get(j);
                }
            }
            return new Graph<>(ImmutableList.copyOf(elements), idsArray);
        }
    }
}
