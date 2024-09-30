package ru.khkhlv.balltree;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

@Getter
@Setter
public class BallTree {

    public Node buildTree(List<RealVector> realVectors){
        if(realVectors.isEmpty()){
            return null;
        }
        if (realVectors.size() == 1){
            return new Node(realVectors.get(0));
        }

        realVectors.sort(Comparator.comparingDouble(RealVector::getDimension));

        // разделение списка realVectors на 2 группы и поиск соседей
        int midIndex = realVectors.size()/2;
        Node leftChild = buildTree(realVectors.subList(0, midIndex));
        Node rightChild = buildTree(realVectors.subList(midIndex + 1, realVectors.size()));

        // собираем дерево
        Node root = new Node(realVectors.get(midIndex));
        root.leftChild = leftChild;
        root.rightChild = rightChild;

        return root;
    }


    public List<RealVector> searchTree(Node root, RealVector targetVector, int k) {
        // ищем ближайших соседей среди векторов
        PriorityQueue<RealVector> nearestNeighbours = new PriorityQueue<>(Comparator.comparingDouble(p-> euclideanDistance(p.toArray(), targetVector.toArray())));

        // ищем ближайшие ноды
        PriorityQueue<Node> nodesToVisit = new PriorityQueue<>(Comparator.comparingDouble(node -> euclideanDistance(node.getRealVector().toArray(), targetVector.toArray())));

        nodesToVisit.add(root);

        // реализация knn алгоритма
        while(!nodesToVisit.isEmpty() && (nearestNeighbours.size() < k || euclideanDistance(nodesToVisit.peek().getRealVector().toArray(), targetVector.toArray()) < euclideanDistance(Objects.requireNonNull(nearestNeighbours.peek()).toArray(), targetVector.toArray()))) {
            Node node = nodesToVisit.poll();

            assert node != null;
            nearestNeighbours.add(node.getRealVector());
            if(nearestNeighbours.size() > k) {
                nearestNeighbours.poll();
            }
            if(node.getLeftChild() != null){
                nodesToVisit.add(node.getLeftChild());
            }
            if(node.getRightChild()!= null) {
                nodesToVisit.add(node.getRightChild());
            }
        }
        return new ArrayList<>(nearestNeighbours);
    }

    // рассчитываем дистанцию между векторами
    private double euclideanDistance(double[] vector1, double[] vector2) {
        double sum = 0;
        for (int i = 0; i < vector1.length; i++) {
            sum += Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
