package fr.avianey.minimax4j.ia;

import fr.avianey.minimax4j.Move;

public final class IAMove implements Move {

    private final int position;

    public IAMove(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
