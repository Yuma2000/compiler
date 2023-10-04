package kc;

import java.util.ArrayList;

/**
 * 字句解析器から受け取ったトークンを用いて構文解析，コード生成，制約検査を行うクラス
 */
public class Kc {
	/**
	 * 使用する字句解析器
	 */
	private LexicalAnalyzer lexer;

	/**
	 * 字句解析器から受け取ったトークン
	 */
	private Token token;

	/**
	 * 変数表
	 */
	private VarTable variableTable;

	/**
	 * アセンブラコード表
	 */
	private PseudoIseg iseg;

	/**
	 * ループ内であるかを示す
	 */
	private boolean inLoop = false;

	/**
	 * break文のJUMP命令の番地を記憶する
	 */
	private ArrayList<Integer>breakAddrList;

    /**
     * ソースファイル名を引数とするコンストラクタ.
     */
    Kc (String sourceFileName) {
    	lexer = new LexicalAnalyzer(sourceFileName);
    	variableTable = new VarTable();
    	iseg = new PseudoIseg();
    	breakAddrList = new ArrayList<Integer>();
    }

    /**
     * K21言語プログラム部解析
     * 再帰降下型構文解析を行うメソッド.
     * 入力が構文規則に違反していることを検出した場合，エラーの内容を表す文字列を引数として
     * syntaxError メソッドを呼び出す.
     */
    void parseProgram() {
    	token = lexer.nextToken();
    	if(token.checkSymbol(Symbol.MAIN)) {
    		token = lexer.nextToken();
    		parseMain_function();
    		if(token.checkSymbol(Symbol.EOF));		//EOFなら何もしない
    		else syntaxError("EOFが期待されます");
    	} else syntaxError("'main'が期待されます");
    	iseg.appendCode(Operator.HALT);
//    	iseg.dump();	//実行結果確認用に追加
    }

    /**
     * <Main_function>の構文解析をする．
     */
    private void parseMain_function() {

    	if(token.checkSymbol(Symbol.LPAREN))
    		token = lexer.nextToken();
    	else syntaxError("'('が期待されます");

    	if(token.checkSymbol(Symbol.RPAREN))
    		token = lexer.nextToken();
    	else syntaxError("')'が期待されます");

    	parseBlock();
    }

    /**
     * <Block>の構文解析をする．
     */
    private void parseBlock() {
    	if(token.checkSymbol(Symbol.LBRACE))
    		token = lexer.nextToken();
    	else syntaxError("'{'が期待されます");

    	int tableSize = variableTable.size();  //変数表のサイズを記憶

    	while(firstStatement()) {	//ブロック内で文が続く限り，文の構文解析をする．
    		parseStatement();
    	}

    	if(token.checkSymbol(Symbol.RBRACE))
    		token = lexer.nextToken();
    	else syntaxError("'}'が期待されます");

    	variableTable.removeTail(tableSize);  //変数表の末尾を削除
    }

    /**
     * <Statement>の構文解析をする．
     */
    private void parseStatement() {
    	switch(token.getSymbol()){
    	case INT:
    		parseVar_decl_statement();
    		break;
    	case IF:
    		parseIf_statement();
    		break;
    	case WHILE:
    		parseWhile_statement();
    		break;
    	case FOR:
    		parseFor_statement();
    		break;
    	case OUTPUTCHAR:
    		parseOutputchar_statement();
    		break;
    	case OUTPUTINT:
    		parseOutputint_statement();
    		break;
    	case BREAK:
    		parseBreak_statement();
    		break;
    	case LBRACE:
    		parseBlock();
    		break;
    	case SEMICOLON:
    		token = lexer.nextToken();
    		break;
    	default:
    		parseExp_statement();
    		break;
    	}
    }

    /**
     * <Var_decl_statement>の構文解析をする．
     * 変数宣言文
     */
    private void parseVar_decl_statement() {
    	parseVar_decl();
    	if(token.checkSymbol(Symbol.SEMICOLON))
    		token = lexer.nextToken();
    	else syntaxError("';'が期待されます");
    }

    /**
     * <Var_decl>の構文解析をする．
     * 変数宣言
     */
    private int parseVar_decl() {
    	int popAddr = -1;  //返り値用
    	token = lexer.nextToken();
    	if(token.checkSymbol(Symbol.NAME))
    		popAddr = parseName_list();
    	else syntaxError("NAMEが期待されます");
    	return popAddr;   //for文のために返す
    }

