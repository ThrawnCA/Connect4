package id.thrawnca.connect4;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

/**
 * Exercises minor AI, detecting immediate potential wins for either side.
 */
public final class EasyAI extends AbstractAI {

  /**
   * Constructs a new EasyAI choosing moves for <code>aiColour</code> on
   * <code>grid</code>.
   */
  public EasyAI(ConnectFourGrid grid, Colour aiColour) {
    super(grid, aiColour);
  }

  /**
   * Chooses a non-full column. If an immediate win is available, that
   * column is chosen; else if an immediate win for the opponent must
   * be blocked, that column is chosen; else the choice is random.
   * @return The index of the chosen column.
   */
  public int chooseColumn() {
    int[] columns;  // contains indices of columns in which the AI should play

    if ((columns = getThreats(grid, aiColour)).length > 0 // immediate win exists
        ||
        // immediate opponent win must be blocked
        (columns = getThreats(grid, aiColour.opposite())).length > 0
        ) {
      // if n-way threat: choose one if its columns at random
      return columns[(int) (Math.random() * columns.length)];
    } else return pickColumn(grid);
  }
}
