package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static id.thrawnca.connect4.ConnectFourGrid.Colour.*;

/**
 * Verify that the 'easy' AI setting can detect opportunities for
 * immediate victory by one side.
 */
public class EasyAITest extends AbstractAITest {

  @BeforeMethod
  public void setUp(){
    super.setUp();
    ai = new EasyAI(grid, Black);
  }

  @Test
  public void shouldDetectImmediateWins() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 2);
      grid.addPiece(Black, 3);
    }

    assertColumnChoice(3);
  }

  @Test
  public void shouldDetectImmediateLosses() {
    for (int i = 0; i < 3; i++) {
      grid.addPiece(White, 2);
    }

    assertColumnChoice(2);
  }

}
