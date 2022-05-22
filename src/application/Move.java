package application;

// A single move of a chess piece.
// Nate Hunter - 04/03/2022
public class Move {
	// Move types to account for particular special cases.
	public static enum Type {
		NORMAL,
		CASTLE,
		PROMOTION,
		EN_PASSANT,
		TWO_SQUARE_PAWN
	}
	
	// Move directions specify the change in rank number and file number, respectively,
	// for a particular move. For example, F3 -> D4 has a move direction of [1, -2].
	public static final int[][] ROYALTY_MOVE_DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0},
															{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
	public static final int[][] ROOK_MOVE_DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
	public static final int[][] BISHOP_MOVE_DIRECTIONS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
	public static final int[][] KNIGHT_MOVE_DIRECTIONS = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
															{1, -2}, {1, 2}, {2, -1}, {2, 1}};
	
	// The coordinates from which and to which the piece is moving (from 0 to 7).
	public int sourceRank, sourceFile, targetRank, targetFile;
	// The move type (for special cases).
	public Type type;
	
	// Creates a move.
	public Move(int sourceRank, int sourceFile, int targetRank, int targetFile) {
		this(sourceRank, sourceFile, targetRank, targetFile, Type.NORMAL);
	}
	public Move(int sourceRank, int sourceFile, int targetRank, int targetFile, Type type) {
		this.sourceRank = sourceRank;
		this.sourceFile = sourceFile;
		this.targetRank = targetRank;
		this.targetFile = targetFile;
		this.type = type;
	}
	
	// Determines whether a move matches the coordinates supplied.
	public boolean matches(int sourceRank, int sourceFile, int targetRank, int targetFile) {
		return matchesSource(sourceRank, sourceFile) && matchesTarget(targetRank, targetFile);
	}
	// Determines whether a move matches the source square supplied.
	public boolean matchesSource(int sourceRank, int sourceFile) {
		return (this.sourceRank == sourceRank && this.sourceFile == sourceFile);
	}
	// Determines whether a move matches the target square supplied.
	public boolean matchesTarget(int targetRank, int targetFile) {
		return (this.targetRank == targetRank && this.targetFile == targetFile);
	}
	
	// Determines whether a coordinate is in the bounds of the chess board.
	public static boolean inbounds(int rank, int file) {
		return (0 <= rank && rank < 8) && (0 <= file && file < 8);
	}
	
	// Gets the rank delta of a move direction. (e.g. a knight moving [1, -2] has a rank delta of 1)
	public static int rankDelta(int[] moveDirection) {
		return moveDirection[0];
	}
	// Gets the file delta of a move direction. (e.g. a knight moving [1, -2] has a file delta of -2)
	public static int fileDelta(int[] moveDirection) {
		return moveDirection[1];
	}
}
