package id.thrawnca.connect4;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ConnectFourGridTest {

  // Colour

  @Test
  public void shouldIdentifyOppositeColour() {
    assertEquals(ConnectFourGrid.Colour.White.opposite(), ConnectFourGrid.Colour.Black);
    assertEquals(ConnectFourGrid.Colour.Black.opposite(), ConnectFourGrid.Colour.White);
  }

  // Grid

  @Test
  public void shouldCreateGridOfSelectedSize() {
    ConnectFourGrid grid = new ConnectFourGrid(1, 1);

    assertEquals(grid.getRows(), 1);
    assertEquals(grid.getColumns(), 1);
  }

  @Test
  public void shouldCreateGridOfDefaultSize() {
    ConnectFourGrid grid = new ConnectFourGrid();

    assertEquals(grid.getRows(), 6);
    assertEquals(grid.getColumns(), 7);
  }

  @Test
  public void shouldRejectCalculationsOutsideGrid() {
    ConnectFourGrid grid = new ConnectFourGrid(1, 4);

    try {
      grid.getNextRow(4);
      fail("Should have rejected column 4 (indexed from zero) on size 1x4 grid");
    } catch (IllegalArgumentException e) {
      try {
        grid.getNextRow(-1);
        fail("Should have rejected column -1");
      } catch (IllegalArgumentException e2) {
        assertEquals(grid.getNextRow(0), 0);
        assertEquals(grid.getNextRow(3), 0);
      }
    }
  }

  @Test
  public void shouldReportFullColumn() {
    ConnectFourGrid grid = new ConnectFourGrid(3, 1);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 0);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 0);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 0);

    assertEquals(grid.getNextRow(0), -1);
    assertFalse(grid.addPiece(ConnectFourGrid.Colour.Black, 0));
  }

  @Test
  public void shouldDetectVerticalConnectFour() {
    ConnectFourGrid grid = new ConnectFourGrid(6, 7);

    assertNoConnectFour(grid);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 0);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 0);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 0);

    grid.addPiece(ConnectFourGrid.Colour.White, 0);
    assertSoleConnectFour(grid, 0, 0, 3, 0);
  }

  @Test
  public void shouldDetectHorizontalConnectFour() {
    ConnectFourGrid grid = new ConnectFourGrid(6, 7);

    assertNoConnectFour(grid);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 2);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 5);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 3);

    grid.addPiece(ConnectFourGrid.Colour.White, 4);
    assertSoleConnectFour(grid, 0, 2, 0, 5);
  }

  @Test
  public void shouldDetectDiagonalRisingConnectFour() {
    ConnectFourGrid grid = new ConnectFourGrid(4, 7);

    assertNoConnectFour(grid);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 2);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 5);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 5);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 5);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 5);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 3);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 3);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 4);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 4);

    grid.addPiece(ConnectFourGrid.Colour.White, 4);
    assertSoleConnectFour(grid, 0, 2, 3, 5);
  }

  @Test
  public void shouldDetectDiagonalFallingConnectFour() {
    ConnectFourGrid grid = new ConnectFourGrid(4, 6);

    assertNoConnectFour(grid);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 2);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 2);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 2);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 2);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 5);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 3);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 3);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 3);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 4);

    grid.addPiece(ConnectFourGrid.Colour.White, 4);
    assertSoleConnectFour(grid, 3, 2, 0, 5);
  }

  @Test
  public void shouldDeepCopyOnClone() {
    ConnectFourGrid grid = new ConnectFourGrid(3, 3);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 0);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 1);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 1);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 2);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 2);

    ConnectFourGrid grid2 = grid.clone();

    assertEquals(grid2.getNextRow(0), 1);
    assertEquals(grid2.getNextRow(1), 2);
    assertEquals(grid2.getNextRow(2), 2);

    addPieceWithoutConnectFour(grid2, ConnectFourGrid.Colour.White, 0);
    addPieceWithoutConnectFour(grid2, ConnectFourGrid.Colour.White, 1);
    addPieceWithoutConnectFour(grid2, ConnectFourGrid.Colour.White, 2);

    assertEquals(grid2.colourAt(1, 0), ConnectFourGrid.Colour.White);
    assertEquals(grid2.colourAt(2, 1), ConnectFourGrid.Colour.White);
    assertEquals(grid2.colourAt(2, 2), ConnectFourGrid.Colour.White);

    assertNull(grid.colourAt(1, 0));
    assertNull(grid.colourAt(2, 1));
    assertNull(grid.colourAt(2, 2));
  }

  @Test
  public void shouldRemoveAllPieces() {
    ConnectFourGrid grid = new ConnectFourGrid(2, 2);

    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.Black, 0);
    addPieceWithoutConnectFour(grid, ConnectFourGrid.Colour.White, 1);

    grid.zap();

    assertEquals(grid.getNextRow(0), 0);
    assertEquals(grid.getNextRow(1), 0);
  }

  private void addPieceWithoutConnectFour(ConnectFourGrid grid, ConnectFourGrid.Colour colour, int column) {
    assertTrue(grid.addPiece(colour, column), "Failed to add piece in column " + column);
    assertNoConnectFour(grid);
  }

  private void assertNoConnectFour(ConnectFourGrid grid) {
    for (int i = 0; i < grid.getRows(); i++) {
      for (int j = 0; j < grid.getColumns(); j++) {
        assertFalse(grid.connectsFour(i, j));
      }
    }
  }

  private void assertSoleConnectFour(ConnectFourGrid grid, int startX, int startY, int endX, int endY) {
    int xIncrement = (endX - startX) / 3;
    int yIncrement = (endY - startY) / 3;
    for (int i = 0; i < grid.getRows(); i++) {
      for (int j = 0; j < grid.getColumns(); j++) {
        if (i == startX && j == startY
          || i == startX + xIncrement && j == startY + yIncrement
          || i == endX - xIncrement && j == endY - yIncrement
          || i == endX && j == endY
        ) {
          assertTrue(grid.connectsFour(i, j), "Expected (" + i + "," + j + ") to be part of a connect 4");
        } else {
          assertFalse(grid.connectsFour(i, j), "Did not expect (" + i + "," + j + ") to be part of a connect 4");
        }
      }
    }
  }

}
