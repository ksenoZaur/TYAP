package com.company;

import java.util.ArrayList;

public class Semantic {

    // Fields
    private Tree root;
    public Tree current;
    private Scaner scanerPtr;

    public boolean fIner = true;
    public boolean first =true;
    public boolean createVariable;

    // Methods

    public Semantic( Scaner scanerPtr ){
        this.root = new Tree();
        this.current = this.root;
        this.scanerPtr = scanerPtr;
    }

    public Tree getCurrent(){

        return this.current;

    }

    // Просто добавить элемент слева, затем к нему элемент справа,
    // перейти к нему и вернуть адрес первого добавленного элемента.
    public Tree sem1(){

        if( createVariable ) {

            Tree leftPart = null;

            if( this.current.getLeft() != null ){
                leftPart = this.current.getLeft().getLeft();
            }

            this.current.setLeftPart(new Node(false), leftPart);

            this.current.getLeft().setUpPart( this.current );

            this.current = this.current.getLeft();

            // Вернуться должны к созданной слева вершине
            Tree returnAddress = this.current;
            this.current.setRightPart(new Node(true));
            this.current = this.current.getRight();
            return returnAddress;
        } else {

            this.current = this.current.getLeft();
            Tree returnAddress = this.current;
            this.current = this.current.getRight();
            return returnAddress;

        }


    }

    // Возвращаем указатель  на вершину, представляющую
    // вход в {}
    public void sem2( Tree oldAddress ){
        this.current = oldAddress;
    }

    // Заносим lex  в таблицу как тип, создаем правого потомка и меняем указатель.
    // В t записываем указатель на вершину в дереве, куда была помещена lex.
    public Tree sem3( ArrayList<Character> lex ){
        String lexNew = convertToString(lex);

        // Проверяем, может такой тип уже был обьявлен.
        Tree result;
        Tree from = this.current;

        do{
            result = this.current.findUp(from, lexNew);
            if( result != null ){
                if( result.node().getFlagType() ){
                    this.scanerPtr.PrintError("Повторное обьявление стуктуры",lex);
                    return this.current;
                }else{
                    // Продолжаем поиск, т.к. была найдена переменная
                    // с аналогичным идентификатором
                    from = from.getUp();
                }
            }
        }while(result != null );
        // Добавляем новый тип в таблицу
        TableType.addType( lexNew );
        // Создаем вершину в дереве
        this.current.setLeftPart( new Node(Node.typeStruct, null, lexNew, false, true),null);
        this.current = this.current.getLeft();
        // Создаем правого потомка
        Tree returnAddress = this.current;
        this.current.setRightPart( new Node( true ));
        this.current = this.current.getRight();
        // Возвращаем адрес возврата
        return returnAddress;
    }

    // Определяем тип переменной
    public Integer sem10( ArrayList<Character> lex ){
        String lexNew = convertToString(lex);
        // Поиск по таблице типов
        Integer type = TableType.find( lexNew );
        if( type == null ){
            this.scanerPtr.PrintError("Указан несуществующий тип", lex);
        }
        return type;
    }

    // Проверяем переменную на дублирование.
    // Заносим в таблицу вместе с типом t.
    public Tree sem11( ArrayList<Character> lex, Integer type){
        String lexNew = this.convertToString( lex );
        // Проверка на дублирование
        if( this.dupControl(this.current, lexNew, false) != null ){

            this.scanerPtr.PrintError("Повторное объявление переменной или константы", lex);
            return null;

        } else {

            if( this.createVariable ) {

                // Создаем новый узел
                // Чтоб хранить данные
                TData data = new TData(type, new TDataValue());
                Node variable = new Node(Node.typeVariable, type, lexNew, false, false);
                variable.data = data;

                // Добавляем слева
                this.current.setLeftPart(variable, null);
                // переходим в новый узел
                this.current = this.current.getLeft();

                // Кажись тут надо проверить если тип - структура, то делаем копию поддерева +
                // добавляем ссылку на него сюда
                if (type != TableType.find("int") &&
                        type != TableType.find("float") &&
                        type != TableType.find("bool")) {

                    // Получили id структуры
                    String id = TableType.getSting(type);

                    // Поиск структуры
                    Tree struct = null;
                    Tree from = this.current;

                    // Переходим к Tree структуры
                    do {
                        struct = this.current.findUp(from, id);
                        from = struct.getUp();
                    } while (!struct.node().getFlagType());

                    // Создаем копию поддерева
                    Tree currentTree = struct.getRight();
                    Tree newStruct = new Tree();
                    currentTree.copy(newStruct, this.current);
                    this.current.setRightPart(newStruct);
                }
            } else {

                this.current = this.current.getLeft();

            }
            return this.current;
        }
    }

