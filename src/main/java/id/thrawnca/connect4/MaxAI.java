package id.thrawnca.connect4;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

/**
 * Uses the maximum AI thus far developed.
 * @author Carl Antuar
 */
public final class MaxAI extends AbstractAI {

  public MaxAI(ConnectFourGrid grid, Colour aiColour) {
    super(grid, aiColour);
  }

  /**
   * Artificial intelligence - try each of the following, in order, until a
   * move is chosen:
   *<ul>
   *  <li>Check whether a column would allow the AI to win.</li>
   *  <li>Check whether a column would block an opposing win.</li>
   *  <li>Check whether a column would give the AI an n-way or consequential
   * threat.</li>
   *  <li>Check whether a column would block an opposing n-way/consequential
   * threat.</li>
   *  <li>Detect columns that, if used by the AI, would allow an opposing win,
   * or an opposing n-way/consequential threat, and avoid these columns if
   * alternatives exist.</li>
   *  <li>Pick a random column.</li>
   *</ul>
   */
  public int chooseColumn() {
    debug("Start choosing");
    int[] columns;          // potential columns
    ConnectFourGrid alternateGrid;  // a cloned grid for testing moves

    /*
     * Test for columns that should be used.
     * Short-circuit of condition testing guarantees proper priority order,
     * ie an immediate win will be used in preference to a blocked opponent
     * win, etc.
     *
     * Consequentials should come after n-ways, so that self-existent n-ways
     * are not attributed to other moves.
     */
    if ((columns = getThreats(grid, aiColour)).length > 0 // immediate win
        ||
        // blocking immediate opponent win
        (columns = getThreats(grid, aiColour.opposite())).length > 0
        ||
        // constructing n-way
        (columns = findNWays(grid, aiColour)).length > 0
        ||
        // constructing consequential
        (columns = findConsequentials(grid, aiColour)).length > 0
        ||
        // blocking opponent n-way
        (columns = findNWays(grid, aiColour.opposite())).length > 0
        ||
        // blocking opponent consequential
        (columns = findConsequentials(grid, aiColour.opposite())).length > 0
        ||
        // constructing setup
        (columns = findSetups(grid, aiColour)).length > 0
        ||
        // blocking opponent setup
        (columns = findSetups(grid, aiColour.opposite())).length > 0
      ) {
      // if one of the above found: choose a random instance of it
      return columns[(int) (Math.random() * columns.length)];
    }

    // check for 'bad' columns that would allow a user win
    boolean[] badColumns = new boolean[grid.getColumns()];
    boolean goodPlayExists = false;
    for (int i = 0; i < grid.getColumns(); i++) {
      if (grid.getNextRow(i) == -1) continue;
debug("is "+i+" bad?");

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

      /*
       * Columns allowing opponent n-way or consequential are bad, unless
       * they stop the opponent with a threat.
       */
      NWayBad:
      if (findNWays(alternateGrid, aiColour.opposite()).length > 0
          ||
          findConsequentials(alternateGrid, aiColour.opposite()).length > 0
          ) {
        if ((threats = getThreats(alternateGrid, aiColour)).length > 0) {
          // can only be 1 threat, or n-way would have been found
          ConnectFourGrid alternateGrid2 = (ConnectFourGrid) alternateGrid.clone();
          alternateGrid2.addPiece(aiColour.opposite(), threats[0]);
          // if blocking threat alleviated danger, column is non-bad
          if ((threats = getThreats(alternateGrid2, aiColour.opposite())).length <= 1
              &&
              findNWays(alternateGrid2, aiColour.opposite()).length == 0
              &&
              findConsequentials(alternateGrid2, aiColour.opposite()).length == 0
            ) {
            break NWayBad;
          }
        }
        debug("allowed by "+i);
        badColumns[i] = true;
        continue;
      }

      /*
       * Columns allowing opponent setup are bad, unless they stop the
       * opponent with a threat.
       */
      SetupBad:
      if (findSetups(alternateGrid, aiColour.opposite()).length > 0) {
        if ((threats = getThreats(alternateGrid, aiColour)).length > 0) {
          // can only be 1 threat, or n-way would have been found
          ConnectFourGrid alternateGrid2 = (ConnectFourGrid) alternateGrid.clone();
          alternateGrid2.addPiece(aiColour.opposite(), threats[0]);
          // if blocking threat alleviated danger, column is non-bad
          if ((threats = getThreats(alternateGrid2, aiColour.opposite())).length <= 1
              &&
              findSetups(alternateGrid2, aiColour.opposite()).length == 0
            ) {
            break SetupBad;
          }
        }
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
      column = pickColumn(grid);
    } while (goodPlayExists && badColumns[column] == true);

    return column;
  }
}
