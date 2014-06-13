package fr.avianey.minimax4j.sample.tictactoe;

import fr.avianey.minimax4j.Transposition;

public class TicTacToeTransposition implements Transposition {

	private final int hash;
	private final int currentPlayer;

	public TicTacToeTransposition(int hash, int currentPlayer) {
		this.hash = hash;
		this.currentPlayer = currentPlayer;
	}
	
	public int hashCode() {
		return hash;
	}
	
	public boolean equals(Object o) {
		return o != null && o instanceof TicTacToeTransposition 
				&& ((TicTacToeTransposition) o).hash == hash 
				// useless because user can't skip turns in tictactoe and hash is bijective
				&& ((TicTacToeTransposition) o).currentPlayer == currentPlayer;
	}
	
	public String toString() {
		return Integer.toBinaryString(hash) + " (" + hash + ")";
	}

}
