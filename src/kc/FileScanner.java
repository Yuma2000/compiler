package kc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイルを一文字ずつスキャンするクラス
 */
class FileScanner {

	/**
	 * 入力ファイルの参照
	 */
	private BufferedReader sourceFile;

	/**
	 * 行バッファ
	 */
	private String line;

	/**
	 * 行カウンタ
	 */
	private int lineNumber;

	/**
	 * 列カウンタ
	 */
	private int columnNumber;

	/**
	 * 読み取り文字
	 */
	private char currentCharacter;

	/**
	 * 先読み文字
	 */
	private char nextCharacter;


    /**
     * コンストラクタ
     * 引数 sourceFileName で指定されたファイルを開き, sourceFile で参照する．
     * また lineNumber, columnNumber, currentCharacter, nextCharacter を初期化する
     * @param sourceFileName ソースプログラムのファイル名
     */
    FileScanner (String sourceFileName) {
        Path path = Paths.get (sourceFileName);
        // ファイルのオープン
        try {
            sourceFile = Files.newBufferedReader (path);
        } catch (IOException err_mes) {
            System.out.println (err_mes);
            System.exit (1);
        }

        // 各フィールドの初期化
        this.lineNumber = 0;
        this.columnNumber = -1;
        this.nextCharacter = '\n';

        // nextChar()の実行
        this.nextChar();
    }

    /**
     * sourceFileで参照しているファイルを閉じる
     */
    void closeFile() {
        try {
            sourceFile.close();
        } catch (IOException err_mes) {
            System.out.println (err_mes);
            System.exit (1);
        }
    }

    /**
     * sourceFileで参照しているファイルから一行読み, フィールドline(文字列変数)にその行を格納する
     */
    void readNextLine() {
        try {
            if (sourceFile.ready()) { // sourceFile中に未読の行があるかを確認 (例外:IllegalStateException)
                /*
                 * nextLineメソッドでsourceFileから1行読み出し 読み出された文字列は改行コードを含まないので
                 * 改めて改行コードをつけ直す
                 */
            	this.line = this.sourceFile.readLine() + '\n';

            } else {
            	this.line = null;
            }
        } catch (IOException err_mes) { // 例外はExceptionでキャッチしてもいい
            // ファイルの読み出しエラーが発生したときの処理
            System.out.println (err_mes);
            closeFile();
            System.exit (1);
        }
    }
    /**
     * nextCharacter中の文字を返す
     */
    char lookAhead() {
    	return this.nextCharacter;
    }

    /**
     * lineフィールドの文字列を返す
     */
    String getLine() {
    	return this.line;
    }

    /**
     * 一文字切り出し用メソッド
     */
     char nextChar() {
    	 this.currentCharacter = this.nextCharacter;
    	 if(this.nextCharacter == '\0') {
    		 // 何もしない
    	 } else if(this.nextCharacter == '\n') {
    		 this.readNextLine();
    		 if(this.line != null) {
    			 this.nextCharacter = this.line.charAt(0);  //次の行の0文字目を代入
    			 this.lineNumber++;
    			 this.columnNumber = 0;
    		 } else {
    			 this.nextCharacter = '\0';
    		 }
    	 } else {
    		 this.columnNumber++;
    		 this.nextCharacter = this.line.charAt(this.columnNumber);
    	 }
    	 return this.currentCharacter;
     }

    /**
     * 現在入力ファイルのどの部分をスキャンしているのかを表現する文字列を返す
     */
     String scanAt() {
    	 return this.lineNumber + "行目" + this.columnNumber + "文字目";
     }

    /**
     * メインメソッド
     * 読み込んだファイルの全文を表示する
     * 問題2.6で使用したコードはコメントアウトしている
     * @param args
     */
    public static void main (String args[]) {
    	FileScanner fileScanner = new FileScanner("bsort.k");
//    	fileScanner.readNextLine();
    	while(fileScanner.getLine() != null) {
    		System.out.print(fileScanner.nextChar());
//    		System.out.print(fileScanner.getLine());
//    		fileScanner.readNextLine();
    	}
    	fileScanner.closeFile();
    }
}