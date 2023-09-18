package kc;

/**
 * 字句解析を行い，トークンを生成するクラス.
 * @author 19-1-037-0032 竹田有真
 * 問題番号: 問題3.3，問題6.1
 * 提出日: 2021年7月21日
 */
class LexicalAnalyzer {
	// フィールド
	/**
	 * ソースファイルに対するスキャナ
	 */
	private FileScanner sourceFileScanner;

	// コンストラクタ
    /**
     * ファイル名を引数とするコンストラクタ
     */
    LexicalAnalyzer(String sourceFileName) {
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
    	} while(currentChar == ' ' || currentChar == '\n' || currentChar =='\t');	// 空白と改行とタブ文字を読み飛ばす

    	//コメントの読み飛ばし
    	if(currentChar == '/' && sourceFileScanner.lookAhead() == '*') {
    		while(!(currentChar == '*' && sourceFileScanner.lookAhead() == '/'))
    			currentChar = sourceFileScanner.nextChar();
    		currentChar = sourceFileScanner.nextChar();
    		currentChar = sourceFileScanner.nextChar();
    		return nextToken();
    	}

    	if(currentChar == '/' && sourceFileScanner.lookAhead() == '/') {
    		while(!(currentChar == '\n'))
    			currentChar = sourceFileScanner.nextChar();
    		return nextToken();
    	}

    	// 整数解析部分
    	if(currentChar == '0') {			// 先頭が0の場合は別処理
    		if(sourceFileScanner.lookAhead() == 'x') {       // このブロック内は16進数の解析
    			currentChar = sourceFileScanner.nextChar();
    			currentChar = sourceFileScanner.nextChar();
    			if(('0' <= currentChar && currentChar <= '9') ||
    					('A' <= currentChar) && currentChar <= 'F') {
    				int value = Character.digit(currentChar, 16);
    				while(('0' <= sourceFileScanner.lookAhead() && sourceFileScanner.lookAhead() <= '9') ||
        					('A' <= sourceFileScanner.lookAhead() && sourceFileScanner.lookAhead() <= 'F')) {
        				currentChar = sourceFileScanner.nextChar();
        				value *= 16;
        				value += Character.digit(currentChar, 16);
        			}
        			token = new Token(Symbol.INTEGER, value);
    			} else syntaxError();
    		} else token = new Token(Symbol.INTEGER, 0);
    	} else if('1' <= currentChar && currentChar <= '9') {
    		int value = Character.digit(currentChar, 10);
    		while('0' <= sourceFileScanner.lookAhead() && sourceFileScanner.lookAhead() <= '9') {
    			currentChar = sourceFileScanner.nextChar();
    			value *= 10;
    			value += Character.digit(currentChar, 10);
    		}
    		token = new Token(Symbol.INTEGER, value);
    	}

    	// 文字解析部分
    	else if(currentChar == '\'') {
    		int value = 0;		// 文字コード
    		currentChar = sourceFileScanner.nextChar();
    		if(sourceFileScanner.lookAhead() == '\\') {
    			currentChar = sourceFileScanner.nextChar();
    			if(currentChar == 'n')
    				value = (int)'\n';
    			else if(currentChar == 't')
    				value = (int)'\t';
    			else if(currentChar == 'r')
    				value = (int)'\r';
    			else if(currentChar == 'f')
    				value = (int)'\f';
    			else if(currentChar == 'b')
        				value = (int)'\b';
    			else if(currentChar == '\\')
    				value = (int)'\\';
    			else if(currentChar == '\'')
    				value = (int)'\'';
    			else if(currentChar == '"')
    				value = (int)'\"';
    		} else {
    			value = (int)currentChar;
    		}
    		currentChar = sourceFileScanner.nextChar();
    		if(currentChar != '\'')
    			syntaxError();
    		token = new Token(Symbol.CHARACTER, value);
    	}

