package ru.khkhlv.balltree;


import org.apache.commons.math3.linear.RealVector;


public class Node {
    private RealVector realVector;

    public RealVector getRealVector() {
        return realVector;
    }

    private int dimension;
    Node leftChild;
    Node rightChild;

    public Node getLeftChild() {
        return leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public Node(RealVector realVector) {
        this.realVector = realVector;
        this.dimension = realVector.getDimension();
    }
}
