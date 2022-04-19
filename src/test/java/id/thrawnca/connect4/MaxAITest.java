package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static id.thrawnca.connect4.ConnectFourGrid.Colour.*;

/**
 * Verify that the 'max' AI setting can detect opportunities for
 * immediate victory by one side, or for establishing a forced win.
 */
public class MaxAITest extends AbstractAITest {

  @BeforeMethod
  public void setUp(){
    super.setUp();
    ai = new MaxAI(grid, Black);
  }

  @Test
  public void mediumAITests() {
    detectImmediateWins();
    detectImmediateLosses();
    detectAvailableNWay();
    detectThreatenedNWay();
    detectBadColumn();
  }

  @Test
  public void shouldDetectAvailableConsequentialWin() {
    grid.addPiece(White, 0);
    grid.addPiece(White, 1);
    grid.addPiece(Black, 2);
    grid.addPiece(White, 3);
    grid.addPiece(Black, 4);

    grid.addPiece(Black, 0);
    grid.addPiece(Black, 1);
    grid.addPiece(White, 2);
    grid.addPiece(White, 3);
    grid.addPiece(Black, 4);

    grid.addPiece(White, 1);
    grid.addPiece(Black, 2);
    grid.addPiece(Black, 3);

    grid.addPiece(White, 2);
    grid.addPiece(Black, 3);

    grid.addPiece(White, 3);

    assertColumnChoice(4);
  }

}
