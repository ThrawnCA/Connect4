package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static id.thrawnca.connect4.ConnectFourGrid.Colour.*;
import static org.testng.Assert.*;

/**
 * Verify that the 'easy' AI setting can detect opportunities for
 * immediate victory by one side.
 */
public class EasyAITest {

  private ConnectFourGrid grid;
  private AbstractAI ai;

  @BeforeMethod
  public void setUp(){
    grid = new ConnectFourGrid(6, 7);
    ai = new EasyAI(grid, Black);
  }

  @Test
  public void shouldPlayRandomlyByDefault() {
    int column = ai.chooseColumn();
    for (int i = 0; i < 10; i++) {
      if (column != ai.chooseColumn()) {
        return;
      }
    }
    fail("AI chose a column consistently");
  }

  @Test
  public void shouldDetectImmediateWins() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 2);
      grid.addPiece(Black, 3);
    }

    // verify the AI's choice multiple times so we know it's not random
    for (int i = 0; i < 10; i++) {
      assertEquals(ai.chooseColumn(), 3);
    }
  }

  @Test
  public void shouldDetectImmediateLosses() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 2);
    }

    // verify the AI's choice multiple times so we know it's not random
    for (int i = 0; i < 10; i++) {
      assertEquals(ai.chooseColumn(), 2);
    }
  }

}
