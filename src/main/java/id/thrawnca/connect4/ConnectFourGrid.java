package id.thrawnca.connect4;

import java.awt.Color;

/**
 * The grid of a Connect Four game, in which two players take turns dropping
 * pieces into the columns until one player achieves a straight line of four
 * pieces in their own colour.
 *
 *<p>The standard Connect Four grid has 6 rows and 7 columns.
 *
 * @author Carl Antuar
 */
class ConnectFourGrid implements Cloneable, java.io.Serializable {

  public enum Colour {

    Black(Color.BLACK), White(Color.WHITE);

    Colour(Color color) {
      this.color = color;
    }

    private Color color;

    public Color getColor() { return color; }

    public Colour opposite() { return (this == Black)? White : Black; }
  }

  private static final long serialVersionUID = -5564021615011740774L;

  private Colour[][] grid;

  public ConnectFourGrid() {
    this(6, 7);
  }

  public ConnectFourGrid(int rows, int columns) {
    grid = new Colour[rows][columns];
    zap();
  }

  public int getRows() { return grid.length; }

  public int getColumns() { return grid[0].length; }

  /**
   * @param column The column for which to specify the next empty row, indexed
   * from 0.
   * @return The next empty row in the specified column (the row in which a
   * piece would be placed), or -1 if the column is full.
   */
  public int getNextRow(int column) {
    // search upward through rows
    for (int i = 0; i < grid.length; i++) {
      // return index of first empty row
      if (colourAt(i, column) == null) return i;
    }

    // no empty rows; fail
    return -1;
  }

  /**
   * @param row The row of the point under examination, indexed from 0.
   * @param column The column of the point under examination, indexed from 0.
   * @return The colour of the piece at <code>(row, column)</code>.
   */
  public Colour colourAt(int row, int column) {
    return grid[row][column];
  }

  /**
   * @return Whether position <code>(row, column)</code> is part of a Connect
   * Four.
   */
  public boolean connectsFour(int row, int column) {
    Colour colour = colourAt(row, column);

    // check for position being empty
    if (colour == null) return false;

    // check all directions except 'no direction' (x and y components 0)
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        // move on if 'no direction'
        if (i == 0 && j == 0) continue;

        // check this point and the one behind it
        if (checkDirection(row, column, i, j) || checkDirection(row - j, column - i, i, j)) return true;
      }
    }

    // no Connect Four found
    return false;
  }

  /**
   * Private helper for <code>connectsFour</code>. Checks for Connect Fours
   * from the point <code>(row, column)</code>, outward in the direction
   * defined by <code>xDir</code> and <code>yDir</code>. A positive
   * <code>xDir</code> indicates rightward; a positive <code>yDir</code>
   * indicates upward; and of course vice versa for negative values.
   *
   *<p>Eg if xDir = -1 and yDir = 1, the indicated direction is diagonally up
   * and left.
   */
  private boolean checkDirection(int row, int column, int xDir, int yDir) {
    // these variables are not necessary, but do improve readability
    int bottom = 0, top = getRows() - 1, left = 0, right = getColumns() - 1;

    // outside grid -> fail
    if (row < bottom || row > top || column < left || column > right) return false;

    // traverse three spaces comparing colours
    int currRow = row, currCol = column;
    for (int i = 1; i <= 3; i++) {
      // move in the specified direction
      switch (xDir) {
        case -1: currCol--; break;
        case 1: currCol++; break;
      }

      switch (yDir) {
        case -1: currRow--; break;
        case 1: currRow++; break;
      }

      // outside grid -> fail
      if (currRow < bottom || currRow > top || currCol < left || currCol > right) return false;

      // compare colour at current position to colour at starting point
      if (colourAt(currRow, currCol) != colourAt(row, column)) return false;
    }

    // traversed three positions without failing -> connect four
    return true;
  }

  public boolean addPiece(Colour colour, int column) {
    int row = getNextRow(column);

    // column is full -> fail
    if (row == -1) return false;

    // otherwise add a piece
    grid[row][column] = colour;
    return true;
  }

  public void zap() {
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        grid[i][j] = null;
      }
    }
  }

  public Object clone() {
    try {
      ConnectFourGrid clone = (ConnectFourGrid) super.clone();
      clone.grid = new Colour[getRows()][getColumns()];
      for (int i = 0; i < clone.getRows(); i++) {
        for (int j = 0; j < clone.getColumns(); j++) {
          clone.grid[i][j] = grid[i][j];
        }
      }
      return clone;
    } catch (CloneNotSupportedException cnse) {
      throw new Error("CloneNotSupportedException thrown by ConnectFourGrid");
    }
  }

}
