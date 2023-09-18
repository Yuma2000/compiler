package kc;

/**
 * 切り出したトークンを格納するクラス
 * 大掛かりなメソッドを持つ類のクラスではない
 */
class Token {
	/**
	 * そのトークンの種別を表す
	 * Symbolはenum型のクラス
	 */
	private Symbol symbol;

	/**
	 * トークンの種別が整数 (INTEGER) または文字 (CHARACTER) であるとき，
	 * その整数値あるいは文字コードを保持する.
	 */
	private int intValue;

	/**
	 * トークンの種別が名前 (NAME) または文字列 (STRING) であるとき，
	 * それを表す文字列を保持する.
	 */
	private String strValue;

    /**
     * 整数，文字，名前以外のトークンを生成するための，
     * トークンの種別のみを引数とするコンストラクタ
     */
    Token(Symbol symbol) {
    	this.symbol = symbol;
    	this.intValue = -1; // 便宜上 -1 を代入しているが，どのような値でもかまわない
        this.strValue = "";
    }

    /**
     * 整数，文字のトークンを生成するための，
     * トークンの種別と値(整数値もしくは文字コード)を引数とするコンストラクタ
     */
    Token(Symbol symbol, int intValue) {
    	this.symbol = symbol;
    	this.intValue = intValue;
    	this.strValue = "";
    }


    /**
     * 名前，文字列のトークンを生成するための，
     * トークンの種別と文字列を引数とするコンストラクタ
     */
    Token(Symbol symbol, String strValue) {
    	this.symbol = symbol;
    	this.intValue = -1;
    	this.strValue = strValue;
    }

    /**
     * symbolフィールドが，引数symbolTypeとトークン種別と一致するかどうかを調べる
     */
    boolean checkSymbol(Symbol symbolType) {
    	return symbol.equals(symbolType);
    }

    // 以下フィールドのゲッター
    /**
     * symbolフィールドのゲッター
     */
    Symbol getSymbol() {
    	return symbol;
    }

    /**
     * intValueフィールドのゲッター
     */
    int getIntValue() {
    	return intValue;
    }

    /**
     * strValueフィールドのゲッター
     */
    String getStrValue() {
    	return strValue;
    }

    public String toString() {
    	return symbol.toString();
    }
}
