package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static id.thrawnca.connect4.ConnectFourGrid.Colour.*;

/**
 * Verify that the 'medium' AI setting can detect opportunities for
 * immediate victory by one side, or for establishing a forced win.
 */
public class MediumAITest extends AbstractAITest {

  @BeforeMethod
  public void setUp(){
    super.setUp();
    ai = new MediumAI(grid, Black);
  }

  @Test
  public void easyAITests() {
    detectImmediateWins();
    detectImmediateLosses();
  }

  @Test
  public void shouldDetectAvailableNWay() {
    detectAvailableNWay();
  }

  @Test
  public void shouldDetectThreatenedNWay() {
    detectThreatenedNWay();
  }

  @Test
  public void shouldDetectBadColumn() {
    detectBadColumn();
  }

}
