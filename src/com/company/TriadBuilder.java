package com.company;

import java.util.ArrayList;

public class TriadBuilder {

    private ArrayList<String[]> triad;

    public TriadBuilder() {

        this.triad = new ArrayList<>();

    }

    public ArrayList<String[]> getTriad() {
        return triad;
    }

    public int create() {

        String[] triad = new String[ 3 ];
        this.triad.add( triad );
        return this.triad.size() - 1;

    }

    public int create(String operator, String operadOne, String operandTwo) {

        String[] triad = new String[]{operator, operadOne, operandTwo};
        this.triad.add( triad );
        return this.triad.size() - 1;

    }

    public void addOperator( int index, String operator ){

        this.triad.get( index )[ 0 ] = operator;
    }

    public void addOperandOne( int index, String operand ){

        this.triad.get( index )[ 1 ] = operand;
    }

    public void addOperandTwo( int index, String operand ){

        this.triad.get( index )[ 2 ] = operand;
    }

    public void print( int index ){
        System.out.println( String.valueOf( index ) + ")" + this.triad.get( index )[ 0 ] + " "
                + this.triad.get( index )[ 1 ] + " " +
                this.triad.get( index )[ 2 ] + " ");
    }
}
