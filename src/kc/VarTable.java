package kc;

import java.util.ArrayList;

/**
 * 変数表クラス
 * 変数クラスのVarをArrayListに格納し，それに対する処理をする
 * @author 19-1-037-0032 竹田有真
 * 問題番号: 問題2.11
 * 提出日: 2021年5月12日
 */
class VarTable {
	// フィールド
    /**
     * 変数表
     */
	private ArrayList<Var> varList;

	/**
	 * 次に登録される変数のアドレス
	 */
	private int nextAddress;


	// コンストラクタ
	/**
	 * ArrayList<Var>を一つ作り，varList で参照する
	 * nextAddressを0に初期化する
	 */
	VarTable() {
		this.varList = new ArrayList<Var>();
		this.nextAddress = 0;
	}

    /**
     * varList中から，引数で与えられた名前nameを持つ
     * 変数(Varクラスのインスタンス)を探し，その参照を戻り値として返す.
     * そのような変数が存在しない場合null値を返す.
     * VarTableクラス内部からのみ呼び出される
     */
    private Var getVar(String name) {
    	for(Var var: varList) {
    		if(var.getName().equals(name))
    			return var;
    	}
    	return null;
    }

    /**
     * 引数で与えられた名前nameを持つ変数が既に存在するかどうかを調べ,
     * 戻り値として返す
     */
    boolean exist(String name) {
    	boolean flag = false;
    	if(getVar(name) != null)
    		flag = true;
    	return flag;
    }

    /**
     * 引数で与えられた型,名前，サイズを持つ変数を登録する.
     * 登録できたら戻り値trueを返す.
     * 既に varList 中に同じ名前の変数が存在する場合は登録せず，
     * 戻り値falseを返す
     */
    boolean registerNewVariable(Type type, String name, int size) {
    	boolean ret = false;
    	if(!exist(name)) {
    		varList.add(new Var(type, name, nextAddress, size));   // 変数表に追加
    		this.nextAddress += size; //nextAddressを更新
    		ret = true;
    	}
    	return ret;
    }

    /**
     * 名前nameを持つ変数に与えられているDsegのアドレスを求めて，戻り値として返す
     * nameを持つ変数が無ければ-1を返す
     */
    int getAddress(String name) {
    	if(exist(name))
    		return getVar(name).getAddress();
    	else
    		return -1;
    }

    /**
     * 名前nameを持つ変数の型を戻り値として返す
     */
    Type getType(String name) {
    	if(exist(name))
    		return getVar(name).getType();
    	else
    		return Type.NULL;
    }

    /**
     * 第1引数nameで与えられた変数の型が第2引数typeと一致するかを確認する
     */
    boolean checkType(String name, Type type) {
    	return getType(name) == (type);
    }

    /**
     * 名前nameを持つ変数のサイズを返す
     */
    int getSize(String name) {
    	if(exist(name))
    		return getVar(name).getSize();
    	else
    		return -1;
    }

    /**
     * 変数表に登録されている変数の個数を返す
     */
    int size() {
    	return varList.size();
    }

    /**
     * 引数で指定した位置から後の変数を変数表から削除する
     */
    void removeTail (int index) {
    	if(index >= 0 && index < varList.size()) {
    		this.nextAddress = varList.get(index).getAddress(); // nextAddressを記憶しておく
    		while(index < varList.size())
    			varList.remove(index);
    	}
    }

    /**
     * 動作確認用のメインメソッド
     * int型変数およびint型配列を表に登録し、その後登録された変数を表示する
     */
    public static void main (String[] args) {
    	VarTable varTable = new VarTable();
    	for(int i = 0; i < 4; i++) {
    		varTable.registerNewVariable(Type.INT, "var" + i, 1);
    	}
    	varTable.registerNewVariable(Type.ARRAYOFINT, "var4", 10);
    	for(Var var: varTable.varList) {
    		if(varTable.checkType(var.getName(), Type.INT)) {
    			System.out.println("タイプ:" + var.getType());
    			System.out.println("アドレス:" + var.getAddress());
    		} else if(varTable.checkType(var.getName(), Type.ARRAYOFINT)){
    			System.out.println("タイプ:" + var.getType());
    			System.out.println("アドレス:" + var.getAddress());
    		}
    	}
    }
}