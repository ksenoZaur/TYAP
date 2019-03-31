package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class Tuple<X, Y> {
    public final X x;
    public final Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}

public class Scaner {
    // Const
    private final int _MAX_LEX = 20;

    public static final int _FOR = 1;
    public static final int _STRUCT = 2;
    public static final int _CONST = 3;
    public static final int _INT = 4;
    public static final int _FLOAT = 5;
    public static final int _MAIN = 6;

    public static final int _ID = 10;

    public static final int _TYPE_INT_10 = 20;
    public static final int _TYPE_FLOAT = 21;

    public static final int _POINT = 30;
    public static final int _COMMA = 31;
    public static final int _SEMICOLON = 32;
    public static final int _PARENTHESIS_OPEN = 33;
    public static final int _PARENTHESIS_CLOSE = 34;
    public static final int _BRACE_OPEN = 35;
    public static final int _BRACE_CLOSE = 36;

    public static final int _LESS = 40;
    public static final int _GREAT = 41;
    public static final int _EQUALLY = 42;
    public static final int _LESS_EQUALLY = 43;
    public static final int _GREAT_EQUALLY =  44;
    public static final int _NOT_EQUALLY = 45 ;
    public static final int _PLUS = 46;
    public static final int _MINUS = 47;
    public static final int _STAR =  48;
    public static final int _SLASH = 49;
    public static final int _PERCENT = 50;
    public static final int _ASSIGN = 51 ;

    public static final int _ERROR = 200;
    public static final int _END = 100;

    // Fields
    private ArrayList<Character> t;       // Исходный текст
    private ArrayList<Integer> indexOfLine;
    private ArrayList<Tuple<String,Integer>> KEYWORD;
    private int uk;                      // указатель текущей позиции в исходном тексте
//    private int lines ;
    private int i = 0 ;                  // Текущая длина лексемы
//    private int old_line = 0;

    // Methods
    public Scaner() throws IOException {
        this.t = new ArrayList<>();
        this.indexOfLine = new ArrayList<>();
        this.KEYWORD = new ArrayList<>();

        this.KEYWORD.add(new Tuple<>("int",_INT));
        this.KEYWORD.add(new Tuple<>("float", _FLOAT));
        this.KEYWORD.add(new Tuple<>("struct", _STRUCT));
        this.KEYWORD.add(new Tuple<>("const", _CONST));
        this.KEYWORD.add(new Tuple<>("main",_MAIN));
        this.KEYWORD.add(new Tuple<>("for",_FOR));

        String path = System.getProperty("user.dir") + "\\test.txt";
        try {
            FileReader fileReader = new FileReader(path);
        }catch (FileNotFoundException ex){
            PrintError("Файла не существует");
            System.exit(404);
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader( path));

        int symbol = bufferedReader.read();
        // Когда дойдём до конца файла, получим '-1'
        Integer lines = 1;

        while (symbol != -1) {
            char c = (char) symbol;
            if(c != '\r') {
                this.t.add(c);
                this.indexOfLine.add(lines);
                if (c == '\n') {
                    lines++;
                }
            }
            symbol = bufferedReader.read(); // Читаем символ
        }
        this.t.add('\0');
        this.indexOfLine.add( lines );
    }

    public int next(ArrayList<Character> l){
        i = 0;
        l.clear();
        char current = ' ';
        try {
            current = t.get(uk);
        }catch (IndexOutOfBoundsException  ex){
            return _END;
        }

        return startMetka(l);
    }

