package application;

// Facilitates processing piece characters.
// Pieces are stored as characters instead of full objects for efficient processing and memory use.
// Nate Hunter - 04/02/2022
public class Piece {
	// An empty piece, representing an unoccupied square.
	public static final char EMPTY = ' ';
	
	// Colors of pieces.
	public static enum Color {
		EMPTY,
		WHITE,
		BLACK,
	}
	
	// Types of pieces.
	public static enum Type {
		EMPTY,
		KING,
		QUEEN,
		ROOK,
		BISHOP,
		KNIGHT,
		PAWN
	}
	
	// Gets the color of the piece.
	public static Color color(char piece) {
		// Capital letters represent white pieces, consistent with Lichess URLs.
		if (Character.isUpperCase(piece))
			return Color.WHITE;
		else if (Character.isLowerCase(piece))
			return Color.BLACK;
		return Color.EMPTY;
	}
	
	// Determines whether the piece is white.
	public static boolean isWhite(char piece) {
		return color(piece) == Color.WHITE;
	}
	// Determines whether the piece is black.
	public static boolean isBlack(char piece) {
		return color(piece) == Color.BLACK;
	}
	// Determines whether the piece is empty (i.e. the square is unoccupied).
	public static boolean isEmpty(char piece) {
		return color(piece) == Color.EMPTY;
	}
	// Determines whether the piece belongs to the active player.
	public static boolean isActive(char piece, boolean whiteToMove) {
		if (whiteToMove)
			return isWhite(piece);
		return isBlack(piece);
	}
	// Determines whether the piece belongs to the opponent of the active player.
	public static boolean isOpposing(char piece, boolean whiteToMove) {
		if (whiteToMove)
			return isBlack(piece);
		return isWhite(piece);
	}
	
	// Gets the type of the piece.
	public static Type type(char piece) {
		char lowercasePiece = Character.toLowerCase(piece);
		switch (lowercasePiece) {
		case 'k': return Type.KING;
		case 'q': return Type.QUEEN;
		case 'r': return Type.ROOK;
		case 'b': return Type.BISHOP;
		case 'n': return Type.KNIGHT;
		case 'p': return Type.PAWN;
		default: return Type.EMPTY;
		}
	}
	
	// Gets the Unicode text representing the piece type (used in the GUI display).
	public static String unicodeType(char piece) {
		Type pieceType = type(piece);
		switch (pieceType) {
		case KING: return "\u265A";
		case QUEEN: return "\u265B";
		case ROOK: return "\u265C";
		case BISHOP: return "\u265D";
		case KNIGHT: return "\u265E";
		case PAWN: return "\u265F";
		default: return "";
		}
	}
	
	// Gets the material value of the piece. Black pieces are weighted negatively.
	public static int materialValue(char piece) {
		int magnitude;
		Type pieceType = type(piece);
		switch (pieceType) {
		case KING:
			// The king is given an arbitrarily large value to make errors obvious.
			magnitude = 1000;
			break;
		case QUEEN:
			magnitude = 9;
			break;
		case ROOK:
			magnitude = 5;
			break;
		case BISHOP:
			magnitude = 3;
			break;
		case KNIGHT:
			magnitude = 3;
			break;
		case PAWN:
			magnitude = 1;
			break;
		default:
			magnitude = 0;
			break;
		}

		int direction;
		Color pieceColor = color(piece);
		switch (pieceColor) {
		case WHITE:
			direction = 1;
			break;
		case BLACK:
			direction = -1;
			break;
		default:
			direction = 0;
			break;
		}
		
		int materialValue = magnitude * direction;
		return materialValue;
	}
	
	// Returns a queen of the same color as the pawn to be promoted.
	public static char promoteToQueen(char pawn) {
		Color pawnColor = color(pawn);
		switch (pawnColor) {
		case WHITE: return 'Q';
		case BLACK: return 'q';
		default: return ' ';
		}
	}
}
