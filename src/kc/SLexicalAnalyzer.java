package kc;

/**
 * LexicalAnalyzerの簡易版クラス
 * 整数と一部の記号のみ解析する.
 * @author 19-1-037-0032 竹田有真
 * 問題番号: 問題3.2
 * 提出日: 2021年5月19日
 */
class SLexicalAnalyzer {
	// フィールド
	/**
	 * ソースファイルに対するスキャナ
	 */
    private FileScanner sourceFileScanner; // 入力ファイルのFileScannerへの参照

    /**
     * コンストラクタ
     */
    SLexicalAnalyzer(String sourceFileName) {
    	sourceFileScanner = new FileScanner(sourceFileName);
    }

    /**
     * 次のトークンを切り出す.
     * ファイル末に達している場合(’\0’を読んだ場合)はEOFを返す.
     * 入力がマイクロ構文に違反したためトークンの切り出しに失敗した場合，
     * syntaxError()メソッドを呼び出す.
     */
    Token nextToken() {
    	Token token = null;     // ダミーの初期値
    	char currentChar;   	// 現在位置の文字
    	do {
    		currentChar = sourceFileScanner.nextChar();
    	} while(currentChar == ' ' || currentChar == '\n' || currentChar == '\t');		// 空白と改行とタブを読み飛ばす

    	// 整数解析部分
    	if(currentChar == '0') {			// 先頭が0の場合は別処理
    		token = new Token(Symbol.INTEGER, 0);
    	} else if('1' <= currentChar && currentChar <= '9') {
    		int value = Character.digit(currentChar, 10);
    		while('0' <= sourceFileScanner.lookAhead() && sourceFileScanner.lookAhead() <= '9') {
    			currentChar = sourceFileScanner.nextChar();
    			value *= 10;
    			value += Character.digit(currentChar, 10);
    		}
    		token = new Token(Symbol.INTEGER, value);
    	}

    	// 記号解析部分
    	else if(currentChar == '+') {
    		token = new Token(Symbol.ADD);
    	} else if(currentChar == '=') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.EQUAL);
    		} else {
        		token = new Token(Symbol.ASSIGN);
        	}
    	} else if(currentChar == '!') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.NOTEQ);
    		} else {
        		token = new Token(Symbol.NOT);
        	}
    	} else if(currentChar == '\0') {	// ファイル末の時
    		token = new Token(Symbol.EOF);
    	} else				// どのトークンとも一致しなかった場合
    		syntaxError();

    	return token;
    }

    /**
     * 読んでいるファイルを閉じる
     */
    void closeFile() {
    	sourceFileScanner.closeFile();
    }

    /**
     * 字句解析時に構文エラーを検出したときに呼ばれるメソッド
     */
    void syntaxError() {
        System.out.print (sourceFileScanner.scanAt());
        System.out.println ("で字句解析プログラムが構文エラーを検出");
        closeFile();
        System.exit(0);
    }
}