    private int startMetka(ArrayList<Character> l){
        // все игнорируемые элементы:
        while( ( t.get(uk) == ' ' ) || ( t.get(uk) == '\n') || ( t.get(uk) == '\t')   ) {
//            if( t.get(uk) == '\n' ) {
////                lines++;
//            }
            uk++;
        }
//        char current = t.get(uk);
        // пропуск незначащих элементов
        if ( (t.get(uk)=='/') && (t.get(uk+1)=='/') ) {
            // начался комментарий, надо пропустить текст до '\n'
            uk = uk + 2;
            while ( t.get(uk) != '\n' && t.get(uk) != '\0' && (t.get(uk) != '#')){
//                if(t.get(uk) == '\n') {
//                    lines++;
//                }
                uk++;
            }
            return this.startMetka(l); //goto startMetka;
        }

        if ( t.get(uk) == '\0' ) {
            l.add('#');         //l.set(0,'#');   // l.get(0) = '#';
            return _END;
        }
        /////////////////////////////////  /////////////////////////////////  /////////////////////////////////
        // Десятичная цифра
        if( t.get(uk) >= '0' && t.get(uk) <= '9'){
            l.add( t.get(uk++) );
            i++;
            while ( t.get(uk) >= '0' && t.get(uk) <= '9' ){
                if( i < _MAX_LEX - 1){
                    l.add(t.get(uk++));
                    i++;
                } else {
                    uk++;
                }
            }
            // тут кажется надо посмотреть, если t.get(uk)=='.'
            // то число с фиксированной точкой
            if( t.get(uk) == '.' ){
                l.add( t.get(uk++) );
                i++;
                while ( t.get(uk) >= '0' && t.get(uk) <= '9' ){
                    if( i < _MAX_LEX - 1){
                        l.add(t.get(uk++));
                        i++;
                    } else {
                        uk++;
                    }
                }
                return _TYPE_FLOAT;
            }
            return _TYPE_INT_10;
        } else if ( ( t.get(uk) >= 'a'  && t.get(uk) <= 'z' ) || ( t.get(uk) >= 'A' && t.get(uk) <= 'Z' ) || t.get(uk) == '_') {
            // начинается идентификатор
            l.add( t.get(uk++) );
            i++;
            while ( ( t.get(uk) >= '0'  &&  t.get(uk) <= '9'  ) || ( t.get(uk) >= 'a'  && t.get(uk) <= 'z' ) ||
                    ( t.get(uk) >= 'A' && t.get(uk) <= 'Z' ) ) {
                if ( i < _MAX_LEX - 1 ) {
                    l.add( t.get(uk++) );
                    i++;
                } else {
                    uk++;
                }
            }
            // длинный идентификатор обрезали
            // проверка на ключевое слово:
            int resultCheckKEYWORD = checkKEYWORD( l );
            if( resultCheckKEYWORD!= 0) {
                return resultCheckKEYWORD;
            }
            return _ID;
        } else if ( t.get(uk) == '.' ){
            l.add(t.get(uk++));
            i++;
            return _POINT;
        } else if( t.get(uk) == '>'){
            l.add( t.get(uk++) );
            i++;
            if( t.get(uk) == '=' ){
                l.add( t.get(uk++) );
                i++;
                return _GREAT_EQUALLY;
            }
            return _GREAT;
        } else if( t.get(uk) == '<' ){
            l.add(t.get(uk++));
            i++;
            if(t.get(uk) == '='){
                l.add(t.get(uk++));
                i++;
                return _LESS_EQUALLY;
            }
            return _LESS;
        } else if( t.get(uk) == '!') {
            l.add( t.get(uk++) );
            i++;
            if( t.get(uk) == '='){
                l.add(t.get(uk++));
                i++;
                return _NOT_EQUALLY;
            } else {
                // ошибка
                PrintError("Неверный символ",l);
                return _ERROR;
            }
        } else if ( t.get(uk)  == '=') {
            l.add(t.get(uk++));
            i++;
            if ( t.get(uk) == '=') {
                l.add(t.get(uk++));
                i++;
                return _EQUALLY;
            }
            return _ASSIGN;
        } else if ( t.get(uk) == '+' ) {
            l.add(t.get(uk++));
            i++;
            return _PLUS;
        } else if ( t.get(uk) == '-' ) {
            l.add(t.get(uk++));
            i++;
            return _MINUS;
        } else if ( t.get(uk) == '*' ) {
            l.add(t.get(uk++));
            i++;
            return _STAR;
        } else if ( t.get(uk) == '%' ) {
            l.add(t.get(uk++));
            i++;
            return _PERCENT;
        } else if ( t.get(uk) == '/' ) {
            l.add(t.get(uk++));
            i++;
            return _SLASH;
        } else if ( t.get(uk) == ';' ) {
            l.add(t.get(uk++));
            i++;
            return _SEMICOLON;
        } else if ( t.get(uk) == ',' ) {
            l.add(t.get(uk++));
            i++;
            return _COMMA;
        } else if ( t.get(uk) == '(' ) {
            l.add(t.get(uk++));
            i++;
            return _PARENTHESIS_OPEN;
        } else if ( t.get(uk) == ')' ) {
            l.add(t.get(uk++));
            i++;
            return _PARENTHESIS_CLOSE;
        } else if ( t.get(uk) == '{' ) {
            l.add(t.get(uk++));
            i++;
            return _BRACE_OPEN;
        } else if ( t.get(uk) == '}' ) {
            l.add(t.get(uk++));
            i++;
            return _BRACE_CLOSE;
        } else {
            // ошибка
            PrintError("Неверный символ",l);
            uk++;
            return _ERROR;
        }
    }

    // проверка на ключевое слово:
    public int checkKEYWORD( ArrayList<Character> l){
        // формируем строку из текущей лексеым
        String lexem = "";
        for( int j = 0 ; j < l.size(); j++) {
            lexem += l.get(j);
        }
        // проверяем текущую лексему, с ключевыми словами языка
        for(int j = 0 ; j < KEYWORD.size(); j++){
            String tmp = KEYWORD.get(j).x;
            if( lexem.equals(tmp) ) {
                return KEYWORD.get(j).y;
            }
        }
        return 0;
    }

    public void PrintNumStr(String str, ArrayList<Character> l ){
        String lexem = "";
        for(int j = 0 ; j < l.size(); j++)
            lexem += l.get(j);
        System.out.print("Ошибка: " + str);
        System.out.print( " Строка " + (this.indexOfLine.get( this.uk )) );
        System.out.print( "\n");
    }

    public void PrintError(String str, ArrayList<Character> l ){
        String lexem = "";
        for(int j = 0 ; j < l.size(); j++)
            lexem += l.get(j);
        System.out.print("Ошибка: " + str + "\t|\t Неверный символ: " + lexem  );
        System.out.print( " Строка " + (this.indexOfLine.get( this.uk )) );
        System.out.print( "\n");
    }

    public void PrintError(String str ){
        System.out.print("Ошибка: " + str + "\t\n");
    }

    public void setUK( int uk ){
//        this.lines = this.old_line;
        this.uk = uk;
    }

    public Integer getUK(){
//        this.old_line = this.uk;
        return this.uk;
    }
}