    // Проверяем, что lex  находится в дереве и возвращаем ее тип. Поиск вниз.
    // Возвращаем TData скорей всего
    public Integer sem22 ( ArrayList<Character> lex, Integer type){
        if( type == null ){
            return null;
        }
        // Т.к. поиск идет вниз, проверяем что тип - структурный:
        if( type == TableType.find("int") || type == TableType.find("float")){
            this.scanerPtr.PrintError("Элемент не является структурой",lex);
            return null;
        }
        // По type найти id типа
        String id = TableType.getSting( type );
        // По id найти Tree структуры
        Tree from = this.current;
        Tree struct;
        // Поиск осуществляем, до тех пор, пока не найдем
        // структуру.
        do{
            struct = this.current.findUp(from, id);
            from = from.getUp();
        }while ( !struct.node().getFlagType());
        // Структура найдена.
        // Правым спуском найдем lex
        String idNext = this.convertToString( lex );
        Tree result = this.current.findRightLeft(struct, idNext);
        if( result == null ){
            this.scanerPtr.PrintError("Структура не содержит указанного поля", lex);
            return  null;
        }else {
            return result.node().getDataType();
        }
    }

    public  Tree sem22Mod ( ArrayList<Character> lex, Tree treeWithStructObject){

        if( treeWithStructObject == null ){
            return null;
        }

        // Т.к. поиск идет вниз, проверяем что тип - структурный:
        if( treeWithStructObject.node().getDataType().equals( TableType.find("int") )||
                treeWithStructObject.node().getDataType().equals( TableType.find("float"))){
            this.scanerPtr.PrintError("Элемент не является структурой",lex);
            return null;
        }
        // Структура найдена.
        // Правым спуском найдем lex
        String idNext = this.convertToString( lex );
        Tree result = this.current.findRightLeft(treeWithStructObject, idNext);

        if( result == null ){
            this.scanerPtr.PrintError("Структура не содержит указанного поля", lex);
            return  null;
        } else {
            return result;
        }
    }

    // Доделать!
    public Integer sem13(ArrayList<Character> lex, Integer type1, Integer type2){
        String lexNew = this.convertToString( lex );
        if( this.dupControl(this.current, lexNew, false) != null ){

            this.scanerPtr.PrintError("Повторной объявление переменной или константы", lex);
            return 0;

        }else{
            if( this.sovmestimie(type1, type2,lex) == null){
                this.scanerPtr.PrintError("Несовместимые типы");
                return 0;
            }
            this.current.setLeftPart( new Node(Node.typeConst, type1, lex.toString(), true,false ), null);
            this.current = this.current.getLeft();
            return type1;
        }
    }

    public TData sem13(ArrayList<Character> lex, TData data1, TData data2){
        String lexNew = this.convertToString( lex );

        if( this.dupControl(this.current, lexNew, false) != null ){

            this.scanerPtr.PrintError("Повторной объявление переменной или константы", lex);
            return null;

        }else{

            if( this.sovmestimie(data1, data2, lex) == null){

                this.scanerPtr.PrintError("Несовместимые типы");
                return null;

            }

            Node newNode = new Node(Node.typeConst, data1.getType(), lexNew, true, false);
            newNode.data = data1;

            this.current.setLeftPart(  newNode, null );

//            this.current.setLeftPart( new Node(Node.typeConst, type1, lex.toString(), true,false ));
            this.current = this.current.getLeft();
            return data1;
        }
    }


