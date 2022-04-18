package id.thrawnca.connect4;

import static id.thrawnca.connect4.ConnectFourGrid.Colour;

/**
 * Subclasses of this class represent an artificial intelligence module,
 * capable of choosing a column in a Connect Four grid.
 *
 *<p>Vocabulary:
 *<dl>
 *  <dt>Threat</dt>
 *    <dd>A column in which a player can win with a single move.</dd>
 *  <dt>N-way</dt>
 *    <dd>To have <i>n</i> simultaneous threats, where n > 1; this gives a win
 * one turn later, since only one can be blocked.</dd>
 *  <dt>Consequential</dt>
 *    <dd>A winning situation (threat on one's own turn, n-way, etc) that
 * becomes available when a threat is blocked.</dd>
 *  <dt>Setup</dt>
 *    <dd>A move that does not create a threat, but makes an unblockable n-way
 * or consequential available next turn.</dd>
 *</dl>
 */
public abstract class AbstractAI {

  /** Whether or not to generate debugging messages. */
  public static boolean debug = Boolean.valueOf(System.getProperty("connect4.debug", "false"));

  protected final ConnectFourGrid grid;

  protected final Colour aiColour;

  /**
   * Constructs a new AI choosing moves for <code>aiColour</code>, on
   * <code>grid</code>.
   */
  public AbstractAI(final ConnectFourGrid grid, final Colour aiColour) {
    this.grid = grid;
    this.aiColour = aiColour;
  }

  /**
   * Selects a column for the AI to play in.
   * @return The column chosen by this AI.
   */
  public abstract int chooseColumn();

  /**
   * Helper for <code>chooseColumn</code>. Selects a random, non-full column
   * and returns its index.
   */
  protected static int pickColumn(final ConnectFourGrid grid) {
    int column;
    do {
      column = (int) (Math.random() * grid.getColumns());
    } while (grid.getNextRow(column) == -1);
    return column;
  }

  /**
   * Helper for <code>chooseColumn</code>. Finds and returns the indices of
   * all columns of <code>grid</code> in which <code>colour</code> could win
   * immediately by playing.
   * @return An array containing the indices of columns in which the specified
   * colour could win by playing. NB This array will be full; its length will
   * be the number of such columns.
   */
  protected static int[] getThreats(
      final ConnectFourGrid grid,
      final Colour colour) {
    int[] threatColumns = new int[grid.getColumns()]; // threat positions
    int threats = 0;                  // threat count

    // check each column
    for (int i = 0; i < grid.getColumns(); i++) {
      if (isThreat(grid, i, colour)) {
        threatColumns[threats++] = i; // found connect 4; record column
        debug(colour+" threat @ "+i);
      }
    }

    // copy threat indices into array of exact length
    int[] returnArray = new int[threats];
    System.arraycopy(threatColumns, 0, returnArray, 0, threats);

    // all done
    return returnArray;
  }

  /**
   * Checks <code>column</code> on <code>grid</code> to see whether
   * <code>colour</code> would win immediately by playing there.
   * @return Whether a play by <code>colour</code> in <code>column</code> would
   * be a win for <code>colour</code>.
   */
  private static boolean isThreat(
      final ConnectFourGrid grid,
      final int column,
      final Colour colour) {
    // ensure column is non-full
    int row = grid.getNextRow(column);
    if (row == -1) return false;

    // create a test environment
    ConnectFourGrid alternateGrid = (ConnectFourGrid) grid.clone();

    // add piece to testing grid and check for Connect 4s
    alternateGrid.addPiece(colour, column);
    return alternateGrid.connectsFour(row, column);
  }

  /**
   * Finds all reasonable moves (ie non-full & not giving an immediate
   * opponent win).
   * @return The indices of all columns in which the AI could reasonably play.
   * This array will be full.
   */
  private static int[] getPotentialMoves(
      final ConnectFourGrid grid,
      final Colour colour) {
    final int[] moves = new int[grid.getColumns()];
    int moveCount = 0;

    for (int i = 0; i < grid.getColumns(); i++) {
      // reject full columns
      if (grid.getNextRow(i) == -1) continue;

      // reject columns handing a win to one's opponent
      ConnectFourGrid alternateGrid = (ConnectFourGrid) grid.clone();
      alternateGrid.addPiece(colour, i);

      if (getThreats(alternateGrid, colour.opposite()).length > 0) continue;

      moves[moveCount++] = i;
    }

    return crunch(moves, moveCount);
  }