    /**
     * <Name_list>の構文解析をする．
     * 変数を複数宣言する
     */
    private int parseName_list() {
    	int popAddr = parseName();

    	while(token.checkSymbol(Symbol.COMMA)) {	//トークンがコンマである限り繰り返す．
    		token = lexer.nextToken();
    		if(token.checkSymbol(Symbol.NAME))
    			popAddr = parseName();
    		else syntaxError("NAMEが期待されます");
    	}
    	return popAddr;
    }

    /**
     * <Name>の構文解析をする
     */
    private int parseName() {
    	String name = token.getStrValue();		//変数名を保管しておく
    	int popAddr = -1;  //for文のために返す
    	token = lexer.nextToken();
    	if(variableTable.exist(name)) syntaxError("変数はすでに存在します");

    	if(token.checkSymbol(Symbol.ASSIGN)) {		//変数に代入する形が続く．
    		token = lexer.nextToken();

    		if(token.checkSymbol(Symbol.SUB) ||
    			token.checkSymbol(Symbol.INTEGER) ||
    			token.checkSymbol(Symbol.CHAR)) {
    			parseConstant();
    		} else syntaxError("'-'またはINTまたはCHARが期待されます");

    		variableTable.registerNewVariable(Type.INT, name, 1);	//変数表に登録
    		popAddr = iseg.appendCode(Operator.POP, variableTable.getAddress(name));	//Dsegに代入

    	} else if(token.checkSymbol(Symbol.LBRACKET)) {  //配列の形になる．
    		int size = 1;  //配列サイズ記憶用
    		token = lexer.nextToken();

    		if(token.checkSymbol(Symbol.INTEGER)) {		//配列のサイズを指定する場合．
    			size = token.getIntValue();  //配列サイズを記憶
    			token = lexer.nextToken();

    			if(token.checkSymbol(Symbol.RBRACKET))
    				token = lexer.nextToken();
    			else syntaxError("']'が期待されます");

    			variableTable.registerNewVariable(Type.ARRAYOFINT, name, size);	//変数表に配列を登録

    		} else if(token.checkSymbol(Symbol.RBRACKET)) {	//配列のサイズを指定しない場合．
    			token = lexer.nextToken();

    			if(token.checkSymbol(Symbol.ASSIGN))
    				token = lexer.nextToken();
    			else syntaxError("'='が期待されます");

				if(token.checkSymbol(Symbol.LBRACE))
					token = lexer.nextToken();
				else syntaxError("'{'が期待されます");

				if(token.checkSymbol(Symbol.SUB) ||
		    			token.checkSymbol(Symbol.INTEGER) ||
		    			token.checkSymbol(Symbol.CHARACTER)) {
		    			popAddr = parseConstant_list(name);  //引数追加
				} else syntaxError("'-'またはINTまたはCHARが期待されます");

    			if(token.checkSymbol(Symbol.RBRACE))
    				token = lexer.nextToken();
    			else syntaxError("'}'が期待されます");

    		} else syntaxError("INTEGERまたは']'が期待されます");
    	} else variableTable.registerNewVariable(Type.INT, name, 1);	//変数表に登録
    	return popAddr;
    }

    /**
     * <Constant_list>の構文解析をする
     * 配列に代入するものを扱う
     */
    private int parseConstant_list(String name) {
    	parseConstant();
    	int size = 1;
    	int popAddr = -1; //返り値用
    	int popAddress = iseg.appendCode(Operator.POP, -1); //POP先未定
    	while(token.checkSymbol(Symbol.COMMA)) {
    		size++;
    		token = lexer.nextToken();
    		parseConstant();
    		iseg.appendCode(Operator.POP, -1);  //POP先未定
    	}
    	variableTable.registerNewVariable(Type.ARRAYOFINT, name, size);	//変数表に登録
    	for(int i = 0; i < size; i++) {
    		iseg.replaceCode(popAddress + i*2, variableTable.getAddress(name) + i);
    		popAddr = popAddress + i*2;
    	}

    	return popAddr;
    }