    public TData sem14( ArrayList<Character> lex ){

//        Integer result = sem10( lex );
        Integer type = sem10( lex );
        TData result = new TData();
        result.setType( type );
        result.setValue( new TDataValue() );

        if( type == 0 ){

            return null;

        }

        if( type != TableType.find("int") &&
                type != TableType.find("float") &&
                type != TableType.find("bool")){

            this.scanerPtr.PrintError("Структура не может быть объявленна как const", lex);
            return null;

        }

        return result;
    }

    // Проверка что тип bool
    public TData sem20( TData type, ArrayList<Character> lex){

        if( type == null ){
            this.scanerPtr.PrintNumStr("Требуется тип bool",lex);
            return null;
        }

        if( !type.getType().equals(TableType.find("bool")) ){

            this.scanerPtr.PrintNumStr("Требуется тип bool",lex);
            return null;

        }
        return type;
    }

    public Integer sem23(Integer type1, Integer type2, ArrayList<Character> operator){
        if( type1 == null || type2 == null ){
//            this.scanerPtr.PrintError("Невозможно применить оператор",operator);
            return null;
        }

        if( type1.equals( type2 )){
            if( type1 != TableType.find("int") &&  type1 != TableType.find("float") ||
                    type2 != TableType.find("int") &&  type2 != TableType.find("float") ){
                this.scanerPtr.PrintError("Невозможно применить оператор",operator);
                return null;
            }
            if( this.convertToString(operator).equals("==") || this.convertToString(operator).equals("!=")
            || this.convertToString(operator).equals("<") || this.convertToString(operator).equals(">") || this.convertToString(operator).equals("<=") ||
                    this.convertToString(operator).equals(">=")){
                return TableType.find("bool");
            }
            if( type1 == TableType.find("int")){
                if( this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                        this.convertToString(operator).equals("*") || this.convertToString(operator).equals("%")){
                    return TableType.find("int");
                }
                if( this.convertToString(operator).equals("/") ){
                    return TableType.find("float");
                }
            }
            if( type1 == TableType.find("float")){
                if( this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                        this.convertToString(operator).equals("*") ||
                        this.convertToString(operator).equals("/")){
                    return TableType.find("float");
                }
                if( this.convertToString(operator).equals("%")){
                    this.scanerPtr.PrintError("Невозможно применить оператор",operator);
                    return null;
                }
            }
        }
        if( this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                this.convertToString(operator).equals("*") || this.convertToString(operator).equals("/")){
            return TableType.find("float");
        }else {
            this.scanerPtr.PrintError("Невозможно применить оператор",operator);
            return null;
        }
    }

    // Проверяем, что id  находится в дереве и не является типом,
    // возвращаем ее тип type1. Поиск вверх.
    public Integer sem24(  ArrayList<Character> lex ){
        // Перевод идентификатора в строку
        String id = this.convertToString( lex );
        Tree result = null;
        // Поиск начинаем с текущей вершины
        Tree from = this.current;
        do {
             result = this.current.findUp(from, id);
             if( result == null ){
                 this.scanerPtr.PrintError("Переменная необъявлена",lex);
                 return null;
             }else if ( result.node().getFlagType() ) {
                 // Найдена вершина, которая является типом. Продолжаем поиск
                 from = from.getUp();
             }else {
                 return result.node().getDataType();
             }
        }while( true );
    }

    // Поиск вершины - переменной
    public Tree sem24Mod(  ArrayList<Character> lex ){
        // Перевод идентификатора в строку
        String id = this.convertToString( lex );
        Tree result = null;
        // Поиск начинаем с текущей вершины
        Tree from = this.current;
        do {
            result = this.current.findUp(from, id);
            if( result == null ){
                this.scanerPtr.PrintError("Переменная необъявлена",lex);
                return null;
            }else if ( result.node().getFlagType() ) {
                // Найдена вершина, которая является типом. Продолжаем поиск
                from = from.getUp();
            }else {
                return result;
            }
        }while( true );
    }

