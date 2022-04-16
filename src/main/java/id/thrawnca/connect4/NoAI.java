package id.thrawnca.connect4;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

/**
 * Chooses a random column. Uses no AI.
 * @author Carl Antuar
 */
public final class NoAI extends AbstractAI {

  public NoAI(ConnectFourGrid grid, Colour aiColour) {
    super(grid, aiColour);
  }

  /**
   * Chooses a random non-full column.
   * @return The index of the chosen column.
   */
  public int chooseColumn() {
    return pickColumn(grid);
  }

}
