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
  public void shouldDetectImmediateWins() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 0);
      grid.addPiece(Black, 1);
    }
    grid.addPiece(Black, 3);
    grid.addPiece(Black, 5);

    assertColumnChoice(1);
  }

  @Test
  public void shouldDetectImmediateLosses() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 0);
    }
    grid.addPiece(Black, 3);
    grid.addPiece(Black, 5);

    assertColumnChoice(0);
  }

  @Test
  public void shouldDetectAvailableNWay() {
    grid.addPiece(Black, 1);
    grid.addPiece(Black, 3);

    assertColumnChoice(2);

    grid.zap();

    /*
     * Black should play in the sixth column despite the white threat
     * in the third column.
     *
     * - B - W - - B
     * - W B W B B W
     * W B W B W B W
     */
    grid.addPiece(White, 0);
    grid.addPiece(Black, 1);
    grid.addPiece(White, 2);
    grid.addPiece(Black, 3);
    grid.addPiece(White, 4);
    grid.addPiece(Black, 5);
    grid.addPiece(White, 6);

    grid.addPiece(White, 1);
    grid.addPiece(Black, 2);
    grid.addPiece(White, 3);
    grid.addPiece(Black, 4);
    grid.addPiece(Black, 5);
    grid.addPiece(White, 6);

    grid.addPiece(Black, 1);
    grid.addPiece(White, 3);
    grid.addPiece(Black, 6);
    assertColumnChoice(5);
  }

  @Test
  public void shouldDetectThreatenedNWay() {
    grid.addPiece(White, 3);
    grid.addPiece(White, 5);

    assertColumnChoice(4);
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

  @Test
  public void shouldDetectBadColumn() {
    grid.addPiece(Black, 0);
    grid.addPiece(White, 1);
    grid.addPiece(Black, 2);

    grid.addPiece(White, 0);
    grid.addPiece(White, 1);
    grid.addPiece(White, 2);

    assertColumnAvoidance(3);
  }

}
