package application;

import java.util.*;

// A computer player.
// Nate Hunter - 07/31/2022
public class Bot {
	// Chooses a move based on the current position.
	public Move move() {
		// TODO Add growth to specific branches.
		PositionTree positionTree = PositionTree.grow(Position.current.copy(), 3);
		Map<Move, Integer> evaluationsByMove = new HashMap<Move, Integer>();
		int badEvaluation = positionTree.rootPosition.whiteToMove ? -1000 : 1000;
		int bestEvaluation = badEvaluation;
		
		// Evaluate all legal moves.
		for (Map.Entry<Move, PositionTree> futureBranch : positionTree.futureBranches.entrySet()) {
			int branchEvaluation = evaluate(futureBranch.getValue());
			bestEvaluation = best(bestEvaluation, branchEvaluation, positionTree.rootPosition.whiteToMove);
			evaluationsByMove.put(futureBranch.getKey(), branchEvaluation);
		}
		Log.writeCalculations(evaluationsByMove.toString());
		
		// Select a random move within some threshold of the best move.
		int EVALUATION_THRESHOLD = 1;
		List<Move> moveOptions = new ArrayList<Move>();
		for (Map.Entry<Move, Integer> evaluationByMove : evaluationsByMove.entrySet()) {
			if (Math.abs(bestEvaluation - evaluationByMove.getValue()) <= EVALUATION_THRESHOLD)
				moveOptions.add(evaluationByMove.getKey());
		}
		int moveIndex = (int)(Math.random() * moveOptions.size());
		return moveOptions.get(moveIndex);
	}
	
	// Evaluates the root position of the tree.
	private int evaluate(PositionTree positionTree) {
		// Evaluate positions that end the game.
		if (positionTree.status == PositionTree.Status.STALEMATE)
			return 0;
		int badEvaluation = positionTree.rootPosition.whiteToMove ? -1000 : 1000;
		if (positionTree.status == PositionTree.Status.CHECKMATE)
			return badEvaluation;
		
		// Evaluate positions at the end of the tree.
		boolean branching = (positionTree.depth > 0);
		if (!branching)
			return positionTree.rootPosition.materialValue + bestMaterialValueToGrab(positionTree.rootPosition);

		// Find the best evaluation of all immediate future positions.
		int bestEvaluation = badEvaluation;
		for (Map.Entry<Move, PositionTree> futureBranch : positionTree.futureBranches.entrySet()) {
			int branchEvaluation = evaluate(futureBranch.getValue());
			bestEvaluation = best(bestEvaluation, branchEvaluation, positionTree.rootPosition.whiteToMove);
		}
		return bestEvaluation;
	}
	
	// Selects the better evaluation from two evaluations.
	// White prefers the most positive evaluation; black prefers the most negative.
	private int best(int evaluationA, int evaluationB, boolean optimizeForWhite) {
		if (optimizeForWhite)
			return Math.max(evaluationA, evaluationB);
		return Math.min(evaluationA, evaluationB);	
	}
	
	// Determines the material value that can be extracted from the juiciest square.
	// This involves identifying what pieces attack each square and evaluating trades.
	private int bestMaterialValueToGrab(Position position) {
		int bestMaterialValue = 0;
		for (int rank = 0; rank < 8; rank++)
			for (int file = 0; file < 8; file++) {
				// Skip squares without capturable pieces.
				Coordinate coordinate = new Coordinate(rank, file);
				char pieceToGrab = position.getPiece(coordinate);
				boolean capturablePiece = Piece.isOpposing(pieceToGrab, position.whiteToMove);
				if (!capturablePiece) continue;
				// Skip unattacked squares.
				final boolean ACTIVE_PLAYER = true;
				List<Character> attackers = position.getAttackers(coordinate, ACTIVE_PLAYER);
				if (attackers.isEmpty()) continue;
				
				// The material value is the value of removing the piece when undefended.
				int pieceCaptureValue = -Piece.materialValue(pieceToGrab);
				List<Character> defenders = position.getAttackers(coordinate, !ACTIVE_PLAYER);
				if (defenders.isEmpty()) {
					bestMaterialValue = best(bestMaterialValue, pieceCaptureValue, position.whiteToMove);
					continue;
				}
				
				// Get the values of all attacking pieces.
				List<Integer> attackerValues = new ArrayList<Integer>();
				for (Character attacker : attackers)
					attackerValues.add(Piece.materialValue(attacker));
				List<Integer> defenderValues = new ArrayList<Integer>();
				for (char defender : defenders)
					defenderValues.add(Piece.materialValue(defender));
				// Sort the pieces from least valuable to most valuable.
				// Optimal players will capture with their least valuable pieces first.
				Collections.sort(attackerValues);
				Collections.sort(defenderValues);
				// The black pieces must be reversed since values are negative.
				if (position.whiteToMove)
					Collections.reverse(defenderValues);
				else Collections.reverse(attackerValues);
				
				// Compute the evaluations for the full series of piece trades.
				List<Integer> tradeEvaluations = new ArrayList<Integer>();
				tradeEvaluations.add(0);
				// Begin with the evaluation after grabbing the piece already on the square.
				boolean attackerToMove = true;
				int latestEvaluation = pieceCaptureValue;
				tradeEvaluations.add(latestEvaluation);
				while (!(attackerValues.isEmpty() || defenderValues.isEmpty())) {
					// Add the evaluation after capturing the latest attacking piece to occupy the square.
					attackerToMove = !attackerToMove;
					if (attackerToMove)
						latestEvaluation -= defenderValues.remove(0);
					else latestEvaluation -= attackerValues.remove(0);
					tradeEvaluations.add(latestEvaluation);
				}
				
				// The material value is the evaluation wherever optimal trading stops.
				// This is determined by working backwards through the series of trades.
				Collections.reverse(tradeEvaluations);
				int optimalEvaluation = tradeEvaluations.remove(0);
				for (int currentEvaluation : tradeEvaluations) {
					// The current evaluation applies before capturing; here, the optimal evaluation
					// applies after capturing. Optimal players only capture if it improves the evaluation.
					boolean whiteToDecide = (position.whiteToMove == attackerToMove);
					optimalEvaluation = best(optimalEvaluation, currentEvaluation, whiteToDecide);
					attackerToMove = !attackerToMove;
				}
				bestMaterialValue = best(bestMaterialValue, optimalEvaluation, position.whiteToMove);
			}
		return bestMaterialValue;
	}
}
