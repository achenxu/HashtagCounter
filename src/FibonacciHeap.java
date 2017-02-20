/**
 * Created by tom on 2/19/17.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Fibonacci heap.
 *
 */
class FibonacciHeap<T>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final double oneOverLogPhi =
            1.0 / Math.log((1.0 + Math.sqrt(5.0)) / 2.0);

    //~ Instance fields --------------------------------------------------------

    /**
     * Points to the minimum node in the heap.
     */
    private FibonacciHeapNode<T> maxNode;

    /**
     * Number of nodes in the heap.
     */
    private int numNodes;


    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a FibonacciHeap object that contains no elements.
     */
    public FibonacciHeap()
    {
    } // FibonacciHeap

    //~ Methods ----------------------------------------------------------------

    /**
     * Removes all elements from this heap.
     */
    public void clear()
    {
        maxNode = null;
        numNodes = 0;
    }

    // clear



    /**
     * Inserts a new data element into the heap. No heap consolidation is
     * performed at this time, the new node is simply inserted into the root
     * list of this heap.
     *
     * <p>Running time: O(1) actual</p>
     *
     * @param node new node to insert into heap
     * @param key key value associated with data object
     */
    public void insert(FibonacciHeapNode<T> node, double key)
    {
        node.key = key;

        // concatenate node into max list
        if (maxNode != null) {
            node.left = maxNode;
            node.right = maxNode.right;
            maxNode.right = node;
            node.right.left = node;

            if (key > maxNode.key) {
                maxNode = node;
            }
        } else {
            maxNode = node;
        }

        numNodes++;
    }

    // insert

    /**
     * Returns the smallest element in the heap. This smallest element is the
     * one with the minimum key value.
     *
     * <p>Running time: O(1) actual</p>
     *
     * @return heap node with the smallest key
     */
    public FibonacciHeapNode<T> max()
    {
        return maxNode;
    }

    // max

    /**
     * Removes the smallest element from the heap. This will cause the trees in
     * the heap to be consolidated, if necessary.
     *
     * <p>Running time: O(log n) amortized</p>
     *
     * @return node with the smallest key
     */
    public FibonacciHeapNode<T> removeMax()
    {
        FibonacciHeapNode<T> z = maxNode;

        if (z != null) {
            int numKids = z.degree;
            FibonacciHeapNode<T> x = z.child;
            FibonacciHeapNode<T> tempRight;

            // for each child of z do cutting and adding to maxList
            while (numKids > 0) {
                tempRight = x.right;

                // remove x from child list
                x.left.right = x.right;
                x.right.left = x.left;

                // add x to root list of heap
                x.left = maxNode;
                x.right = maxNode.right;
                maxNode.right = x;
                x.right.left = x;

                // set parent[x] to null
                x.parent = null;
                x = tempRight;
                numKids--;
            }

            // remove z from root list of heap
            z.left.right = z.right;
            z.right.left = z.left;

            if (z == z.right) {
                maxNode = null;
            } else {
                maxNode = z.right;
                consolidate();
            }

            // decrement size of heap
            numNodes--;
        }

        return z;
    }

    // removeMax



    protected void consolidate()
    {
        int arraySize =
                ((int) Math.floor(Math.log(numNodes) * oneOverLogPhi)) + 1;

        List<FibonacciHeapNode<T>> array =
                new ArrayList<FibonacciHeapNode<T>>(arraySize);

        // Initialize degree array
        for (int i = 0; i < arraySize; i++) {
            array.add(null);
        }

        // Find the number of root nodes.
        int numRoots = 0;
        FibonacciHeapNode<T> x = maxNode;

        if (x != null) {
            numRoots++;
            x = x.right;

            while (x != maxNode) {
                numRoots++;
                x = x.right;
            }
        }

        // For each node in root list do...
        while (numRoots > 0) {
            // Access this node's degree..
            int d = x.degree;
            FibonacciHeapNode<T> next = x.right;

            // ..and see if there's another of the same degree.
            for (;;) {
                FibonacciHeapNode<T> y = array.get(d);
                if (y == null) {
                    // Nope.
                    break;
                }

                // There is, make one of the nodes a child of the other.
                // Do this based on the key value.
                if (x.key < y.key) {
                    FibonacciHeapNode<T> temp = y;
                    y = x;
                    x = temp;
                }

                // FibonacciHeapNode<T> y disappears from root list.
                link(y, x);

                // We've handled this degree, go to next one.
                array.set(d, null);
                d++;
            }

            // Save this node for later when we might encounter another
            // of the same degree.
            array.set(d, x);

            // Move forward through list.
            x = next;
            numRoots--;
        }

        // Set max to null (effectively losing the root list) and
        // reconstruct the root list from the array entries in array[].
        maxNode = null;

        for (int i = 0; i < arraySize; i++) {
            FibonacciHeapNode<T> y = array.get(i);
            if (y == null) {
                continue;
            }

            // We've got a live one, add it to root list.
            if (maxNode != null) {
                // First remove node from root list.
                y.left.right = y.right;
                y.right.left = y.left;

                // Now add to root list, again.
                y.left = maxNode;
                y.right = maxNode.right;
                maxNode.right = y;
                y.right.left = y;

                // Check if this is a new min.
                if (y.key > maxNode.key) {
                    maxNode = y;
                }
            } else {
                maxNode = y;
            }
        }
    }

    // consolidate

    /**
     * Make node y a child of node x.
     *
     * <p>Running time: O(1) actual</p>
     *
     * @param y node to become child
     * @param x node to become parent
     */
    protected void link(FibonacciHeapNode<T> y, FibonacciHeapNode<T> x)
    {
        // remove y from root list of heap
        y.left.right = y.right;
        y.right.left = y.left;

        // make y a child of x
        y.parent = x;

        if (x.child == null) {
            x.child = y;
            y.right = y;
            y.left = y;
        } else {
            y.left = x.child;
            y.right = x.child.right;
            x.child.right = y;
            y.right.left = y;
        }

        // increase degree[x]
        x.degree++;

        // set mark[y] false
        y.mark = false;
    }

    // link

    /**
     * Increases the key value for a heap node, given the new value to take on.
     * The structure of the heap may be changed and will not be consolidated.
     *
     * <p>Running time: O(1) amortized</p>
     *
     * @param x node to decrease the key of
     * @param k new key value for node x
     *
     * @exception IllegalArgumentException Thrown if k is larger than x.key
     * value.
     */
    public void increaseKey(FibonacciHeapNode<T> x, double k)
    {
        x.key += k;

        FibonacciHeapNode<T> y = x.parent;

        if ((y != null) && (x.key > y.key)) {
            cut(x, y);
            cascadingCut(y);
        }

        if (x.key > maxNode.key) {
            maxNode = x;
        }
    }

    // increaseKey

    /**
     * The reverse of the link operation: removes x from the child list of y.
     * This method assumes that min is non-null.
     *
     * <p>Running time: O(1)</p>
     *
     * @param x child of y to be removed from y's child list
     * @param y parent of x about to lose a child
     */
    protected void cut(FibonacciHeapNode<T> x, FibonacciHeapNode<T> y)
    {
        // remove x from childlist of y and decrement degree[y]
        x.left.right = x.right;
        x.right.left = x.left;
        y.degree--;

        // reset y.child if necessary
        if (y.child == x) {
            y.child = x.right;
        }

        if (y.degree == 0) {
            y.child = null;
        }

        // add x to root list of heap
        x.left = maxNode;
        x.right = maxNode.right;
        maxNode.right = x;
        x.right.left = x;

        // set parent[x] to nil
        x.parent = null;

        // set mark[x] to false
        x.mark = false;
    }

    // cut

    /**
     * Performs a cascading cut operation. This cuts y from its parent and then
     * does the same for its parent, and so on up the tree.
     *
     * <p>Running time: O(log n); O(1) excluding the recursion</p>
     *
     * @param y node to perform cascading cut on
     */
    protected void cascadingCut(FibonacciHeapNode<T> y)
    {
        FibonacciHeapNode<T> z = y.parent;

        // if there's a parent...
        if (z != null) {
            // if y is unmarked, set it marked
            if (!y.mark) {
                y.mark = true;
            } else {
                // it's marked, cut it from parent
                cut(y, z);

                // cut its parent as well
                cascadingCut(z);
            }
        }
    }

    // cascadingCut

}

