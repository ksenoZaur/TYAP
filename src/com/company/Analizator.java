package com.company;

import java.io.IOException;
import java.util.ArrayList;

public class Analizator {

    private TriadBuilder triadBuilder;
    private String resultTriad;

    // Fields
    private Scaner scaner;
    private Integer currentLEX;
    private Semantic sema;
    private Integer currentType;

    private TData currentData;  // Заменит currentType

    private String nameVariable;


    private boolean flag;

    // Methods
    public Analizator() {

        this.triadBuilder = new TriadBuilder();

        try {
            this.scaner = new Scaner();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.sema = new Semantic(this.scaner);
        this.program();
    }

    public boolean program() {
        ArrayList<Character> l = new ArrayList<>();
        Integer savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(savePointer);

        while (this.currentLEX == Scaner._ID || this.currentLEX == Scaner._FLOAT ||
                this.currentLEX == Scaner._INT || this.currentLEX == Scaner._STRUCT) {
            if (this.currentLEX == Scaner._INT || this.currentLEX == Scaner._ID || this.currentLEX == Scaner._FLOAT) {

                this.currentLEX = this.scaner.next(l);
                this.currentLEX = this.scaner.next(l);
                this.scaner.setUK(savePointer);

                if (this.currentLEX == Scaner._MAIN) {
                    this.currentLEX = this.scaner.next(l);
                    if (this.currentLEX != Scaner._INT) {
                        this.scaner.PrintError("Ожидался тип int", l);
                        return false;
                    }

                    /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
//                    String type = TableType.find( l.toString() );
//                    TableType.addType( type );

                    this.currentLEX = this.scaner.next(l);
                    if (this.currentLEX != Scaner._MAIN) {
                        this.scaner.PrintError("Ожидался тип main", l);
                        return false;
                    }

                    /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
                    this.sema.sem666(l, TableType.find("int"));
//                    this.sema.insertLeft( new Node(Node.typeFunc,TableType.find( type ),l.toString(), false, false));

                    this.currentLEX = this.scaner.next(l);
                    if (this.currentLEX != Scaner._PARENTHESIS_OPEN) {
                        this.scaner.PrintError("Ожидался символ (", l);
                        return false;
                    }

                    this.currentLEX = this.scaner.next(l);
                    if (this.currentLEX != Scaner._PARENTHESIS_CLOSE) {
                        this.scaner.PrintError("Ожидался символ )", l);
                        return false;
                    }

                    if (!this.composite()) {
                        return false;
                    }

                } else {
                    if (!this.data()) {
                        return false;
                    }
                }
            } else if (this.currentLEX == Scaner._STRUCT) {
                if (!this.struct()) {
                    return false;
                }
            }
            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(savePointer);
        }
        if (l.get(0) != '#') {
            this.scaner.PrintError("Ошибка", l);
        }
        return true;
    }

    private boolean composite() {

        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._BRACE_OPEN) {
            this.scaner.PrintError("Ожидался символ {", l);
            return false;
        }

        boolean saveFirst = this.sema.first;
        boolean saveCreate = this.sema.createVariable;

        if( !this.sema.first ) {

            this.sema.first = true;

        } else {

            this.sema.createVariable = true;
        }

        /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
        // Добавили две пустые вершины - одну слева, другую справа от нее
        Tree returnAddres = this.sema.sem1();


        Integer savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        while (this.currentLEX == Scaner._CONST ||
                this.currentLEX == Scaner._INT ||
                this.currentLEX == Scaner._FLOAT ||
                this.currentLEX == Scaner._ID ||
                this.currentLEX == Scaner._SEMICOLON ||
                this.currentLEX == Scaner._FOR ||
                this.currentLEX == Scaner._BRACE_OPEN) {

            this.scaner.setUK(savePointer);

            if (this.currentLEX == Scaner._INT ||
                    this.currentLEX == Scaner._FLOAT ||
                    this.currentLEX == Scaner._ID ||
                    this.currentLEX == Scaner._FOR ||
                    this.currentLEX == Scaner._SEMICOLON ||
                    this.currentLEX == Scaner._BRACE_OPEN) {

                if (this.currentLEX == Scaner._ID) {
                    this.currentLEX = this.scaner.next(l);
                    this.currentLEX = this.scaner.next(l);
                    this.scaner.setUK(savePointer);
                    if (this.currentLEX != Scaner._ID) {
                        // данные
                        if (!this.operator()) {
                            return false;
                        }
                    } else {
                        if (!this.data()) {
                            return false;
                        }
                    }
                } else if (this.currentLEX == Scaner._SEMICOLON || this.currentLEX == Scaner._FOR || this.currentLEX == Scaner._BRACE_OPEN) {
                    // данные
                    if (!this.operator()) {
                        return false;
                    }
                } else {
                    if (!this.data()) {
                        return false;
                    }
                }
            } else if (this.currentLEX == Scaner._CONST) {
                if (!this.const_()) {
                    return false;
                }
            }
            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
        }
        if (this.currentLEX != Scaner._BRACE_CLOSE) {
            this.scaner.PrintError("Ожидался символ }", l);
            return false;
        }

      //  this.sema.first = saveFirst;

        /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
        // Вернемся к вершине с открытой фигурной скобкой
        this.sema.sem2(returnAddres);

        this.sema.first = saveFirst;
        this.sema.createVariable = saveCreate;

        return true;
    }

