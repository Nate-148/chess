package application;

import java.util.*;

// A tree of future positions stemming from a current root position.
// Nate Hunter - 05/13/2022
public class PositionTree {
	// The status of a position.
	// This is part of the position tree because future positions are needed to evaluate status.
	public static enum Status {
		NORMAL,
		CHECK,
		CHECKMATE,
		STALEMATE,
		ILLEGAL
	}
	
	// The current root position of the tree.
	public Position rootPosition;
	// The status of the root position of the tree.
	public Status status;
	// The legal future positions branching from the tree.
	public Map<Move, PositionTree> futureBranches;
	// The number of layers of future branches proceeding from the root.
	public int depth;
	
	// "Grows" a position tree from the specified position and completely fills out its
	// future position branches to the depth specified.
	public static PositionTree grow(Position position, int depth) {
		// Initialize the root of the tree.
		PositionTree tree = new PositionTree();
		tree.rootPosition = position;
		tree.depth = depth;
		
		// Compute possible moves and validate the position.
		position.computeAllMoves();
		if (!position.valid()) {
			// Indicate that the position should not be reachable.
			tree.status = Status.ILLEGAL;
			return tree;
		}
		
		// Grow branches for every legal move.
		boolean branching = (depth > 0);
		if (branching) {
			tree.futureBranches = new HashMap<Move, PositionTree>();
			for (Move move : position.moves) {
				Position futurePosition = position.nextPosition(move);
				PositionTree futureBranch = PositionTree.grow(futurePosition, depth - 1);
				// Only legal moves should be saved.
				if (futureBranch.status != Status.ILLEGAL) {
					tree.futureBranches.put(move, futureBranch);
					// Update the move notation as needed.
					if (futureBranch.status == Status.CHECK)
						move.notateCheck();
					else if (futureBranch.status == Status.CHECKMATE)
						move.notateCheckmate();
				}
			}
			
			// Determine the status of the position.
			if (position.inCheck()) {
				if (tree.futureBranches.isEmpty())
					tree.status = Status.CHECKMATE;
				else tree.status = Status.CHECK;
			}
			else if (tree.futureBranches.isEmpty())
				tree.status = Status.STALEMATE;
			else tree.status = Status.NORMAL;
		}
		else {
			// Determine the status of the position.
			// Checkmate and stalemate cannot be evaluated at this base level, but check can.
			if (position.inCheck())
				tree.status = Status.CHECK;
			else tree.status = Status.NORMAL;
		}
		return tree;
	}
	
	// Gets the legal moves of the root position.
	// This method assumes that the position tree has been grown to at least depth 1.
	public Set<Move> getLegalMoves() {
		return futureBranches.keySet();
	}
}
