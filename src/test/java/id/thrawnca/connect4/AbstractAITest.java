package id.thrawnca.connect4;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

}