    private boolean const_() {
        Integer savePointer;
        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._CONST) {
            this.scaner.PrintError("Ожидался символ const", l);
            return false;
        }

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._INT && this.currentLEX != Scaner._FLOAT) {
            this.scaner.PrintError("Ожидался символ int или float", l);
            return false;
        }

//        Integer type;
        TData data1;

//        type = this.sema.sem14(l);
        data1 = this.sema.sem14(l);

        do {

            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._ID) {
                this.scaner.PrintError("Ожидался символ идентификатор", l);
                return false;
            }

            ArrayList<Character> id = new ArrayList<>(l);

            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._ASSIGN) {
                this.scaner.PrintError("Ожидался символ =", l);
                return false;
            }

            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._PARENTHESIS_OPEN &&
                    this.currentLEX != Scaner._TYPE_INT_10 &&
                    this.currentLEX != Scaner._TYPE_FLOAT &&
                    this.currentLEX != Scaner._ID) {
                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;
            }

            this.scaner.setUK(savePointer);

//            Integer type2 = null;
            TData data2 = null;

            if ( !this.expression() ) {
                return false;
            }

//            type2 = this.currentType;
            data2 = this.currentData;

//            this.sema.sem13(id, type, type2);
            if( this.sema.sem13(id, data1, data2) == null ){
                return false;
            }

            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(savePointer);

        } while (this.currentLEX == Scaner._COMMA);

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._SEMICOLON) {

            this.scaner.PrintError("Ожидался символ ;", l);
            return false;

        }
        return true;
    }

    public boolean operator() {
        ArrayList<Character> l = new ArrayList<>();
        Integer savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        if ( this.currentLEX != Scaner._SEMICOLON &&
                this.currentLEX != Scaner._ID &&
                this.currentLEX != Scaner._FOR &&
                this.currentLEX != Scaner._BRACE_OPEN ) {

            this.scaner.PrintError("Ожидался другой символ for | ; | id | {", l);
            return false;

        }

        if (this.currentLEX == Scaner._ID) {
            this.scaner.setUK(savePointer);

            if (!this.assignment()) {
                return false;
            }

            this.currentLEX = this.scaner.next(l);
            if (this.currentLEX != Scaner._SEMICOLON) {
                this.scaner.PrintError("Ожидался символ ;", l);
                return false;
            }
        } else if (this.currentLEX == Scaner._FOR) {
            this.scaner.setUK(savePointer);

            // Сохраняем значение флага
            boolean saveCreateFlag = this.sema.createVariable;
            this.sema.createVariable = true;

            if (!this.for_()) {
                return false;
            }

            // Восстанавливаем значение флага
            this.sema.createVariable = saveCreateFlag;

        } else if (this.currentLEX == Scaner._BRACE_OPEN) {
            this.scaner.setUK(savePointer);
            if (!this.composite()) {
                return false;
            }
        }
        // Сработает когда будет считана ;
        return true;
    }

    private boolean for_() {

        // Сохраняем значение флага
        boolean before = this.sema.fIner;
        boolean test1 = this.sema.first;
        this.sema.first = true;

        Integer savePointer;
        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._FOR) {
            this.scaner.PrintError("Ожидался символ for", l);
            return false;
        }

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._PARENTHESIS_OPEN) {
            this.scaner.PrintError("Ожидался символ (", l);
            return false;
        }

        savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(savePointer);

        if (this.currentLEX != Scaner._ID) {

            this.scaner.PrintError("Ожидался символ идентификатор", l);
            return false;

        }

        // Первая зависимость от флага
        if (!this.assignment()) {

            return false;

        }

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._SEMICOLON) {

            this.scaner.PrintError("Ожидался символ ;", l);
            return false;

        }

        // Мы находимся здесь: for(x=v();  <-
        // Нужно сохранить указатель на это место МЕТКА1, чтобы
        // возвращаться к нему для проверки условия цикла

        Integer metka1 = this.scaner.getUK();
        start: do{

            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(savePointer);

            if (this.currentLEX != Scaner._PARENTHESIS_OPEN &&
                    this.currentLEX != Scaner._TYPE_INT_10 &&
                    this.currentLEX != Scaner._TYPE_FLOAT &&
                    this.currentLEX != Scaner._ID) {

                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;

            }

            Integer type = null;
            TData data = null;

            // Вторая зависимость от флага
            if (!this.expression()) {
                return false;
            }

            //type = this.currentType;
            data = this.currentData;

            if( data == null ){
                return false;
            }

            // Третья зависимость от флага
            this.sema.sem20(data, l);
            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._SEMICOLON) {

                this.scaner.PrintError("Ожидался символ ;", l);
                return false;

            }

            // Мы находимся здесь: for(x=v(); a == b; <-
            // Если a==b - true (что-то делать); fIntr = false;
            // Ставим метку МЕТКА2, чтобы возвращаться к ней после каждой итерации

            // Нужно проработать случаи когда value Integer и Float
            boolean bodyFlag;

            if (data.getType().equals(TableType.find("bool"))) {

                bodyFlag = this.sema.fIner && data.getValue().getDataAsBool();

            } else if (data.getType().equals(TableType.find("int"))) {

                bodyFlag = this.sema.fIner && (data.getValue().getDataAsInt() != 0);

            } else {

                bodyFlag = this.sema.fIner && (data.getValue().getDataAsFloat() != 0);
            }

            Integer metka2 = this.scaner.getUK();
            this.sema.fIner = false;

            increment: do{

                savePointer = this.scaner.getUK();
                this.currentLEX = this.scaner.next(l);
                this.scaner.setUK(savePointer);

                if (this.currentLEX != Scaner._ID) {

                    this.scaner.PrintError("Ожидался символ идентификатор", l);
                    return false;

                }

                // Четвертая зависимость от флага
                if (!this.assignment()) {

                    return false;

                }

                // Проверяем условие выполнения
                // очередной итерации цикла
                if (this.sema.fIner) {

                    this.scaner.setUK(metka1);
                    continue start;

                }

                this.currentLEX = this.scaner.next(l);

                if (this.currentLEX != Scaner._PARENTHESIS_CLOSE) {

                    this.scaner.PrintError("Ожидался символ )", l);
                    return false;

                }

                savePointer = this.scaner.getUK();
                this.currentLEX = this.scaner.next(l);
                this.scaner.setUK(savePointer);

                // Устанавливаем флаг в зависимости
                // от вычесленного значения
                this.sema.fIner = bodyFlag;

                if( this.currentLEX == Scaner._BRACE_OPEN && !this.sema.first){

                    this.sema.current = this.sema.getCurrent().getUp();

                }

                // Пятая зависимость от флага
                if ( !this.operator() ) {

                    return false;

                }

                // Мы находимся здесь: for(x=v(); a==b; a = a + 1){} <-
                // Если fIntr == true - возвращаемся к метке проверки условия
                // иначе продолжаем движение по коду

                if (bodyFlag) {

                    this.scaner.setUK(metka2);
                    // Меняем значение флага, чтобы повторно не создавать переменные в цикле
                    this.sema.createVariable = false;
                    this.sema.first = false;

                    continue increment;

                } else {

                    this.sema.fIner = before;
                    this.sema.first = test1;
                    break increment;
                }
            } while( true );

            break start;

        } while( true );

        return true;
    }

    private boolean assignment() {

        String operator;
        String operandOne;
        String operandTwo;

        ArrayList<Character> l = new ArrayList<>();
        Integer savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._ID) {

            this.scaner.PrintError("Ожидался идентификатор", l);
            return false;

        }

        String nameASSI;
        TData data1 = null;

        this.scaner.setUK(savePointer);

        // Затираем старое имя
        this.nameVariable = "";

        Tree variableTree = null;

        if ( (variableTree = this.name()) == null ) {

            return false;

        } else if( variableTree.node().getFlagConst() ){

            this.scaner.PrintNumStr("Попытка присвоить новое значение const", l);
            return false;

        }

        nameASSI = this.nameVariable;
        data1 = this.currentData;
        operandOne = nameASSI;

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._ASSIGN) {

            this.scaner.PrintError("Ожидался символ =", l);
            return false;

        }

        operator = "=";

        savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(savePointer);

        if (this.currentLEX != Scaner._PARENTHESIS_OPEN &&
                this.currentLEX != Scaner._TYPE_INT_10 &&
                this.currentLEX != Scaner._TYPE_FLOAT &&
                this.currentLEX != Scaner._ID) {

            this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
            return false;

        } else {

            Integer type2 = null;
            TData data2 =null;

            if ( !this.expression() ) {

                return false;

            }

            data2 = this.currentData;
            operandTwo = this.resultTriad;

            // Проверка data добавить
            if (this.sema.sovmestimie(data1, data2, l) == null){

                return false;

            }

            int assignmentTriadIndex  = this.triadBuilder.create(operator, operandOne, operandTwo);

            if( this.sema.fIner ){

                this.printMessage( data1, nameASSI );
                this.triadBuilder.print( assignmentTriadIndex );

            }

        }
        return true;
    }

    public boolean struct() {
        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._STRUCT) {
            this.scaner.PrintError("Ожидался символ struct", l);
            return false;
        }

        this.currentLEX = this.scaner.next(l);
        // Если прочитан идентификатор
        if (this.currentLEX != Scaner._ID) {
            this.scaner.PrintError("Ожидался символ идентификатор", l);
            return false;
        }

        /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
        // Добавили две пустые вершины - одну слева, другую справа от нее
        Tree addres = this.sema.sem3(l);

        this.currentLEX = this.scaner.next(l);
        // Если прочитана фигурная скобка
        if (this.currentLEX != Scaner._BRACE_OPEN) {
            this.scaner.PrintError("Ожидался другой символ {", l);
            return false;
        }

        Integer savePointer = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(savePointer);

        while (this.currentLEX == Scaner._INT || this.currentLEX == Scaner._FLOAT || this.currentLEX == Scaner._ID) {
            if (!this.data()) {
                return false;
            }
            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(savePointer);
        }

        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._BRACE_CLOSE) {
            this.scaner.PrintError("Ожидался другой символ }", l);
            return false;
        }

        /*********!!!ТЕСТИРУЕМЫЕ ОБЬЕКТ!!!******/
        this.sema.sem2(addres);


        this.currentLEX = this.scaner.next(l);
        if (this.currentLEX != Scaner._SEMICOLON) {
            this.scaner.PrintError("Ожидался другой символ ;", l);
            return false;
        }
        return true;
    }

    public boolean data() {
        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._INT && this.currentLEX != Scaner._FLOAT && this.currentLEX != Scaner._ID) {
            this.scaner.PrintError("Ожидался тип данных", l);
            return false;
        }

        // Определяем тип данных
        Integer type = this.sema.sem10(l);

        do {
            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._ID) {
                this.scaner.PrintError("Ожидался идентификатор ", l);
                return false;
            }

            // Проверка на дублирование. Занесение переменной в дерево.
            Tree tmp = this.sema.sem11(l, type);

            if( tmp == null ){
                return false;
            }

            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX == Scaner._ASSIGN) {
                Integer savePointer2 = this.scaner.getUK();
                this.currentLEX = this.scaner.next(l);

                if (this.currentLEX != Scaner._BRACE_OPEN &&
                        this.currentLEX != Scaner._PARENTHESIS_OPEN &&
                        this.currentLEX != Scaner._TYPE_INT_10 &&
                        this.currentLEX != Scaner._TYPE_FLOAT &&
                        this.currentLEX != Scaner._ID) {

                    this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( | {", l);
                    return false;

                } else if (this.currentLEX == Scaner._BRACE_OPEN) {

                    Integer savePointer = this.scaner.getUK();
                    this.currentLEX = this.scaner.next(l);
                    this.scaner.setUK(savePointer);

                    if (this.currentLEX == Scaner._BRACE_OPEN ||
                            this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                            this.currentLEX == Scaner._ID ||
                            this.currentLEX == Scaner._TYPE_INT_10 ||
                            this.currentLEX == Scaner._TYPE_FLOAT) {

                        if( !this.inside( tmp ) ){

                            return false;

                        }

//                        if (!this.inside( type )) {
//                            return false;
//                        }

                        this.currentLEX = this.scaner.next(l);

                    }
                    if (this.currentLEX != Scaner._BRACE_CLOSE) {
                        this.scaner.PrintError("Ожидался символ }", l);
                        return false;
                    }
                    this.currentLEX = this.scaner.next(l);
                } else {
                    this.scaner.setUK(savePointer2);

                    Integer typeExp = null;
                    TData dataExp = null;

                    if (!this.expression()) {
                        return false;
                    }

                    typeExp = this.currentType;
                    dataExp = this.currentData;

                    if( this.sema.createVariable ){

                        if ( this.sema.sovmestimie(tmp.node().data, dataExp, l) == null ){

                            return false;

                        }

                    }

                    this.currentLEX = this.scaner.next(l);
                }
            }
        } while (this.currentLEX == Scaner._COMMA);

        if (this.currentLEX != Scaner._SEMICOLON) {
            this.scaner.PrintError("Ожидался символ ;", l);
            return false;
        }
        return true;
    }

    public boolean expression() {

        String operatorTriad;
        String operandOne;
        String operandTwo;

        ArrayList<Character> l = new ArrayList<>();
        Integer saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        Integer type1 = null;
        TData data1 = null;

        if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                this.currentLEX == Scaner._TYPE_INT_10 ||
                this.currentLEX == Scaner._TYPE_FLOAT ||
                this.currentLEX == Scaner._ID) {

            if (!this.a2()) {
                return false;
            }

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

        } else {
            this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
            return false;
        }


        ArrayList<Character> operator = null;

        saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        while (this.currentLEX == Scaner._EQUALLY || this.currentLEX == Scaner._NOT_EQUALLY) {

            operator = new ArrayList<>(l);
            operatorTriad = Semantic.convertToString( operator );

            Integer type2 = null;
            TData data2 = null;

            this.currentLEX = this.scaner.next(l);
            Integer saveLEX2 = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX2);
            if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                    this.currentLEX == Scaner._TYPE_INT_10 ||
                    this.currentLEX == Scaner._TYPE_FLOAT ||
                    this.currentLEX == Scaner._ID) {
                if (!this.a2()) {
                    return false;
                }
                type2 = this.currentType;
                data2 = this.currentData;
                operandTwo = this.resultTriad;
            } else {
                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;
            }

            this.currentData = this.sema.sem23(data1, data2, operator);


            int indexExpressionTriad = this.triadBuilder.create(operatorTriad, operandOne, operandTwo);
            this.triadBuilder.print( indexExpressionTriad );
            this.resultTriad = String.valueOf( indexExpressionTriad ) + ")";

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

            saveLEX = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX);
        }

        return true;
    }

    private boolean a2() {

        String operatorTriad;
        String operandOne;
        String operandTwo;

        ArrayList<Character> l = new ArrayList<>();
        Integer saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        Integer type1 = null;
        TData data1 = null;

        if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                this.currentLEX == Scaner._TYPE_INT_10 ||
                this.currentLEX == Scaner._TYPE_FLOAT ||
                this.currentLEX == Scaner._ID) {
            if (!this.a3()) {
                return false;
            }

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

        } else {
            this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
            return false;
        }

        ArrayList<Character> operator = null;

        saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        while (this.currentLEX == Scaner._LESS || this.currentLEX == Scaner._GREAT ||
                this.currentLEX == Scaner._LESS_EQUALLY || this.currentLEX == Scaner._GREAT_EQUALLY) {

            operator = new ArrayList<>(l);
            operatorTriad= Semantic.convertToString( operator );

            Integer type2 = null;
            TData data2 = null;

            this.currentLEX = this.scaner.next(l);
            Integer saveLEX2 = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX2);

            if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                    this.currentLEX == Scaner._TYPE_INT_10 ||
                    this.currentLEX == Scaner._TYPE_FLOAT ||
                    this.currentLEX == Scaner._ID) {
                if (!this.a3()) {
                    return false;
                }
                type2 = this.currentType;
                data2 = this.currentData;
                operandTwo = this.resultTriad;
            } else {
                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;
            }

            //this.currentType = this.sema.sem23(type1, type2, operator);
            this.currentData = this.sema.sem23(data1, data2, operator);

            int indexA2Triad = this.triadBuilder.create(operatorTriad, operandOne, operandTwo);
            this.triadBuilder.print( indexA2Triad );
            this.resultTriad = String.valueOf( indexA2Triad ) + ")";

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

            // ??????????????????
            saveLEX = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX);
        }
        return true;
    }

    private boolean a3() {

        String operatorTriad;
        String operandOne;
        String operandTwo;

        ArrayList<Character> l = new ArrayList<>();
        Integer saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        Integer type1 = null;
        TData data1 = null;

        this.scaner.setUK(saveLEX);
        if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                this.currentLEX == Scaner._TYPE_INT_10 ||
                this.currentLEX == Scaner._TYPE_FLOAT ||
                this.currentLEX == Scaner._ID) {
            if (!this.a4()) {
                return false;
            }

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;


        } else {
            this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
            return false;
        }

        ArrayList<Character> operator = null;

        saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        while (this.currentLEX == Scaner._PLUS || this.currentLEX == Scaner._MINUS) {

            operator = new ArrayList<>(l);
            operatorTriad = Semantic.convertToString( operator );

            Integer type2 = null;
            TData data2 = null;

            this.currentLEX = this.scaner.next(l);
            Integer saveLEX2 = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX2);

            if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                    this.currentLEX == Scaner._TYPE_INT_10 ||
                    this.currentLEX == Scaner._TYPE_FLOAT ||
                    this.currentLEX == Scaner._ID) {
                if (!this.a4()) {
                    return false;
                }
                type2 = this.currentType;
                data2 = this.currentData;
                operandTwo = this.resultTriad;

            } else {
                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;
            }

            this.currentType = this.sema.sem23(type1, type2, operator);
            this.currentData = this.sema.sem23(data1, data2, operator);

            int indexA3Triad = this.triadBuilder.create(operatorTriad, operandOne, operandTwo);
            this.triadBuilder.print( indexA3Triad );
            this.resultTriad = String.valueOf( indexA3Triad ) + ")";

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

            // ??????????????????
            saveLEX = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX);
        }
        return true;
    }

    private boolean a4() {

        String operatorTriad;
        String operandOne;
        String operandTwo;

        ArrayList<Character> l = new ArrayList<>();
        Integer saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        Integer type1 = null;
        TData data1 = null;

        if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                this.currentLEX == Scaner._TYPE_INT_10 ||
                this.currentLEX == Scaner._TYPE_FLOAT ||
                this.currentLEX == Scaner._ID) {

            if (!this.a5()) {
                return false;
            }

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;

        } else {
            this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
            return false;
        }

        ArrayList<Character> operator = null;

        saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);
        this.scaner.setUK(saveLEX);

        while (this.currentLEX == Scaner._STAR ||
                this.currentLEX == Scaner._SLASH ||
                this.currentLEX == Scaner._PERCENT) {

            operator = new ArrayList<>(l);
            operatorTriad = Semantic.convertToString( operator );

            Integer type2 = null;
            TData data2 = null;

            this.currentLEX = this.scaner.next(l);
            Integer saveLEX2 = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX2);

            if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                    this.currentLEX == Scaner._TYPE_INT_10 ||
                    this.currentLEX == Scaner._TYPE_FLOAT ||
                    this.currentLEX == Scaner._ID) {
                if (!this.a5()) {
                    return false;
                }

                type2 = this.currentType;
                data2 = this.currentData;
                operandTwo = this.resultTriad;

            } else {
                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;
            }

            this.currentType = this.sema.sem23(type1, type2, operator);
            this.currentData = this.sema.sem23(data1, data2, operator);

            int indexA4Triad = this.triadBuilder.create(operatorTriad, operandOne, operandTwo);
            this.triadBuilder.print( indexA4Triad );
            this.resultTriad = String.valueOf( indexA4Triad ) + ")";

            type1 = this.currentType;
            data1 = this.currentData;
            operandOne = this.resultTriad;
            // ??????????????????
            saveLEX = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX);
        }
        return true;
    }

    private boolean a5() {
        ArrayList<Character> l = new ArrayList<>();
        Integer saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX == Scaner._TYPE_INT_10) {

            this.currentType = TableType.find("int");
            TDataValue value = new TDataValue();
            value.setDataAsInt(Integer.parseInt(Semantic.convertToString(l)));
            this.currentData = new TData(TableType.find("int"), value);

            this.resultTriad = Semantic.convertToString( l );
            return true;
        }

        if (this.currentLEX == Scaner._TYPE_FLOAT) {
            this.currentType = TableType.find("float");

            TDataValue value = new TDataValue();
            value.setDataAsFloat(Float.parseFloat(Semantic.convertToString(l)));
            this.currentData = new TData(TableType.find("float"), value);

            this.resultTriad = Semantic.convertToString( l );
            return true;
        }

        if (this.currentLEX == Scaner._PARENTHESIS_OPEN) {

            Integer saveLEX2 = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
            this.scaner.setUK(saveLEX2);

            if (this.currentLEX == Scaner._PARENTHESIS_OPEN ||
                    this.currentLEX == Scaner._TYPE_INT_10 ||
                    this.currentLEX == Scaner._TYPE_FLOAT ||
                    this.currentLEX == Scaner._ID) {

                if (!this.expression()) {

                    return false;

                }

            } else {

                this.scaner.PrintError("Ожидался тип TYPE_INT_10 | TYPE_FLOAT | ID | ( ", l);
                return false;

            }

            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._PARENTHESIS_CLOSE) {

                this.scaner.PrintError("Ожидался символ )", l);
                return false;

            }

            // Вернуть ссылку на триаду
        }

        if (this.currentLEX == Scaner._ID) {
            this.scaner.setUK(saveLEX);

            // this.resultTriad остается от name
            return this.name() != null;

        }

        return true;
    }

    private Tree name() {

        Integer saveLEX;
        Integer type1 = null;
        TData node1 = null;
        Tree tmp;

        ArrayList<Character> l = new ArrayList<>();
        this.currentLEX = this.scaner.next(l);

        if (this.currentLEX != Scaner._ID) {

            this.scaner.PrintError("Ожидался идентификатор", l);
            return null;

        }

        // Формируем имя переменной!!!
        this.nameVariable = Semantic.convertToString( l );

        // Проверяем, что id  находится в дереве и не является типом,
        // возвращаем ее тип type1. Поиск вверх.
        type1 = this.sema.sem24(l);
        tmp = this.sema.sem24Mod(l);

        if( tmp == null ){

            return null;

        }

        node1 = tmp.node().data;
        saveLEX = this.scaner.getUK();
        this.currentLEX = this.scaner.next(l);

        while (this.currentLEX == Scaner._POINT) {

            // Формируем имя
            this.nameVariable += (".");

            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._ID) {

                this.scaner.PrintError("Ожидался идентификатор", l);
                return null;

            }

            // Формируем имя
            this.nameVariable += ( Semantic.convertToString(l) );
            // Надо искать переменную в поддереве структуры вместо type1 надо искать обькт структуры
            tmp = this.sema.sem22Mod(l, tmp);

            if( tmp != null ) {
                node1 = tmp.node().data;

            } else {
                node1 = null;
            }

            // Проверяем, что l  находится в дереве и возвращаем ее тип type1. Поиск вниз.
            type1 = this.sema.sem22(l, type1);
            saveLEX = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
        }

        this.scaner.setUK(saveLEX);
        this.currentType = type1;
        this.currentData = node1;

        this.resultTriad = tmp.node().id();

        return tmp;
    }

    public boolean inside( Tree objectStruct ) {

        ArrayList<Character> l = new ArrayList<>();
        Integer savePointer;
        int index = 0;

        do {
            index++;
            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);

            if (this.currentLEX != Scaner._BRACE_OPEN &&
                    this.currentLEX != Scaner._PARENTHESIS_OPEN &&
                    this.currentLEX != Scaner._TYPE_INT_10 &&
                    this.currentLEX != Scaner._TYPE_FLOAT &&
                    this.currentLEX != Scaner._ID) {

                this.scaner.PrintError("Ожидался другой символ { | ( | TYPE_INT_10 | TYPE_FLOAT", l);
                return false;

            } else if (this.currentLEX == Scaner._BRACE_OPEN) {

                savePointer = this.scaner.getUK();
                this.currentLEX = this.scaner.next(l);

                this.scaner.setUK(savePointer);
                // Требует доработки
//                Integer type2 = this.sema.sem32(typeStruct, index, l);
//                TData data2 = this.sema.sem32(nameVariable, index, l);
                Tree structSubTree = this.sema.sem32(objectStruct, index, l);

                // Получае Node

                if (!this.inside( structSubTree ) ) {
                    return false;
                }

                this.currentLEX = this.scaner.next(l);
                if (this.currentLEX != Scaner._BRACE_CLOSE) {
                    this.scaner.PrintError("Ожидался символ }", l);
                    return false;
                }
            } else {
                this.scaner.setUK(savePointer);
                Integer type = null;
                TData data = null;

                if (!this.expression()) {
                    return false;
                }

                type = this.currentType;
                data = this.currentData;

                this.sema.sem31(index, data, objectStruct, l);
//                this.sema.sem31(index, data, typeStruct, l);
            }
            savePointer = this.scaner.getUK();
            this.currentLEX = this.scaner.next(l);
        } while (this.currentLEX == Scaner._COMMA);

        this.scaner.setUK(savePointer);
        return true;
    }

    public void printMessage( TData value, String name ){
        String mes;
        if(value == null ){
            System.out.println("Что-то не так!");
            return;
        }

        if( value.getType() == TableType.find("int")){

            System.out.println("Новое значение " + name + " = " + value.getValue().getDataAsInt());

        } else if ( value.getType() == TableType.find("float") ){

            System.out.println("Новое значение " + name + " = " + value.getValue().getDataAsFloat());

        } else if( value.getType() == TableType.find("bool" ) ){

            System.out.println("Новое значение " + name + " = " + value.getValue().getDataAsBool());
            //this.nameVariable

        }

    }

}


