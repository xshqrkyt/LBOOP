package ru.ssau.tk.samsa.LB2.functions;

import ru.ssau.tk.samsa.LB2.exceptions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    private static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        public double x;
        public double y;
        public Node next;
        public Node prev;
    }

    private Node head;
    private int count;

    private void addNode(double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (head == null) {
            head = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
        } else {
            Node last = head.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }
        count++;
    }

    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds");
        }

        Node current = head;
        if (index <= count / 2) {
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            for (int i = count; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        checkLengthIsTheSame(xValues, yValues);
        
        if (xValues.length < 2)
            throw new IllegalArgumentException("Length of array is less than 2");

        checkSorted(xValues);
        
        for (int i = 0; i < xValues.length; i++)
            addNode(xValues[i], yValues[i]);
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2)
            throw new IllegalArgumentException("Count must be at least 2");

        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            double value = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, value);
            }
        } else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
    }

    @Override
    public void remove(int index) {
        if (count == 2)
            throw new IllegalArgumentException("Length of array is 2");
        
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Index out of bounds");

        Node nodeToRemove = getNode(index);

        if (count == 1) {
            head = null;
        } else {
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;

            if (nodeToRemove == head) {
                head = nodeToRemove.next;
            }
        }
        count--;
    }
    
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Incorrect index.");
        
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Incorrect index.");
        
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Incorrect index.");
        
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }
    
    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node currentNode = head;
            private int returnedCount = 0;

            @Override
            public boolean hasNext() {
                return returnedCount < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Point point = new Point(currentNode.x, currentNode.y);
                currentNode = currentNode.next;
                returnedCount++;
                return point;
            }
        };
    }
    
    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < head.x)
            throw new IllegalArgumentException("x = " + x + " is less than left bound " + head.x);
        if (x > head.prev.x) 
            return count;

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x > x)
                return i - 1;
            
            current = current.next;
        }
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, 0);
    }

    @Override
    protected double extrapolateRight(double x) {
        return interpolate(x, count - 2);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (x < getX(floorIndex) || x > getX(floorIndex + 1))
            throw new InterpolationException("The number is not inside the interpolation interval.");

        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    // Оптимизированная версия apply() для X*
    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            Node node = floorNodeOfX(x);
            if (node.x == x) {
                return node.y;
            } else if (node.next.x == x) {
                return node.next.y;
            } else {
                return interpolate(x, node.x, node.next.x, node.y, node.next.y);
            }
        }
    }

    private Node floorNodeOfX(double x) {
        if (x < head.x)
            throw new IllegalArgumentException("x = " + x + " is less than left bound " + head.x);
        if (x > head.prev.x) 
            return head.prev.prev;

        Node current = head;
        while (current.next != head && current.next.x <= x) {
            current = current.next;
        }
        return current;
    }

    @Override
    public void insert(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y))
            throw new IllegalArgumentException("Incorrect value.");
        
        if (head == null) {
            addNode(x, y);
            return;
        }

        // Вставка в начало
        if (x < head.x) {
            Node newNode = new Node();
            newNode.x = x;
            newNode.y = y;
            newNode.next = head;
            newNode.prev = head.prev;
            head.prev.next = newNode;
            head.prev = newNode;
            head = newNode;
            ++count;
            return;
        }

        Node node = head;
        do {
            // Обновление существующего значения
            if (node.x == x) {
                node.y = y;
                return;
            }

            // Вставка между текущим и следующим
            if (node.x < x && (node.next.x > x || node.next == head)) {
                Node newNode = new Node();
                newNode.x = x;
                newNode.y = y;
                newNode.prev = node;
                newNode.next = node.next;
                node.next.prev = newNode;
                node.next = newNode;
                ++count;
                return;
            }

            node = node.next;
        } while (node != head);

        // Вставка в конец (если не сработали другие условия)
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;
        newNode.prev = head.prev;
        newNode.next = head;
        head.prev.next = newNode;
        head.prev = newNode;
        ++count;
    }
}