    	// 変数名・予約語解析部分
    	else if(Character.isAlphabetic(currentChar) || currentChar == '_') {
    		String name = "";
    		name += currentChar;
    		while(Character.isAlphabetic(sourceFileScanner.lookAhead()) ||
    				sourceFileScanner.lookAhead() == '_' ||
    				'0' <= sourceFileScanner.lookAhead() && sourceFileScanner.lookAhead() <= '9') {
    			currentChar = sourceFileScanner.nextChar();
    			name += currentChar;
    		}
    		if(name.equals("main")) 			token = new Token(Symbol.MAIN);
    		else if(name.equals("if")) 			token = new Token(Symbol.IF);
    		else if(name.equals("while")) 		token = new Token(Symbol.WHILE);
    		else if(name.equals("for")) 		token = new Token(Symbol.FOR);
    		else if(name.equals("inputint")) 	token = new Token(Symbol.INPUTINT);
    		else if(name.equals("inputchar")) 	token = new Token(Symbol.INPUTCHAR);
    		else if(name.equals("outputint")) 	token = new Token(Symbol.OUTPUTINT);
    		else if(name.equals("outputchar")) 	token = new Token(Symbol.OUTPUTCHAR);
    		else if(name.equals("break")) 		token = new Token(Symbol.BREAK);
    		else if(name.equals("int")) 		token = new Token(Symbol.INT);
    		else 								token = new Token(Symbol.NAME, name);
    	}

    	// 記号解析部分
    	else if(currentChar == '+') {
    		if(sourceFileScanner.lookAhead() == '+') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.INC);
    		} else if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.ASSIGNADD);
    		} else token = new Token(Symbol.ADD);
    	} else if(currentChar == '-') {
    		if(sourceFileScanner.lookAhead() == '-') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.DEC);
    		} else if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.ASSIGNSUB);
    		} else token = new Token(Symbol.SUB);
    	} else if(currentChar == '*') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.ASSIGNMUL);
    		} else token = new Token(Symbol.MUL);
    	} else if(currentChar == '/') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.ASSIGNDIV);
    		} else token = new Token(Symbol.DIV);
    	} else if(currentChar == '%') token = new Token(Symbol.MOD);
    	else if(currentChar == '=') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.EQUAL);
    		} else token = new Token(Symbol.ASSIGN);
    	} else if(currentChar == '!') {
    		if(sourceFileScanner.lookAhead() == '=') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.NOTEQ);
    		} else token = new Token(Symbol.NOT);
    	} else if(currentChar == '<') 	token = new Token(Symbol.LESS);
    	else if(currentChar == '>') 	token = new Token(Symbol.GREAT);
    	else if(currentChar == '&') {
    		if(sourceFileScanner.lookAhead() == '&') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.AND);
    		} else syntaxError();
    	} else if(currentChar == '|') {
    		if(sourceFileScanner.lookAhead() == '|') {
    			sourceFileScanner.nextChar();
    			token = new Token(Symbol.OR);
    		} else syntaxError();
    	} else if(currentChar == ';') 	token = new Token(Symbol.SEMICOLON);
    	else if(currentChar == '(') 	token = new Token(Symbol.LPAREN);
    	else if(currentChar == ')') 	token = new Token(Symbol.RPAREN);
    	else if(currentChar == '{') 	token = new Token(Symbol.LBRACE);
    	else if(currentChar == '}') 	token = new Token(Symbol.RBRACE);
    	else if(currentChar == '[') 	token = new Token(Symbol.LBRACKET);
    	else if(currentChar == ']') 	token = new Token(Symbol.RBRACKET);
    	else if(currentChar == ',') 	token = new Token(Symbol.COMMA);
    	else if(currentChar == '\0') 	token = new Token(Symbol.EOF);
    	else syntaxError();  // どのトークンとも一致しなかった場合
    	return token;
    }

    /**
     * 読んでいるファイルを閉じる
     */
    void closeFile() {
    	sourceFileScanner.closeFile();
    }

    /**
     * 現在，入力ファイルのどの部分を解析中であるのかを表現する文字列を返す.
     * sourceFileScannerのscanAtメソッドを呼び出せばよい.
     */
    String analyzeAt() {
    	return sourceFileScanner.scanAt();
    }

    /**
     * 字句解析時に構文エラーを検出したときに呼ばれるメソッド.
     * プログラム例3のとおりに作成すること.
     */
    private void syntaxError() {
        System.out.print (sourceFileScanner.scanAt());
        //下記の文言は自動採点で使用するので変更しないでください。
        System.out.println ("で字句解析プログラムが構文エラーを検出");
        closeFile();
        System.exit(1);
    }
}