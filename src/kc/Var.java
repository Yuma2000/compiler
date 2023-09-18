package kc;

/**
 * 変数を表すクラス
 * タイプ、名前、アドレス、サイズを持つ
 */
class Var {
	// フィールド
	/**
	 * Typeはenum型のクラスであり，
	 * Type.INT, Type.ARRAYOFINT, Type.NULLのいずれかの値を持つ
	 */
	private Type type;

	/**
	 * 変数名
	 */
	private String name;

	/**
	 * Dseg上のアドレス
	 */
	private int address;

	/**
	 * 配列の場合，そのサイズ
	 */
	private int size;

	// コンストラクタ
    /**
     * 各フィールドを引数で与えられたもので初期化する
     */
    Var(Type type, String name, int address, int size) {
    	this.type = type;
    	this.name = name;
    	this.address = address;
    	this.size = size;
    }

    // 以下それぞれのゲッター
    /**
     * フィールドtypeのゲッター
     * @return type
     */
	public Type getType() {
		return type;
	}

	/**
     * フィールドnameのゲッター
     * @return name
     */
	public String getName() {
		return name;
	}

	/**
     * フィールドaddressのゲッター
     * @return address
     */
	public int getAddress() {
		return address;
	}

	/**
     * フィールドsizeのゲッター
     * @return size
     */
	public int getSize() {
		return size;
	}
}