    /**
     * <Constant>の構文解析をする
     * 変数に代入するもの
     */
    private int parseConstant() {
    	int value = -1;
    	if(token.checkSymbol(Symbol.SUB)){
    		token = lexer.nextToken();

    		if(token.checkSymbol(Symbol.INTEGER)) {
    			value = -token.getIntValue();	//整数値を保管
    			token = lexer.nextToken();
    		} else syntaxError("INTEGERが期待されます");

    	} else {
    		value = token.getIntValue();	//整数値を保管
			token = lexer.nextToken();
    	}
    	iseg.appendCode(Operator.PUSHI, value);	//代入する値を積む
    	return value;
    }

    /**
     * <If_statement>の構文解析をする．
     */
    private void parseIf_statement() {
    	token = lexer.nextToken();

    	if(token.checkSymbol(Symbol.LPAREN))
    		token = lexer.nextToken();
    	else syntaxError("'('が期待されます");

		if(firstExpression())
			parseExpression();
		else syntaxError("<Expression>が期待されます");

		if(token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else syntaxError("')'が期待されます");

		int beqAddr = iseg.appendCode(Operator.BEQ, -1);  //飛び先が未定なので変数に格納する

		if(firstStatement())
		    parseStatement();
		else syntaxError("<Statement>が期待されます");

		iseg.replaceCode(beqAddr, iseg.getLastCodeAddress()+1); //飛び先が決定したので変更する

    }

    /**
     * <While_statement>の構文解析をする．
     */
    private void parseWhile_statement() {
    	token = lexer.nextToken();

    	if(token.checkSymbol(Symbol.LPAREN))
    		token = lexer.nextToken();
    	else syntaxError("'('が期待されます");

    	int lastAddr = iseg.getLastCodeAddress();  //条件式直前の番地を記憶

		if(firstExpression())
			parseExpression();
		else syntaxError("<Expression>が期待されます");

		if(token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else syntaxError("')'が期待されます");

		int beqAddr = iseg.appendCode(Operator.BEQ, -1);  //飛び先未定

		/* ここからbreak文のための処理 */
		boolean outerLoop = inLoop;  //while文外部の情報を記憶
		ArrayList<Integer> outerList = breakAddrList;  //ループに入る前にリストを記憶しておく
		inLoop = true;
		breakAddrList = new ArrayList<Integer>();  //リストを初期化
		/* ここまでbreak文のための処理 */

		if(firstStatement())
			parseStatement();
		else syntaxError("<Statement>が期待されます");

		int jumpAddr = iseg.appendCode(Operator.JUMP, lastAddr+1);  //条件式へJUMP

		/* ここからbreak文のための処理 */
		for(int i=0; i<breakAddrList.size(); ++i) {
			int breakAddr = breakAddrList.get(i);  //break文の番地
			iseg.replaceCode(breakAddr, jumpAddr+1);  //ループ外へ
		}
		inLoop = outerLoop;
		breakAddrList = outerList;
		/* ここまでbreak文のための処理 */

		iseg.replaceCode(beqAddr, jumpAddr+1); //while文の終わりがわかったので書き換える
    }

    /**
     * <For_statement>の構文解析をする．
     */
    private void parseFor_statement() {
    	token = lexer.nextToken();
    	int tableSize = variableTable.size();  //変数表のサイズを記憶
    	int removeAddr = -1;

    	if(token.checkSymbol(Symbol.LPAREN)) {
    		token = lexer.nextToken();
    	} else syntaxError("'('が期待されます");

    	if(firstExpression()) {
			parseExpression();
			removeAddr = iseg.appendCode(Operator.REMOVE);  //条件式直前の番地を記憶
		} else if(token.checkSymbol(Symbol.INT)) {
			removeAddr = parseVar_decl();
		} else syntaxError("<Expression>または<Var_decl>が期待されます");

		if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
		} else syntaxError("';'が期待されます");

		if(firstExpression()) {
			parseExpression();
		} else syntaxError("<Expression>が期待されます");

		if(token.checkSymbol(Symbol.SEMICOLON))
			token = lexer.nextToken();
		else syntaxError("';'が期待されます");

		int beqAddr = iseg.appendCode(Operator.BEQ, -1);  //飛び先未定
		int jumpAddr = iseg.appendCode(Operator.JUMP, -1);  //飛び先未定

		if(firstExpression())
			parseExpression();
		else syntaxError("<Expression>が期待されます");

		if(token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else syntaxError("')'が期待されます");

		iseg.appendCode(Operator.REMOVE);
		int jumpAddr2 = iseg.appendCode(Operator.JUMP, removeAddr+1);

		/* ここからbreak文のための処理 */
		boolean outerLoop = inLoop;  //for文外部の情報を記憶
		ArrayList<Integer> outerList = breakAddrList;  //ループに入る前にリストを記憶しておく
		inLoop = true;
		breakAddrList = new ArrayList<Integer>();  //リストを初期化
		/* ここまでbreak文のための処理 */


		if(firstStatement())
			parseStatement();
		else syntaxError("<Statement>が期待されます");

		int jumpAddr3 = iseg.appendCode(Operator.JUMP, jumpAddr+1);


		/* ここからbreak文のための処理 */
		for(int i = 0; i<breakAddrList.size(); ++i) {
			int breakAddr = breakAddrList.get(i);  //break文の番地
			iseg.replaceCode(breakAddr, jumpAddr+1); //ループ外へ
		}
		inLoop = outerLoop;    //外部のループ情報を復帰
		breakAddrList = outerList;
		/* ここまでbreak文のための処理 */


		iseg.replaceCode(beqAddr, jumpAddr3+1); //飛び先修正
		iseg.replaceCode(jumpAddr, jumpAddr2+1); //飛び先修正

		variableTable.removeTail(tableSize);  //変数表の末尾を削除
    }

    /**
     * <Exp_statement>の構文解析をする．
     */
    private void parseExp_statement() {
    	parseExpression();
    	if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
			iseg.appendCode(Operator.REMOVE);
    	}
		else syntaxError("';'が期待されます");
    }

    /**
     * <Outputchar_statement>の構文解析をする．
     * 文字の出力文を扱う
     */
    private void parseOutputchar_statement() {
    	token = lexer.nextToken();

    	if(token.checkSymbol(Symbol.LPAREN))
			token = lexer.nextToken();
		else syntaxError("'('が期待されます");

    	if(firstExpression())
			parseExpression();
		else syntaxError("<Expression>が期待されます");

    	if(token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else syntaxError("')'が期待されます");

    	if(token.checkSymbol(Symbol.SEMICOLON))
			token = lexer.nextToken();
		else syntaxError("';'が期待されます");

    	//コード生成
    	iseg.appendCode(Operator.OUTPUTC);
    	iseg.appendCode(Operator.OUTPUTLN);
    }

    /**
     * <Outputint_statement>の構文解析をする．
     * 数字の出力文を扱う
     */
    private void parseOutputint_statement() {
    	token = lexer.nextToken();

    	if(token.checkSymbol(Symbol.LPAREN))
			token = lexer.nextToken();
		else syntaxError("'('が期待されます");

    	if(firstExpression())
			parseExpression();
		else syntaxError("<Expression>が期待されます");

    	if(token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else syntaxError("')'が期待されます");

    	if(token.checkSymbol(Symbol.SEMICOLON))
			token = lexer.nextToken();
		else syntaxError("';'が期待されます");

    	//コード生成
    	iseg.appendCode(Operator.OUTPUT);
    	iseg.appendCode(Operator.OUTPUTLN);
    }

    /**
     * <Break_statement>の構文解析をする．
     * break文を扱う
     */
    private void parseBreak_statement() {
    	token = lexer.nextToken();

    	if(inLoop == false) syntaxError("ループ内ではありません");

    	int addr = iseg.appendCode(Operator.JUMP, -1);  //飛び先未定
    	breakAddrList.add(addr);  //JUMP命令の番地を記憶

    	if(token.checkSymbol(Symbol.SEMICOLON))
			token = lexer.nextToken();
		else syntaxError("';'が期待されます");
    }

    /**
     * <Expression>の構文解析をする．
     * 主に代入演算を扱う．
     */
    private void parseExpression() {
        boolean hasLeftValue = parseExp();	//左辺値の有無を記憶
        if (token.checkSymbol(Symbol.ASSIGN)
            || token.checkSymbol(Symbol.ASSIGNADD)
            || token.checkSymbol(Symbol.ASSIGNSUB)
            || token.checkSymbol(Symbol.ASSIGNMUL)
            || token.checkSymbol(Symbol.ASSIGNDIV)) {

        	if(!hasLeftValue) syntaxError("左辺値がありません");
        	Symbol op = token.getSymbol(); 	//演算子を記憶
            token = lexer.nextToken();

            if(op != Symbol.ASSIGN) {
            	iseg.appendCode(Operator.COPY);
            	iseg.appendCode(Operator.LOAD);
            }

            if(firstExpression())
            	parseExpression();
            else syntaxError("<Expression>が期待されます");
            if(op == Symbol.ASSIGNADD) iseg.appendCode(Operator.ADD);
            else if(op == Symbol.ASSIGNSUB) iseg.appendCode(Operator.SUB);
            else if(op == Symbol.ASSIGNMUL) iseg.appendCode(Operator.MUL);
            else if(op == Symbol.ASSIGNDIV) iseg.appendCode(Operator.DIV);
            iseg.appendCode(Operator.ASSGN);
        }
    }

    /**
     * <Exp>の構文解析をする．
     * OR演算を扱う
     * 左辺値があるならtrueを返す
     */
    private boolean parseExp() {
    	boolean hasLeftValue = parseLogical_term();

    	while(token.checkSymbol(Symbol.OR)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる
    		token = lexer.nextToken();
    		if(firstExpression())
    			parseLogical_term();
    		else syntaxError("<Logical_term>が期待されます");

    		iseg.appendCode(Operator.OR);
    	}
    	return hasLeftValue;
    }

    /**
     * <Logical_term>の構文解析をする．
     * AND演算を扱う
     * 左辺値があるならtrueを返す
     */
    private boolean parseLogical_term() {
    	boolean hasLeftValue = parseLogical_factor();

    	while(token.checkSymbol(Symbol.AND)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる
    		token = lexer.nextToken();
    		if(firstExpression())
    			parseLogical_factor();
    		else syntaxError("<Logical_factor>が期待されます");

    		iseg.appendCode(Operator.AND);
    	}
    	return hasLeftValue;
    }

    /**
     * <Logical_factor>の構文解析をする．
     * 比較演算を扱う
     * 左辺値があるならtrueを返す
     */
    private boolean parseLogical_factor() {
    	boolean hasLeftValue = parseArithmetic_expression();

    	if(token.checkSymbol(Symbol.EQUAL) ||
    			token.checkSymbol(Symbol.NOTEQ) ||
    			token.checkSymbol(Symbol.LESS) ||
    			token.checkSymbol(Symbol.GREAT)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる
    		Symbol op = token.getSymbol();  //演算子を記憶
    		token = lexer.nextToken();
    		if(firstExpression())
    			parseArithmetic_expression();
    		else syntaxError("<Arithmetic_expression>が期待されます");

    		int compAddr = iseg.appendCode(Operator.COMP);
    		switch(op) {
    			case EQUAL:
    				iseg.appendCode(Operator.BEQ, compAddr+4);
    				break;
    			case NOTEQ:
    				iseg.appendCode(Operator.BNE, compAddr+4);
    				break;
    			case LESS:
    				iseg.appendCode(Operator.BLT, compAddr+4);
    				break;
    			case GREAT:
    				iseg.appendCode(Operator.BGT, compAddr+4);
    				break;
    			default: break;
    		}

    		iseg.appendCode(Operator.PUSHI, 0);
    		iseg.appendCode(Operator.JUMP, compAddr+5);
    		iseg.appendCode(Operator.PUSHI, 1);
    	}
    	return hasLeftValue;
    }

    /**
     * <Arithmetic_expression>の構文解析をする．
     * 左辺値があるならtrueを返す
     */
    private boolean parseArithmetic_expression() {
    	boolean hasLeftValue = parseArithmetic_term();

    	while(token.checkSymbol(Symbol.ADD) ||
    			token.checkSymbol(Symbol.SUB)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる

    		char operator;
    		if(token.checkSymbol(Symbol.ADD))
    			operator = '+';
    		else
    			operator = '-';

    		token = lexer.nextToken();
    		if(firstExpression())
    			parseArithmetic_term();
    		else syntaxError("<Arithmetic_term>が期待されます");

    		/* コード生成 */
    		if(operator == '+')
    			iseg.appendCode(Operator.ADD);
    		else
    			iseg.appendCode(Operator.SUB);
    	}
    	return hasLeftValue;
    }

    /**
     * <Arithmetic_term>の構文解析をする．
     * 左辺値があるならtrueを返す
     */
    private boolean parseArithmetic_term() {
    	boolean hasLeftValue = parseArithmetic_factor();

    	while(token.checkSymbol(Symbol.MUL) ||
    			token.checkSymbol(Symbol.DIV) ||
    			token.checkSymbol(Symbol.MOD)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる

    		char operator;
    		if(token.checkSymbol(Symbol.MUL))
    			operator = '*';
    		else if(token.checkSymbol(Symbol.DIV))
    			operator = '/';
    		else
    			operator = '%';

    		token = lexer.nextToken();
    		if(firstExpression())
    			parseArithmetic_factor();
    		else syntaxError("<Arithmetic_factor>が期待されます");

    		/* コード生成 */
    		if(operator == '*')
    			iseg.appendCode(Operator.MUL);
    		else if(operator == '/')
    			iseg.appendCode(Operator.DIV);
    		else
    			iseg.appendCode(Operator.MOD);
    	}
    	return hasLeftValue;
    }

    /**
     * <Arithmetic_factor>の構文解析をする．
     * 左辺値があるならtrueを返す
     */
    private boolean parseArithmetic_factor() {
    	boolean hasLeftValue = false;
    	if(token.checkSymbol(Symbol.SUB) ||
    		token.checkSymbol(Symbol.NOT)) {
    		hasLeftValue = false;  //演算をすると左辺値ではなくなる
    		boolean minus = false;			//負の数であるか
    		if(token.checkSymbol(Symbol.SUB))
    			minus = true;
    		token = lexer.nextToken();

    		if(firstExpression())
    			parseArithmetic_factor();
    		else syntaxError("<Arithmetic_factor>が期待されます");

    		/* コード生成 */
    		if(minus) iseg.appendCode(Operator.CSIGN);
    		else iseg.appendCode(Operator.NOT);
    	} else {
    		hasLeftValue =  parseUnsigned_factor();
    	}
    	return hasLeftValue;
    }

    /**
     * <Unsigned_factor>の構文解析をする．
     * 左辺値があるならtrueを返す
     */
    private boolean parseUnsigned_factor() {
    	boolean hasLeftValue = false;

    	if(token.checkSymbol(Symbol.NAME)) {
    		hasLeftValue = true;	//左辺値が出てきた
    		String name = token.getStrValue();	//変数名を保存
    		int address = variableTable.getAddress(name);	//アドレスを保存
    		token = lexer.nextToken();

    		//型検査
    		if(!token.checkSymbol(Symbol.LBRACKET)) {
    			if(!variableTable.checkType(name, Type.INT))
    				syntaxError("型が不一致です");
    		}

    		//コード生成
    		if(token.checkSymbol(Symbol.ASSIGN) || token.checkSymbol(Symbol.LBRACKET) ||
    			token.checkSymbol(Symbol.ASSIGNADD) || token.checkSymbol(Symbol.ASSIGNSUB) ||
    			token.checkSymbol(Symbol.ASSIGNMUL) || token.checkSymbol(Symbol.ASSIGNDIV)) {	//左辺値の場合
    			iseg.appendCode(Operator.PUSHI, address); //左辺値を積む
    		} else {
    			iseg.appendCode(Operator.PUSH, address);  //右辺値を積む
    		}

    		// NAME++の場合
    		if(token.checkSymbol(Symbol.INC)) {
    			hasLeftValue = false;  //演算をすると左辺値ではなくなる
    			token = lexer.nextToken();

    			// 後置インクリメントのコード生成
    			iseg.appendCode(Operator.COPY);
    			iseg.appendCode(Operator.INC);
        		iseg.appendCode(Operator.POP, address);

        	// NAME--の場合
    		} else if(token.checkSymbol(Symbol.DEC)) {
    			hasLeftValue = false;  //演算をすると左辺値ではなくなる
    			token = lexer.nextToken();

    			// 後置デクリメントのコード生成
    			iseg.appendCode(Operator.COPY);
        		iseg.appendCode(Operator.DEC);
        		iseg.appendCode(Operator.POP, address);

    		} else if(token.checkSymbol(Symbol.LBRACKET)){	//配列の場合
    			//型検査
    			if(!variableTable.checkType(name, Type.ARRAYOFINT))
    				syntaxError("型が不一致です");

    			token = lexer.nextToken();

    			if(firstExpression())
    				parseExpression();	//配列の添字の部分を解析
    			else syntaxError("<Expression>が期待されます");

    			if(token.checkSymbol(Symbol.RBRACKET))
    				token = lexer.nextToken();
    			else syntaxError("']'が期待されます");

    			iseg.appendCode(Operator.ADD); //配列の番地を出す
    			if(!token.checkSymbol(Symbol.ASSIGN) && !token.checkSymbol(Symbol.ASSIGNADD) &&
    				!token.checkSymbol(Symbol.ASSIGNSUB) && !token.checkSymbol(Symbol.ASSIGNMUL) &&
    				!token.checkSymbol(Symbol.ASSIGNDIV) && !token.checkSymbol(Symbol.INC) &&
    				!token.checkSymbol(Symbol.DEC))	//左辺値かどうか
    				iseg.appendCode(Operator.LOAD);

    			// NAME[<Expression>]++の場合
    			if(token.checkSymbol(Symbol.INC)) {
    				hasLeftValue = false;  //演算をすると左辺値ではなくなる
    				token = lexer.nextToken();
    				// 後置インクリメントのコード生成
        			iseg.appendCode(Operator.COPY);
        			iseg.appendCode(Operator.LOAD);
        			iseg.appendCode(Operator.INC);
            		iseg.appendCode(Operator.ASSGN);
            		iseg.appendCode(Operator.DEC);
    			}

    			// NAME[<Expression>]--の場合
    			if(token.checkSymbol(Symbol.DEC)) {
    				hasLeftValue = false;  //演算をすると左辺値ではなくなる
    				token = lexer.nextToken();
    				// 後置インクリメントのコード生成
    				iseg.appendCode(Operator.COPY);
        			iseg.appendCode(Operator.LOAD);
        			iseg.appendCode(Operator.DEC);
            		iseg.appendCode(Operator.ASSGN);
            		iseg.appendCode(Operator.INC);
    			}
    		}

    	} else if(token.checkSymbol(Symbol.INC) ||
    			token.checkSymbol(Symbol.DEC)) {
    		Symbol op = token.getSymbol();
    		token = lexer.nextToken();
    		int addr = -1; //変数のアドレス記憶用
    		String name = "";  //変数名記憶用

    		if(token.checkSymbol(Symbol.NAME)) {
    			addr = variableTable.getAddress(token.getStrValue());
    			name = token.getStrValue();	//変数名を記憶
    			token = lexer.nextToken();
    		}
    		else syntaxError("NAMEが期待されます");

    		if(token.checkSymbol(Symbol.LBRACKET)){  //配列の場合
    			//型検査
    			if(!variableTable.checkType(name, Type.ARRAYOFINT))
    				syntaxError("型が不一致です");

    			token = lexer.nextToken();

    			iseg.appendCode(Operator.PUSHI, addr);

    			if(firstExpression())
    				parseExpression();
    			else syntaxError("<Expression>が期待されます");

    			if(token.checkSymbol(Symbol.RBRACKET))
    				token = lexer.nextToken();
    			else syntaxError("']'が期待されます");

    			//配列の場合の前置INC,DECのコード生成
    			iseg.appendCode(Operator.ADD);
    			iseg.appendCode(Operator.COPY);
    			iseg.appendCode(Operator.LOAD);
    			if(op == Symbol.INC) iseg.appendCode(Operator.INC);
        		else iseg.appendCode(Operator.DEC);
    			iseg.appendCode(Operator.ASSGN);
    		} else {
    			//配列でない場合の型検査
    			if(!variableTable.checkType(name, Type.INT))
    				syntaxError("型が不一致です");

    			//配列でない場合の前置INC,DECのコード生成
    			iseg.appendCode(Operator.PUSH, addr);
        		if(op == Symbol.INC) iseg.appendCode(Operator.INC);
        		else iseg.appendCode(Operator.DEC);
        		iseg.appendCode(Operator.COPY);
        		iseg.appendCode(Operator.POP, addr);
    		}


    	} else if(token.checkSymbol(Symbol.LPAREN)) {
    		token = lexer.nextToken();

    		if(firstExpression())
				parseExpression();
			else syntaxError("<Expression>が期待されます");

    		if(token.checkSymbol(Symbol.RPAREN))
				token = lexer.nextToken();
			else syntaxError("')'が期待されます");

    	} else if(token.checkSymbol(Symbol.INTEGER) ||
    				token.checkSymbol(Symbol.CHARACTER)) {
    		int value = token.getIntValue();
    		token = lexer.nextToken();
    		iseg.appendCode(Operator.PUSHI, value);
    	} else if(token.checkSymbol(Symbol.INPUTINT)) {
    		token = lexer.nextToken();
    		iseg.appendCode(Operator.INPUT);
    	} else if(token.checkSymbol(Symbol.INPUTCHAR)) {
    		token = lexer.nextToken();
    		iseg.appendCode(Operator.INPUTC);
    	} else
    		token = lexer.nextToken();
    	return hasLeftValue;
    }

    /**
     * 現在のトークンがStatementのファースト集合に含まれていれば，
     * trueを返し，そうでなければfalseを返す．
     */
    private boolean firstStatement() {
    	if(token.checkSymbol(Symbol.INT) ||
		   token.checkSymbol(Symbol.IF) ||
    	   token.checkSymbol(Symbol.WHILE) ||
    	   token.checkSymbol(Symbol.FOR) ||
    	   token.checkSymbol(Symbol.NAME) ||
    	   token.checkSymbol(Symbol.INC) ||
    	   token.checkSymbol(Symbol.DEC) ||
    	   token.checkSymbol(Symbol.INTEGER) ||
    	   token.checkSymbol(Symbol.CHARACTER) ||
    	   token.checkSymbol(Symbol.LPAREN) ||
    	   token.checkSymbol(Symbol.INPUTCHAR) ||
    	   token.checkSymbol(Symbol.INPUTINT) ||
    	   token.checkSymbol(Symbol.SUB) ||
    	   token.checkSymbol(Symbol.NOT) ||
    	   token.checkSymbol(Symbol.OUTPUTCHAR) ||
    	   token.checkSymbol(Symbol.OUTPUTINT) ||
    	   token.checkSymbol(Symbol.BREAK) ||
    	   token.checkSymbol(Symbol.LBRACE) ||
    	   token.checkSymbol(Symbol.SEMICOLON))
    		return true;
    	else return false;
    }

    /**
     * 現在のトークンがExpressionのファースト集合に含まれていれば，
     * trueを返し，そうでなければfalseを返す．
     */
    private boolean firstExpression() {
    	if(token.checkSymbol(Symbol.NAME) ||
			token.checkSymbol(Symbol.INC) ||
			token.checkSymbol(Symbol.DEC) ||
			token.checkSymbol(Symbol.INTEGER) ||
			token.checkSymbol(Symbol.CHARACTER) ||
			token.checkSymbol(Symbol.LPAREN) ||
			token.checkSymbol(Symbol.INPUTCHAR) ||
			token.checkSymbol(Symbol.INPUTINT) ||
			token.checkSymbol(Symbol.SUB) ||
			token.checkSymbol(Symbol.NOT))
    		return true;
    	else return false;
    }


    /**
     * 現在読んでいるファイルを閉じる (lexerのcloseFile()に委譲)
     */
    void closeFile() {
    	lexer.closeFile();
    }

    /**
     * アセンブラコードをファイルに出力する (isegのdump2file()に委譲)
     *
     */
    void dump2file() {
    	iseg.dump2file();
    }

    /**
     * アセンブラコードをファイルに出力する (isegのdump2file()に委譲)
     *
     */
    void dump2file (String fileName) {
    	iseg.dump2file(fileName);
    }

    /**
     * エラーメッセージを出力しプログラムを終了する
     * @param message 出力エラーメッセージ
     */
    private void syntaxError (String message) {
        System.out.print (lexer.analyzeAt());
        //下記の文言は自動採点で使用するので変更しないでください。
        System.out.println ("で構文解析プログラムが構文エラーを検出");
        System.out.println (message);
        closeFile();
        System.exit(0);
    }

    /**
     * 引数で指定したK21言語ファイルを解析する
     * 読み込んだファイルが文法上正しければアセンブラコードを出力する
     */
    public static void main (String[] args) {
        Kc parser;

        if (args.length == 0) {
            System.out.println ("Usage: java kc.Kc21 file [objectfile]");
            System.exit (0);
        }

        parser = new Kc (args[0]);

        parser.parseProgram();
        parser.closeFile();

        if (args.length == 1)
            parser.dump2file();
        else
            parser.dump2file (args[1]);
    }
}