  /**
   * Helper for <code>chooseColumn</code>. Finds and returns the index of a
   * column that would give the specified colour an n-way or consequent win on
   * <code>grid</code>, or -1 if no such column exists.
   */
  protected int[] findNWays(
      final ConnectFourGrid grid,
      final Colour colour) {
    //debug("findNWays for "+colour.toString());
    return getNWays(grid, colour, true);
  }

  /**
   * The 'workhorse' of <code>findNWay<code>.
   * @param topLevel Whether this call is a top-level call, ie whether it is
   * an analysis of this move or a future one; this is important for not
   * attempting to block unblockable moves.
   */
  private int[] getNWays(
      final ConnectFourGrid grid,
      final Colour colour,
      final boolean topLevel) {
    final int[] nWays = new int[grid.getColumns()];
    int nWayCount = 0;

    ConnectFourGrid alternateGrid;

    // check each column
    int[] columns = getPotentialMoves(grid, colour);  // get safe moves
    for (int i = 0; i < columns.length; i++) {
      // if we could score multiple wins, return this column
      if (isNWay(grid, i, colour)) {
        // threatening an n-way
        debug(aiColour+" found threatened n-way for "+colour.toString()+" at "+columns[i]);

        /*
         * If opponent is threatening, and block is impossible, don't
         * try; it just wastes time and makes the move more obvious.
         *
         * This only applies to top-level calls, ie those
         * determining the AI's immediate move, because it has the
         * effect of concealing the opponent's threatened n-ways, and is
         * therefore undesirable if those threats are consequent on
         * something blockable.
         *
         * In altering this code, remember that exclusion of useless
         * moves is not necessary, and erring on the side of caution is
         * advised.
         */
        if (topLevel && colour == aiColour.opposite()) {
          // reset testing grid to current state
          alternateGrid = (ConnectFourGrid) grid.clone();

          // add a piece of the AI's colour instead
          alternateGrid.addPiece(colour.opposite(), columns[i]);
          if (getThreats(alternateGrid, colour).length > 0) {
            // nothing can be done
            debug("but can't do anything");
            continue;
          }
        }

        nWays[nWayCount++] = columns[i];
      }
    }

    // fit n-ways into array and return
    return crunch(nWays, nWayCount);
  }

  /**
   * Checks whether, if <code>colour</code> played in <code>column</code>
   * on <code>grid</code>, then <code>colour</code> would have created
   * multiple simultaneous threats.
   */
  private static boolean isNWay(
      final ConnectFourGrid grid,
      final int column,
      final Colour colour) {
    // ensure column is non-full
    if (grid.getNextRow(column) < 0) {
      return false;
    }

    // create a test environment
    final ConnectFourGrid alternateGrid = (ConnectFourGrid) grid.clone();

    // add piece to testing grid and check for multiple threats
    alternateGrid.addPiece(colour, column);
    return getThreats(alternateGrid, colour).length > 1;
  }

  /**
   * Finds 'consequential wins', ie columns in which a play would create
   * a threat, the blocking of which by the opponent would allow an AI win
   * or another AI threat, the blocking of which...etc.
   */
  protected int[] findConsequentials(
      final ConnectFourGrid grid,
      final Colour colour) {
    debug("findConsequentials for " + colour);
    return getConsequentials(grid, colour, true);
  }

