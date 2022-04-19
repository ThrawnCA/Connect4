package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static id.thrawnca.connect4.ConnectFourGrid.Colour.*;
import static org.testng.Assert.*;

/**
 * Superclass for unit-testing AI logic.
 */
public abstract class AbstractAITest {

  /** verify the AI's choice multiple times so we know it's not random */
  protected static final int RETRIES = 10;

  protected ConnectFourGrid grid;
  protected AbstractAI ai;

  @BeforeMethod
  public void setUp(){
    grid = new ConnectFourGrid(6, 7);
  }

  @Test
  public void shouldPlayRandomlyByDefault() {
    int column = ai.chooseColumn();
    for (int i = 0; i < RETRIES; i++) {
      if (column != ai.chooseColumn()) {
        return;
      }
    }
    fail("AI chose a column consistently");
  }

  protected void assertColumnChoice(int column) {
    for (int i = 0; i < RETRIES; i++) {
      assertEquals(ai.chooseColumn(), column);
    }
  }

  protected void assertColumnAvoidance(int column) {
    for (int i = 0; i < RETRIES; i++) {
      assertNotEquals(ai.chooseColumn(), column, "AI chose a column that should have been avoided");
    }
  }

  protected void detectImmediateWins() {
    grid.zap();
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 0);
      grid.addPiece(Black, 1);
    }
    grid.addPiece(Black, 3);
    grid.addPiece(Black, 5);

    assertColumnChoice(1);
  }

  protected void detectImmediateLosses() {
    grid.zap();
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 0);
    }
    grid.addPiece(Black, 3);
    grid.addPiece(Black, 5);

    assertColumnChoice(0);
  }

  protected void detectAvailableNWay() {
    grid.zap();
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

  protected void detectThreatenedNWay() {
    grid.zap();
    grid.addPiece(White, 3);
    grid.addPiece(White, 5);

    assertColumnChoice(4);
  }

  protected void detectBadColumn() {
    grid.zap();
    grid.addPiece(Black, 0);
    grid.addPiece(White, 1);
    grid.addPiece(Black, 2);

    grid.addPiece(White, 0);
    grid.addPiece(White, 1);
    grid.addPiece(White, 2);

    assertColumnAvoidance(3);
  }
}
