package id.thrawnca.connect4;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

/**
 * Detects:<br>
 *<ul>
 *  <li>immediate potential wins for either side</li>
 *  <li>potential n-ways for either side</li>
 *  <li>columns in which it would be unwise to go.</li>
 *</ul>
 * @author Carl Antuar
 */
public final class MediumAI extends AbstractAI {

  /**
   * Constructs a new MediumAI choosing moves for <code>aiColour</code> on
   * <code>grid</code>.
   */
  public MediumAI(ConnectFourGrid grid, Colour aiColour) {
    super(grid, aiColour);
  }

  public int chooseColumn() {
    ConnectFourGrid alternateGrid;
    int[] columns;
    if ((columns = getThreats(grid, aiColour)).length > 0 // immediate win
        ||
        // blocking immediate opponent win
        (columns = getThreats(grid, aiColour.opposite())).length > 0
        ||
        // constructing n-way
        (columns = findNWays(grid, aiColour)).length > 0
        ||
        // blocking opponent n-way
        (columns = findNWays(grid, aiColour.opposite())).length > 0
        ) {
      // if one of the above found: choose a random instance of it
      return columns[(int) (Math.random() * columns.length)];
    }

    // check for 'bad' columns that would allow opponent win
    boolean[] badColumns = new boolean[grid.getColumns()];
    boolean goodPlayExists = false;
    for (int i = 0; i < grid.getColumns(); i++) {
      alternateGrid = (ConnectFourGrid) grid.clone();

      // add a piece and check for threats
      alternateGrid.addPiece(aiColour, i);
      int[] threats = getThreats(alternateGrid, aiColour.opposite());

      // columns creating opponent threats are bad
      if (threats.length > 0) {
        debug(i+" would allow win");
        badColumns[i] = true;
        continue;
      }

      // columns allowing opponent n-way are bad
      if (findNWays(alternateGrid, aiColour.opposite()).length > 0) {
        debug("allowed by "+i);
        badColumns[i] = true;
        continue;
      }

      // column is still okay, therefore is 'good' if non-full
      if (grid.getNextRow(i) != -1) goodPlayExists = true;
    }

    // avoid bad columns if good play exists
    int column;
    do {
      column = pickColumn(grid);  // choose a random column until non-bad
    } while (goodPlayExists && badColumns[column] == true);

    return column;
  }
}
