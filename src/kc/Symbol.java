package kc;
enum Symbol {
    NULL,									//
    MAIN,       /* main */					//
    IF,         /* if */					//
    WHILE,      /* while */					//
	FOR,        /* for */					//
    INPUTINT,   /* inputint */				//
    INPUTCHAR,  /* inputchar */				//
    OUTPUTINT,  /* outputint */				//
    OUTPUTCHAR, /* outputchar */			//
    OUTPUTSTR,  /* outputstr(拡張用) */
    SETSTR,     /* setstr   (拡張用) */
    ELSE,       /* else     (拡張用) */
    DO,         /* do       (拡張用) */
    SWITCH,     /* switch   (拡張用) */
    CASE,       /* case     (拡張用) */
    BREAK,      /* break */					//
    CONTINUE,   /* continue (拡張用) */
    INT,        /* int */					//
    CHAR,       /* char     (拡張用) */
    BOOLEAN,    /* boolean  (拡張用) */
    TRUE,       /* true     (拡張用) */
    FALSE,      /* false    (拡張用) */
    EQUAL,      /* == */					//
    NOTEQ,      /* != */					//
    LESS,       /* < */						//
    GREAT,      /* > */						//
    LESSEQ,     /* <=       (拡張用) */
    GREATEQ,    /* >=       (拡張用) */
    AND,        /* && */					//
    OR,         /* || */					//
    NOT,        /* ! */						//
    ADD,        /* + */						//
    SUB,        /* - */						//
    MUL,        /* * */						//
    DIV,        /* / */						//
    MOD,        /* % */						//
    ASSIGN,     /* = */						//
    ASSIGNADD,  /* += */					//
    ASSIGNSUB,  /* -= */					//
    ASSIGNMUL,  /* *= */					//
    ASSIGNDIV,  /* /= */					//
    ASSIGNMOD,  /* %=       (拡張用) */
    INC,        /* ++ */					//
    DEC,        /* -- */					//
    SEMICOLON,  /* ; */						//
    LPAREN,     /* ( */						//
    RPAREN,     /* ) */						//
    LBRACE,     /* { */						//
    RBRACE,     /* } */						//
    LBRACKET,   /* [ */						//
    RBRACKET,   /* ] */						//
    COMMA,      /* , */						//
    INTEGER,    /* 整数 */					//
    CHARACTER,  /* 文字 */
    NAME,       /* 変数名 */					//
    STRING,     /* 文字列   (拡張用) */
    ERR,        /* エラー */
    EOF         /* end of file */			//
}