//    private boolean name() {
//
//        Integer saveLEX;
//        Integer type1 = null;
//        TData node1 = null;
//        Tree tmp;
//
//        ArrayList<Character> l = new ArrayList<>();
//        this.currentLEX = this.scaner.next(l);
//
//        if (this.currentLEX != Scaner._ID) {
//            this.scaner.PrintError("Ожидался идентификатор", l);
//            return false;
//        }
//
//        // Формируем имя переменной!!!
//        this.nameVariable = Semantic.convertToString( l );
//
//        // Проверяем, что id  находится в дереве и не является типом,
//        // возвращаем ее тип type1. Поиск вверх.
//        type1 = this.sema.sem24(l);
//
//
//        tmp = this.sema.sem24Mod(l);
////        tmp.node().data = new TData(tmp.node().getDataType(), new TDataValue());
//        if( tmp == null ){
//            return false;
//        }
//        node1 = tmp.node().data;
//
//        saveLEX = this.scaner.getUK();
//        this.currentLEX = this.scaner.next(l);
//
//        while (this.currentLEX == Scaner._POINT) {
//
//            // Формируем имя
//            this.nameVariable += (".");
//
//            this.currentLEX = this.scaner.next(l);
//
//            if (this.currentLEX != Scaner._ID) {
//                this.scaner.PrintError("Ожидался идентификатор", l);
//                return false;
//            }
//
//            // Формируем имя
//            this.nameVariable += ( Semantic.convertToString(l) );
//
//            // Надо искать переменную в поддереве структуры вместо type1 надо искать обькт структуры
//            tmp = this.sema.sem22Mod(l, tmp);
//
//            if( tmp != null ) {
//
//                node1 = tmp.node().data;
//
//            } else {
//
//                node1 = null;
//
//            }
//            // Проверяем, что l  находится в дереве и возвращаем ее тип type1. Поиск вниз.
//            type1 = this.sema.sem22(l, type1);
//
//            saveLEX = this.scaner.getUK();
//
//            this.currentLEX = this.scaner.next(l);
//        }
//
//        this.scaner.setUK(saveLEX);
//
//        this.currentType = type1;
//        this.currentData = node1;
//
//        return true;
//    }
