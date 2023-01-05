package io.blodhgarm.personality.misc.pond.owo;

public interface GridLayoutDuck {

    /**
     * Reset the size of a grid layout only if the Children array contains nothing
     * @param rowSize the given row size
     * @param columnSize the given column size
     * @return if the size was set with the new values
     */
    boolean resetSize(int rowSize, int columnSize);
}
