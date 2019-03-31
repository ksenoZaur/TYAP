package com.company;

import java.util.HashMap;

class TableType{
    private static int typeIndex;
    private static HashMap<String, Integer> typeTable;
    static {
        typeIndex = 4;
        typeTable = new HashMap<>();
        typeTable.put("int",1);
        typeTable.put("float",2);
        typeTable.put("bool",3);
    }


    public static String getSting( Integer index){
        for( String value : typeTable.keySet()){
            if( index == typeTable.get(value)){
                return value;
            }
        }
        return null;
    }

    public static void addType( String lex ){
        typeTable.put(lex, typeIndex++);
    }

    public static Integer find( String lex){
        return typeTable.get( lex );
    }
}

// Вершина дерева / запись в таблице
public class Node {
    // Const
    public static final int typeVariable = 1;
    public static final int typeStruct = 2;
    public static final int typeConst = 3;
    public static final int typeFunc = 4;


    // Fields
    private Integer typeObject;
    private Integer dataType;
    private String id;
    private Boolean flagConst;
    private Boolean flagType;
    private Boolean newLevelStart;

    TData data;

//    public static Enum<String>
    // количество полей в структуре

    public Boolean getNewLevelStart() {
        return newLevelStart;
    }

    private Integer numberOfFields;
    // типы полей в структуре
    private Integer[] typeOfField;

    // необязательные данные
    private Boolean flagInit;

    // Methods
    public Node( Boolean newLevelStart ){
        this.typeObject = null;
        this.dataType = null;
        this.id = null;
        this.flagConst = null;
        this.flagType = null;
        this.numberOfFields = null;
        this.typeOfField = null;
        this.flagInit = null;
        this.newLevelStart = newLevelStart;
    }



    public Node(Integer typeObject, Integer dataType, String id, Boolean flagConst, Boolean flagType){
        this.typeObject = typeObject;
        this.dataType = dataType;
        this.id = id;
        this.flagConst = flagConst;
        this.flagType = flagType;
        this.numberOfFields = 0;
        this.typeOfField = null;
        this.flagInit = false;
        this.newLevelStart = false;
    }

    public String id(){
        return this.id;
    }

    public Integer getTypeObject(){
        return this.typeObject;
    }

    public Integer getDataType() {
        return dataType;
    }

    public Boolean getFlagConst() {
        return flagConst;
    }

    public Boolean getFlagType() {
        return flagType;
    }

    public Integer getNumberOfFields() {
        return numberOfFields;
    }

    public Integer[] getTypeOfField() {
        return typeOfField;
    }

    public Boolean getFlagInit() {
        return flagInit;
    }

    public void setTypeObject(Integer typeObject) {
        this.typeObject = typeObject;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFlagConst(Boolean flagConst) {
        this.flagConst = flagConst;
    }

    public void setFlagType(Boolean flagType) {
        this.flagType = flagType;
    }

    public void setNumberOfFields(Integer numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public void setTypeOfField(Integer[] typeOfField) {
        this.typeOfField = typeOfField;
    }

    public void setFlagInit(Boolean flagInit) {
        this.flagInit = flagInit;
    }

    // Копируем элемент дерева
    public void copy(Node newNode){
//        TData data;
        newNode.typeObject = this.typeObject;
        newNode.dataType = this.dataType;
        newNode.id = this.id;
        newNode.flagConst = this.flagConst;
        newNode.flagType = this.flagType;
        newNode.newLevelStart = this.newLevelStart;
        if( this.data != null ) {
            newNode.data = new TData(1, new TDataValue());
            // Копируем данные
            this.data.copy(newNode.data);
        }
    }
}

// Значение одного элемента данных
class TDataValue{
    // Fields
    private int dataAsInt;          // Целое значение
    private float dataAsFloat;      // Число с фиксированной точкой
    private boolean dataAsBool;     // Логическое значение

    // Methods
    public boolean getDataAsBool() {
        return dataAsBool;
    }

    public void setDataAsBool(boolean dataAsBool) {
        this.dataAsBool = dataAsBool;
    }

    public int getDataAsInt() {
        return dataAsInt;
    }

    public void setDataAsInt(int dataAsInt) {
        this.dataAsInt = dataAsInt;
    }

    public float getDataAsFloat() {
        return dataAsFloat;
    }

    public void setDataAsFloat(float dataAsFloat) {
        this.dataAsFloat = dataAsFloat;
    }

}

class TData{
    private Integer type;
    private TDataValue value;

    // Копируем обьект с данными
    public void copy(TData newData){
        newData.type = this.type;
        newData.value.setDataAsFloat( this.value.getDataAsFloat() );
        newData.value.setDataAsInt( this.value.getDataAsInt() );
    }

    public TData(Integer type, TDataValue value) {
        this.type = type;
        this.value = value;
    }

    public TData(){}

    public Integer getType() {
        return this.type;
    }

    public TDataValue getValue(){
        return this.value;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setValue(TDataValue value) {
        this.value = value;
    }
}


