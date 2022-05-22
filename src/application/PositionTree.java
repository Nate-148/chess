package application;

import java.util.ArrayList;

// A tree of future positions stemming from a current root position.
// TODO Flesh out class and add to arbiter and bot.
// Nate Hunter - 05/13/2022
public class PositionTree {
	// The status of a position (e.g. check, checkmate, stalemate).
	// This is part of the position tree because future positions are needed to evaluate status.
	public static enum Status {
		NORMAL,
		CHECK,
		CHECKMATE,
		STALEMATE,
		ILLEGAL,
		UNKNOWN
	}
	
	// The current root position of the tree.
	public Position rootPosition;
	// The status of the root position of the tree.
	public Status status;
	// The move that lead to the current root position.
	public Move previousMove;
	// The future positions branching from the tree.
	public ArrayList<PositionTree> futureBranches;
	
	// "Grows" a position tree from the specified position and completely fills out its
	// future position branches to the depth specified.
	public static PositionTree grow(Position position, Move previousMove, int depth) {
		PositionTree tree = new PositionTree();
		tree.rootPosition = position;
		tree.previousMove = previousMove;
		// TODO Use moves for evaluation in non-branching case.
		ArrayList<Move> moves = position.computeMovesAndValidateSelf();
		if (position.valid) {
			boolean branching = (depth > 0);
			if (branching) {
				// Create branches for every possible next move.
				tree.futureBranches = new ArrayList<PositionTree>();
				for (Move move : moves) {
					Position futurePosition = position.nextPosition(move);
					PositionTree futureBranch = PositionTree.grow(futurePosition, move, depth - 1);
					tree.futureBranches.add(futureBranch);
				}
			} else {
				// Indicate that the position cannot be fully evaluated at this base level.
				tree.status = Status.UNKNOWN;
			}
		} else {
			// Indicate that the position should not be reachable.
			tree.status = Status.ILLEGAL;
		}
		return tree;
	}
}