    // Приведение типа type2 к type1
    public Integer sovmestimie( Integer type1, Integer type2, ArrayList<Character> lex){
        if(type1 == null || type2 == null){
//            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
//                    TableType.getSting( type2 ) + "->" + TableType.getSting( type1 ), lex);
            return null;
        }
        if( type1.equals(type2)){
            return type1;
        }
        if( type1 == TableType.find("int")){
            if(type2 == TableType.find("int")){
                return TableType.find("int");
            }
            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                    TableType.getSting( type2 ) + "->" + TableType.getSting( type1 ), lex);
            return null;
        }
        if( type1 == TableType.find("float") ) {
            if(type2 == TableType.find("int") || type2 == TableType.find("float") ){
                return TableType.find("float");
            }
            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                    TableType.getSting( type2 ) + "->" + TableType.getSting( type1 ), lex);
            return null;
        }
            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                TableType.getSting( type2 ) + "->" + TableType.getSting( type1 ), lex);
        return null;
    }

    public Tree dupControl( Tree addr, String lex, Boolean type){
        return Tree.findUpOneLevel(addr, lex, type);
    }

    public static String convertToString( ArrayList<Character> lex ){
        String result ="";
        for( Character a : lex){
            result = result.concat(a.toString());
        }
        return result;
    }

    // Возвращаем Node index-ого потомка стуктуры
    public Integer sem32(Integer typeStruct, Integer index,ArrayList<Character> lex){
        // Передано пустое значение
        if( typeStruct == null ){
            return null;
        }

        // Если тип не принадлежит к типу структур
        if( typeStruct == TableType.find("int") ||
                typeStruct == TableType.find("float")){

            this.scanerPtr.PrintNumStr("Элемент не является структурой",lex);
            return null;

        }

        // Получили id структуры
        String id = TableType.getSting( typeStruct );

        // Нашли структуру
        Tree struct = null;
        Tree from = this.current;

        // Переходим к Tree структуры
        do {
            struct = this.current.findUp(from, id);
            from = struct.getUp();
//            if( struct == null ){
//                this.scanerPtr.PrintNumStr("Такого не может быть стр 249кода",lex);
//                return null;
//            }
        }while ( !struct.node().getFlagType() );

        // Находим index-ый параметр перемещаясь по правым потомкам
        Tree currentTree = struct.getRight();
        int i = 0;

        while( i < index ){
            currentTree = currentTree.getLeft();
            if( currentTree == null ){
                this.scanerPtr.PrintNumStr("Неверное число параметров",lex);
                return null;
            }
            i++;
        }
        // Вернуть тип index-ого потомка
        return currentTree.node().getDataType();
    }

    public Tree sem32(Tree treeNodeStruct, Integer index, ArrayList<Character> lex){

        if( treeNodeStruct == null ){
            return null;
        }

        // Если тип не принадлежит к типу структур
        if( treeNodeStruct.node().getDataType() == TableType.find("int") ||
                treeNodeStruct.node().getDataType() == TableType.find("float")){

            this.scanerPtr.PrintNumStr("Элемент не является структурой",lex);
            return null;

        }

        // Находим index-ый параметр перемещаясь по правым потомкам
        Tree currentTree = treeNodeStruct.getRight();
        int i = 0;

        while( i < index ){
            currentTree = currentTree.getLeft();
            if( currentTree == null ){
                this.scanerPtr.PrintNumStr("Неверное число параметров",lex);
                return null;
            }
            i++;
        }

        // Вернуть тип index-ого потомка
        return currentTree;
    }

    // Находим index-ый элемент, в структуре typeStruct, проверяем приводимость type к типу этого поля.
    // Нужно сделать присваивание
    public Integer sem31(Integer index, Integer type, Integer typeStruct,ArrayList<Character> lex ){

        if( typeStruct == null ){
            return null;
        }

        if( typeStruct == TableType.find("int") || typeStruct == TableType.find("float")){
            this.scanerPtr.PrintNumStr("Элемент не является структурой",lex);
            return null;
        }

        // Получили id структуры
        String id = TableType.getSting( typeStruct );

        // Нашли структуру
        Tree struct = null;
        Tree from = this.current;
        do {
            // Кажись id = имя вершины
            struct = this.current.findUp(from, id);
            from = struct.getUp();
//            if( struct == null ){
//                this.scanerPtr.PrintNumStr("Такого не может быть стр 249кода",lex);
//                return null;
//            }
            // это кажись не актуально или типа того(надо выяснить)
        }while ( !struct.node().getFlagType() );

        // Проходим по правым потомкам структуры
        Tree currentTree = struct.getRight();
        int i = 0;
        while( i < index ){

            currentTree = currentTree.getLeft();
            if( currentTree == null ){
                this.scanerPtr.PrintNumStr("Неверное число параметров",lex);
                return null;
            }
            i++;

        }
        // Дошли до index-ого
        // Получим его тип и сравним с типом type
        return this.sovmestimie(currentTree.node().getDataType(),type,lex);
    }


    public Integer sem666( ArrayList<Character> lex, Integer type ){
        String lexNew = this.convertToString( lex );
        // Проверка на дублирование
        Tree from = this.current;
        Tree currentTree = null;
        do{
            currentTree = this.current.findUp(from, lexNew);
            if( currentTree != null ){
                if( currentTree.node().getTypeObject() == Node.typeFunc ){
                    this.scanerPtr.PrintError("Повторное объявление функции", lex);
                    return null;
                } else {
                    from = from.getUp();
                }
            }
        }while(from != null && currentTree != null );
        // Создаем новый узел
        Node variable = new Node(Node.typeFunc, type, lexNew, false, false);
        // Добавляем слева
        this.current.setLeftPart( variable, null );
        // переходим в новый узел
        this.current = this.current.getLeft();
        return type;
    }

    // Встраиваем сюда вычисление
    public TData sem23(TData data1, TData data2, ArrayList<Character> operator) {

        if( data1.getType() == null || data2.getType() == null ){

            return null;

        }

        // Типы аргументов совпадают
        if( data1.getType().equals( data2.getType() )){

            if( data1.getType() != TableType.find("int") &&  data1.getType() != TableType.find("float") &&
                    data1.getType() != TableType.find("bool") ||
                    data2.getType() != TableType.find("int") &&  data2.getType() != TableType.find("float")
                    &&  data1.getType() != TableType.find("bool")){

                this.scanerPtr.PrintError("Невозможно применить оператор",operator);
                return null;

            }

            if( this.convertToString(operator).equals("==") ||
                    this.convertToString(operator).equals("!=")||
                    this.convertToString(operator).equals("<") ||
                    this.convertToString(operator).equals(">") ||
                    this.convertToString(operator).equals("<=") ||
                    this.convertToString(operator).equals(">=")){

                TDataValue resultValue = new TDataValue();

                if( data1.getType().equals( TableType.find("int" )) ){

                    if( this.convertToString(operator).equals("==") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() == data2.getValue().getDataAsInt());

                    } else if( this.convertToString(operator).equals("!=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() != data2.getValue().getDataAsInt());

                    } else if( this.convertToString(operator).equals("<") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() < data2.getValue().getDataAsInt());

                    } else if( this.convertToString(operator).equals(">") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() > data2.getValue().getDataAsInt());

                    } else if( this.convertToString(operator).equals("<=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() <= data2.getValue().getDataAsInt());

                    } else if( this.convertToString(operator).equals(">=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsInt() >= data2.getValue().getDataAsInt());

                    }

                    TData result = new TData(TableType.find("bool"), resultValue);
                    return result;
                }

                if( data1.getType().equals( TableType.find("float" )) ){

                    if( this.convertToString(operator).equals("==") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() == data2.getValue().getDataAsFloat());

                    } else if( this.convertToString(operator).equals("!=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() != data2.getValue().getDataAsFloat());

                    } else if( this.convertToString(operator).equals("<") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() < data2.getValue().getDataAsFloat());

                    } else if( this.convertToString(operator).equals(">") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() > data2.getValue().getDataAsFloat());

                    } else if( this.convertToString(operator).equals("<=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() <= data2.getValue().getDataAsFloat());

                    } else if( this.convertToString(operator).equals(">=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsFloat() >= data2.getValue().getDataAsFloat());

                    }

                    TData result = new TData(TableType.find("bool"), resultValue);
                    return result;
                }

                if( data1.getType().equals( TableType.find("bool" )) ){

                    if( this.convertToString(operator).equals("==") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsBool() == data2.getValue().getDataAsBool());

                    } else if( this.convertToString(operator).equals("!=") ) {

                        resultValue.setDataAsBool(
                                data1.getValue().getDataAsBool() != data2.getValue().getDataAsBool());

                    } else {

                        this.scanerPtr.PrintError("Невозможно применить оператор(недопустимые операцие над bool)", operator);
                        return null;

                    }

                    TData result = new TData(TableType.find("bool"), resultValue);
                    return result;
                }
            }

            if( data1.getType() == TableType.find("int")){
                if( this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                        this.convertToString(operator).equals("*") || this.convertToString(operator).equals("%") ||
                        this.convertToString(operator).equals("/")){

                    TDataValue resultValue = new TDataValue();

                    if( this.convertToString(operator).equals("+") ){
                        resultValue.setDataAsInt(data1.getValue().getDataAsInt() +
                                data2.getValue().getDataAsInt());
                    }

                    if( this.convertToString(operator).equals("-") ){
                        resultValue.setDataAsInt(data1.getValue().getDataAsInt() -
                                data2.getValue().getDataAsInt());
                    }

                    if( this.convertToString(operator).equals("*") ){
                        resultValue.setDataAsInt(data1.getValue().getDataAsInt() *
                                data2.getValue().getDataAsInt());
                    }

                    if( this.convertToString(operator).equals("%") ){
                        resultValue.setDataAsInt(data1.getValue().getDataAsInt() %
                                data2.getValue().getDataAsInt());
                    }

                    if( this.convertToString(operator).equals("/") ){
                        resultValue.setDataAsInt(data1.getValue().getDataAsInt() /
                                data2.getValue().getDataAsInt());
                    }

                    TData result = new TData(TableType.find("int"), resultValue);
                    return result;

                    // Сравниваем int и float
                } else if( this.convertToString(operator).equals("==") ||
                        this.convertToString(operator).equals("!=")||
                        this.convertToString(operator).equals("<") ||
                        this.convertToString(operator).equals(">") ||
                        this.convertToString(operator).equals("<=") ||
                        this.convertToString(operator).equals(">=")){


                }
            }

            if( data1.getType() == TableType.find("float")){
                if( this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                        this.convertToString(operator).equals("*") ||
                        this.convertToString(operator).equals("/")){

                    TDataValue resultValue = new TDataValue();

                    if( this.convertToString(operator).equals("+") ){
                        resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() +
                                data2.getValue().getDataAsFloat());
                    }

                    if( this.convertToString(operator).equals("-") ){
                        resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() -
                                data2.getValue().getDataAsFloat());
                    }

                    if( this.convertToString(operator).equals("*") ){
                        resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() *
                                data2.getValue().getDataAsFloat());
                    }

                    if( this.convertToString(operator).equals("/") ){
                        resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() /
                                data2.getValue().getDataAsFloat());
                    }

                    TData result = new TData(TableType.find("float"), resultValue);
                    return result;
                }
                if( this.convertToString(operator).equals("%")){
                    this.scanerPtr.PrintError("Невозможно применить оператор",operator);
                    return null;
                }
            }
        }

        // Типы аргементов не совпадают
        if (this.convertToString(operator).equals("+") || this.convertToString(operator).equals("-") ||
                this.convertToString(operator).equals("*") || this.convertToString(operator).equals("/")) {

            if( data1.getType().equals( TableType.find("bool")) ||
                    data2.getType().equals( TableType.find("bool"))){

                this.scanerPtr.PrintError("Невозможно применить оператор(недопустимые операцие над bool)", operator);
                return null;

            }

            TDataValue resultValue = new TDataValue();

            if(data1.getType().equals( TableType.find("float"))){

                if( this.convertToString(operator).equals("+") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() +
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("-") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() -
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("*") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() *
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("/") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsFloat() /
                            data2.getValue().getDataAsInt());
                }

            } else {

                if( this.convertToString(operator).equals("+") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsInt() +
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("-") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsInt() -
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("*") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsInt() *
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("/") ){
                    resultValue.setDataAsFloat(data1.getValue().getDataAsInt() /
                            data2.getValue().getDataAsFloat());
                }

            }

            TData result = new TData(TableType.find("float"), resultValue);
            return result;

        } else if( this.convertToString(operator).equals("==") ||
                this.convertToString(operator).equals("!=")||
                this.convertToString(operator).equals("<") ||
                this.convertToString(operator).equals(">") ||
                this.convertToString(operator).equals("<=") ||
                this.convertToString(operator).equals(">=" ) ){

            if( data1.getType().equals( TableType.find("bool")) ||
                    data2.getType().equals( TableType.find("bool"))) {

                this.scanerPtr.PrintError("Невозможно применить оператор(несовместимые типы)", operator);
                return null;

            }

            TDataValue resultValue = new TDataValue();

            if( data1.getType().equals( TableType.find("float")) ) {

                if( this.convertToString(operator).equals("==") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() ==
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("!=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() !=
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("<") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() <
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals(">") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() >
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals("<=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() <=
                            data2.getValue().getDataAsInt());
                }

                if( this.convertToString(operator).equals(">=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsFloat() >=
                            data2.getValue().getDataAsInt());
                }

            } else {

                if( this.convertToString(operator).equals("==") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() ==
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("!=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() !=
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("<") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() <
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals(">") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() >
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals("<=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() <=
                            data2.getValue().getDataAsFloat());
                }

                if( this.convertToString(operator).equals(">=") ){
                    resultValue.setDataAsBool(data1.getValue().getDataAsInt() >=
                            data2.getValue().getDataAsFloat());
                }

            }

            TData result = new TData(TableType.find("bool"), resultValue);
            return result;

        } else {
            this.scanerPtr.PrintError("Невозможно применить оператор", operator);
            return null;
        }
    }

    // Присвоить значени переменной
    public TData sovmestimie(TData data, TData dataExp, ArrayList<Character> l) {


        if(data== null || dataExp== null){
            return null;
        }

        //data.setType( data.getType());
        TDataValue value = new TDataValue();

        // Присваивание
        if( data.getType().equals(dataExp.getType())){

            if( this.fIner ) {
                if (data.getType().equals(TableType.find("int"))) {

                    value.setDataAsInt(dataExp.getValue().getDataAsInt());

                } else if (data.getType().equals(TableType.find("float"))) {

                    value.setDataAsFloat(dataExp.getValue().getDataAsFloat());

                } else if (data.getType().equals(TableType.find("bool"))) {

                    value.setDataAsBool(dataExp.getValue().getDataAsBool());

                }

                data.setValue(value);
            }

            return data;
        }

        // Присваиваем float или bool целочисленной переменной - низя!
        if( data.getType().equals( TableType.find("int")) ){

            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                    TableType.getSting( data.getType() ) + "->" + TableType.getSting( dataExp.getType() ), l);
            return null;
        }

        // Присваиваем int или bool float-у
        if( data.getType() == TableType.find("float") ) {
            if(dataExp.getType() == TableType.find("int") || dataExp.getType() == TableType.find("float") ){
                // Вычисляем

                if( this.fIner ) {

                    if (dataExp.getType().equals(TableType.find("int"))) {

                        value.setDataAsFloat(dataExp.getValue().getDataAsInt());

                    } else if (dataExp.getType().equals(TableType.find("float"))) {

                        value.setDataAsFloat(dataExp.getValue().getDataAsFloat());

                    }
                    data.setValue(value);
                }

                return data;
            }

            this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                    TableType.getSting( dataExp.getType() ) + "->" + TableType.getSting( data.getType() ), l);
            return null;
        }

        this.scanerPtr.PrintNumStr("Невозможно выполнить приведение типов " +
                TableType.getSting( dataExp.getType() ) + "->" + TableType.getSting( data.getType() ), l);
        return null;
    }

    public TData sem31(Integer index, TData data, Tree objectStruct, ArrayList<Character> lex ){

        if( objectStruct.node().getDataType() == null ){
            return null;
        }

        if( objectStruct.node().getDataType() == TableType.find("int") ||
                objectStruct.node().getDataType() == TableType.find("float")){

            this.scanerPtr.PrintNumStr("Элемент не является структурой",lex);
            return null;

        }

        Tree currentTree = objectStruct.getRight();
        int i = 0;
        while( i < index ){

            currentTree = currentTree.getLeft();
            if( currentTree == null ){
                this.scanerPtr.PrintNumStr("Неверное число параметров",lex);
                return null;
            }
            i++;

        }
        // Дошли до index-ого
        // Запихиваем туда значение
        return this.sovmestimie(currentTree.node().data, data,lex);
    }

}
