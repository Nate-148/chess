package application;

// A coordinate vector corresponding to a location on the chess board.
// The coordinate vector can represent an absolute location (e.g. f7)
// or a relative move direction (e.g. two squares forward).
// Nate Hunter - 06/08/2022
public class Coordinate {
	// The rank or rank delta of the coordinate vector.
	// Absolute ranks range from 0 to 7 (chess-notation rank minus one).
	public int rank;
	// The file or file delta of the coordinate vector.
	// Absolute files range from 0 (A) to 7 (H).
	public int file;
	
	// An invalid absolute rank or file (for disabling moves, etc.).
	public static final int INVALID = -1;
	
	// Move directions represent moves as relative vectors. For example, F3 -> D4 has a move direction of (1, -2).
	// Compass direction names are used to model the chess board from white's perspective as a geographic map.
	private static final Coordinate N = new Coordinate(1, 0);
	private static final Coordinate NNE = new Coordinate(2, 1);
	private static final Coordinate NE = new Coordinate(1, 1);
	private static final Coordinate ENE = new Coordinate(1, 2);
	private static final Coordinate E = new Coordinate(0, 1);
	private static final Coordinate ESE = new Coordinate(-1, 2);
	private static final Coordinate SE = new Coordinate(-1, 1);
	private static final Coordinate SSE = new Coordinate(-2, 1);
	private static final Coordinate S = new Coordinate(-1, 0);
	private static final Coordinate SSW = new Coordinate(-2, -1);
	private static final Coordinate SW = new Coordinate(-1, -1);
	private static final Coordinate WSW = new Coordinate(-1, -2);
	private static final Coordinate W = new Coordinate(0, -1);
	private static final Coordinate WNW = new Coordinate(1, -2);
	private static final Coordinate NW = new Coordinate(1, -1);
	private static final Coordinate NNW = new Coordinate(2, -1);
	// Move directions are aggregated for different piece types to facilitate move calculations.
	public static final Coordinate[] ROYALTY_MOVE_DIRECTIONS = {N, NE, E, SE, S, SW, W, NW};
	public static final Coordinate[] ROOK_MOVE_DIRECTIONS = {N, E, S, W};
	public static final Coordinate[] BISHOP_MOVE_DIRECTIONS = {NE, SE, SW, NW};
	public static final Coordinate[] KNIGHT_MOVE_DIRECTIONS = {NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW};
	public static final Coordinate WHITE_PAWN_NON_CAPTURE_DIRECTION = N;
	public static final Coordinate[] WHITE_PAWN_CAPTURE_DIRECTIONS = {NE, NW};
	public static final Coordinate BLACK_PAWN_NON_CAPTURE_DIRECTION = S;
	public static final Coordinate[] BLACK_PAWN_CAPTURE_DIRECTIONS = {SE, SW};
	
	// Creates a coordinate.
	public Coordinate(int rank, int file) {
		this.rank = rank;
		this.file = file;
	}
	
	// Copies the coordinate.
	public Coordinate copy() {
		return new Coordinate(rank, file);
	}
	
	// Determines whether coordinates are equal.
	@Override
	public boolean equals(Object coordinate) {
		return (rank == ((Coordinate)coordinate).rank) && (file == ((Coordinate)coordinate).file);
	}
	
	// Determines whether an absolute coordinate is in the bounds of the chess board.
	public boolean inbounds() {
		return (0 <= rank && rank < 8) && (0 <= file && file < 8);
	}
	
	// Determines whether an absolute coordinate corresponds to a dark square (or a light square).
	public boolean darkSquare() {
		return rank % 2 == file % 2;
	}
	
	// Adds a move direction to the coordinate.
	// This can be used to compute a target square from a source square.
	public Coordinate add(Coordinate moveDirection) {
		return new Coordinate(rank + moveDirection.rank, file + moveDirection.file);
	}
	
	// Scales a relative move direction.
	public Coordinate scale(int multiplier) {
		return new Coordinate(rank * multiplier, file * multiplier);
	}
	
	// Gets an invalid coordinate.
	public static Coordinate getInvalid() {
		return new Coordinate(INVALID, INVALID);
	}
}