  /**
   * @param topLevel Whether this call is a top-level call, ie whether it is
   * an analysis of this move or of a future one; this is important for not
   * attempting to block unblockable moves.
   */
  private int[] getConsequentials(
      final ConnectFourGrid grid,
      final Colour colour,
      final boolean topLevel) {
    final int[] consequentials = new int[grid.getColumns()];
    int consCount = 0;

    ConnectFourGrid alternateGrid;

    int[] columns = getPotentialMoves(grid, colour);  // get safe moves
    ColumnSearch:
    for (int i = 0; i < columns.length; i++) {
      // create testing grid
      alternateGrid = (ConnectFourGrid) grid.clone();

      // add one of our pieces and check for threats
      alternateGrid.addPiece(colour, columns[i]);

      // if we have a threat: check whether block gives n-way/consequential
      int[] ourWins = getThreats(alternateGrid, colour);
      if (ourWins.length > 0) {
        /*
         * There can be only one threat at this point; a move allowing more
         * would have been detected as an n-way.
         *
         * Experimentally block the envisaged threat & examine the results.
         */
        int threatColumn = ourWins[0];
        alternateGrid.addPiece(colour.opposite(), threatColumn);

        // if AI immediately has a threat, victory!
        if (isThreat(alternateGrid, threatColumn, colour)) {
          // threat exists after block; consequential win exists
          debug(aiColour+" found threatened consequent win for "+colour.toString()+" at "+columns[i]);
        } else if (isNWay(alternateGrid, threatColumn, colour)  // check for consequent n-way
            ||
            // check for consequent consequential
            (getConsequentials(alternateGrid, colour, false)).length > 0
          ) {
          // blocked threat has allowed n-way or consequential
          debug("allowed by blocked threat at "+threatColumn);
        } else continue ColumnSearch; // nothing found

        /*
         * If opponent is threatening, and block is impossible, don't
         * try; it just wastes time and makes the move more obvious.
         *
         * This only applies to top-level calls, ie those
         * determining the AI's immediate move, because it has the
         * effect of concealing the opponent's threatened
         * consequentials, and is therefore undesirable if those threats
         * are themselves consequent on something blockable.
         *
         * In altering this code, remember that exclusion of useless
         * moves is not necessary, and erring on the side of caution is
         * advised.
         */
        if (topLevel && colour == aiColour.opposite()) {
          alternateGrid = (ConnectFourGrid) grid.clone();

          // add a piece of the computer's colour instead
          alternateGrid.addPiece(colour.opposite(), columns[i]);
          if (getThreats(alternateGrid, colour).length > 0) {
            // opponent could now win directly
            debug("but can't do anything");
            continue;
          }
        }

        consequentials[consCount++] = columns[i];
      }
    }

    // fit consequentials into array and return
    return crunch(consequentials, consCount);
  }

  protected int[] findSetups(final ConnectFourGrid grid, final Colour colour) {
    return getSetups(grid, colour);
  }

  private int[] getSetups(final ConnectFourGrid grid, final Colour colour) {
    final int[] setups = new int[grid.getColumns()];
    int setupCount = 0;

    ConnectFourGrid alternateGrid;

    // test each column to see if it constitutes a setup
    int[] columns = getPotentialMoves(grid, colour);  // get safe moves
    for (int i = 0; i < columns.length; i++) {
      // create testing grid
      alternateGrid = (ConnectFourGrid) grid.clone();

      // add a piece and test if a victory condition is now available
      alternateGrid.addPiece(colour, columns[i]);

      // if the new piece has created a threat for us, assume block
      int[] threats = getThreats(alternateGrid, colour);
      if (threats.length > 0) alternateGrid.addPiece(colour.opposite(), threats[0]);

      // test for unblockable n-ways and consequentials
      if (getNWays(alternateGrid, colour, false).length > 0
            && getNWays(alternateGrid, colour, true).length == 0
          ||
          getConsequentials(alternateGrid, colour, false).length > 0
            && getConsequentials(alternateGrid, colour, true).length == 0
        ) {
        debug(aiColour+" found setup for "+colour+" at "+columns[i]);

        /*
         * If opponent is threatening, and block is impossible, don't
         * try; it just wastes time and makes the move more obvious.
         *
         * In altering this code, remember that exclusion of useless
         * moves is not necessary, and erring on the side of caution is
         * advised.
         */
        if (colour == aiColour.opposite()) {
          alternateGrid = (ConnectFourGrid) grid.clone();

          // add a piece of the computer's colour instead
          alternateGrid.addPiece(colour.opposite(), columns[i]);
          if (getThreats(alternateGrid, colour).length > 0
              ||
              getNWays(alternateGrid, colour, false).length > 0
              ||
              getConsequentials(alternateGrid, colour, false).length > 0
            ) {
            // opponent could still win
            debug("but can't do anything");
            //continue;
          }
        }

        setups[setupCount++] = columns[i];
      }
    }

    // fit setups into array and return
    return crunch(setups, setupCount);
  }

  protected static void debug(final String message) {
    if (debug) {
      System.out.println(message);
    }
  }

  /**
   * Returns a new array of length <code>elements</code>, containing elements
   * 0...n-1 of <code>array</code>.
   */
  private static int[] crunch(final int[] array, final int elements) {
    int[] newArray = new int[elements];
    System.arraycopy(array, 0, newArray, 0, elements);
    return newArray;
  }

  public static void main(String[] args) {
    // Test that the pickColumn method is producing valid results
    System.out.println("Testing pickColumn()");
    ConnectFourGrid grid = new ConnectFourGrid();
    System.out.println("Grid has "+grid.getColumns()+" columns");
    for (int i = 0; i < 100; i++) {
      System.out.print(pickColumn(grid)+" ");
    }
  }

}
