package fr.avianey.minimax4j;

import fr.avianey.minimax4j.ia.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static fr.avianey.minimax4j.Minimax.Algorithm.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class BestMoveTest {

    private final int depth;
    private final Minimax<IAMove> minimax;

    public BestMoveTest(int depth, Minimax<IAMove> minimax) {
        this.depth = depth;
        this.minimax = minimax;
    }

    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
                // minimax
                new Object[]{1, new IA(MINIMAX, 2)},
                new Object[]{2, new IA(MINIMAX, 2)},
                new Object[]{3, new IA(MINIMAX, 2)},
                new Object[]{1, new IA(ALPHA_BETA, 2)},
                new Object[]{2, new IA(ALPHA_BETA, 2)},
                new Object[]{3, new IA(ALPHA_BETA, 2)},
                new Object[]{1, new IA(NEGAMAX, 2)},
                new Object[]{2, new IA(NEGAMAX, 2)},
                new Object[]{3, new IA(NEGAMAX, 2)},
                new Object[]{1, new IA(NEGASCOUT, 2)},
                new Object[]{2, new IA(NEGASCOUT, 2)},
                new Object[]{3, new IA(NEGASCOUT, 2)},
                // parallel minimax
                new Object[]{1, new ParallelIA(NEGAMAX, 2)},
                new Object[]{2, new ParallelIA(NEGAMAX, 2)},
                new Object[]{3, new ParallelIA(NEGAMAX, 2)}
        );
    }

    @Test
    public void shouldBestMoveAlwaysReturnLastAvailableCell() {
        int cell = Logic.GRID_SIZE - 1;
        while (!minimax.isOver()) {
            IAMove move = minimax.getBestMove(depth);
            assertEquals("Best move must be highest available position in grid.", cell, move.getPosition());
            minimax.makeMove(move);
            cell--;
        }
        assertEquals("When over, all cell should be taken.", -1, cell);
    }

}
