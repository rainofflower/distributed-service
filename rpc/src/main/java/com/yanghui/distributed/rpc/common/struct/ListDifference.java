package com.yanghui.distributed.rpc.common.struct;

import com.yanghui.distributed.rpc.common.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 比较两个list的不同，列出差异部分：包括左侧独有，右侧独有，双方都有
 *
 *
 */
public class ListDifference<T> {

    /**
     * The Only on left.
     */
    private List<T> onlyOnLeft;

    /**
     * The Only on right.
     */
    private List<T> onlyOnRight;

    /**
     * The On both.
     */
    private List<T> onBoth;

    /**
     * Difference list difference.
     *
     * @param left  the left
     * @param right the right
     */
    public ListDifference(List<? extends T> left, List<? extends T> right) {
        if (CommonUtils.isEmpty(left) || CommonUtils.isEmpty(right)) {
            this.onlyOnLeft = Collections.unmodifiableList(left == null ? new ArrayList<T>() : left);
            this.onlyOnRight = Collections.unmodifiableList(right == null ? new ArrayList<T>() : right);
            this.onBoth = Collections.unmodifiableList(new ArrayList<T>());
            return;
        }
        boolean switched = false;
        if (left.size() < right.size()) { // 做优化，比较大小，只遍历少的
            List<? extends T> tmp = left;
            left = right;
            right = tmp;
            switched = true;
        }

        List<T> onlyOnLeft = new ArrayList<T>();
        List<T> onlyOnRight = new ArrayList<T>(right);
        List<T> onBoth = new ArrayList<T>();

        for (T leftValue : left) {
            if (right.contains(leftValue)) {
                onlyOnRight.remove(leftValue);
                onBoth.add(leftValue);
            } else {
                onlyOnLeft.add(leftValue);
            }
        }
        this.onlyOnLeft = Collections.unmodifiableList(switched ? onlyOnRight : onlyOnLeft);
        this.onlyOnRight = Collections.unmodifiableList(switched ? onlyOnLeft : onlyOnRight);
        this.onBoth = Collections.unmodifiableList(onBoth);
    }

    /**
     * Are equal.
     *
     * @return the boolean
     */
    public boolean areEqual() {
        return onlyOnLeft.isEmpty() && onlyOnRight.isEmpty();
    }

    /**
     * Gets only on left.
     *
     * @return the only on left
     */
    public List<T> getOnlyOnLeft() {
        return onlyOnLeft;
    }

    /**
     * Gets only on right.
     *
     * @return the only on right
     */
    public List<T> getOnlyOnRight() {
        return onlyOnRight;
    }

    /**
     * Gets on both.
     *
     * @return the on both
     */
    public List<T> getOnBoth() {
        return onBoth;
    }

}