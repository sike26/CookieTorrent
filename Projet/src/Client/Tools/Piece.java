package Cookie;

/**
 * <b>This class represents a piece in the application.</b>
 * <p>A piece is a couple of an index and a data.</p>
 */
class Piece {
	
    /**
     * the index of the piece
     */
    private int index;

    /**
     * The data of the piece encode in a string (Base64 encoder).
     */
    private String data;

    /**
     * Constructor.
     * @param index
     *        the index of the piece.
     * @param data
     *        the data of the piece encode in a string.
     */
    public Piece (int index, String data) {
	this.index = index;
	this.data = data;
    }

    /**
     * Getter to the index
     * @return the index of the piece
     */
    public int getIndex() {
	return index;
    }

    /**
     * Getter to the data
     * @return the data of the piece
     */
    public String getData() {
	return data;
    }
}
