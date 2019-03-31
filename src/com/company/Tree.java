package com.company;

public class Tree {
    // Fields
    private Node node;
    private Tree leftPart;
    private Tree rightPart;
    private Tree upPart;

    // Methods
    public Tree( Tree leftPart, Tree rightPart, Tree upPart, Node data){
        this.node = data;
        this.leftPart = leftPart;
        this.rightPart = rightPart;
        this.upPart = upPart;
    }

    public Tree(){
        this.node = new Node( true );
        this.leftPart = null;
        this.rightPart = null;
        this.upPart = null;
    }

    // Создаем левого потомка текущей вершины
    public void setLeftPart( Node data, Tree leftPart){
        Tree leftChildren = new Tree(leftPart, null, this, data);
        this.leftPart = leftChildren;
    }

    public void setUpPart(Tree upPart) {
        this.upPart = upPart;
    }

    // Создаем правого потомка текущей вершины
    public void setRightPart( Node data){
        Tree rightChildren = new Tree(null, null,this, data);
        this.rightPart = rightChildren;
    }

    public void setRightPart( Tree rightPart){
        this.rightPart = rightPart;
    }

    // Поиск в дереве от текущей вершины
    public Tree findUp( String id ){
        return findUp(this, id);
    }

    // Поиск в дереве от вершины from
    public Tree findUp( Tree from, String id){
        Tree currentTree = from;
        while( currentTree != null ){

            if( currentTree.node.id() == null ){

                currentTree = currentTree.upPart;
                continue;

            }

            if( id.compareTo( currentTree.node.id()) != 0 ) {

                currentTree = currentTree.upPart;

            }else{

                return currentTree;

            }
        }
        return currentTree;
    }

    // Поиск прямых потомков заданной вершины
    public Tree findRightLeft( Tree from, String id){
        Tree currentTree = from.rightPart;
        while( currentTree != null ){
            if( currentTree.node.id() != null ) {
                if (id.compareTo(currentTree.node.id()) != 0) {
                    currentTree = currentTree.leftPart;
                } else {
                    return currentTree;
                }
            }else{
                currentTree = currentTree.leftPart;
            }
        }
        return currentTree;
    }

    public Node node(){
        return this.node;
    }

    public Tree getLeft(){
        return this.leftPart;
    }

    public Tree getRight(){
        return this.rightPart;
    }

    public Tree getUp(){ return  this.upPart; }

    static public Tree findUpOneLevel(Tree from, String id, Boolean type){
        Tree currentTree = from;
        while( currentTree != null ){
            if( !currentTree.node.getNewLevelStart() ){
                if( currentTree.node.id() != null ){
                    if( id.compareTo( currentTree.node.id())!= 0 || currentTree.node.getFlagType() != type){
                        currentTree = currentTree.upPart;
                    }else{
                        return currentTree;
                    }
                } else {
                    currentTree = currentTree.upPart;
                }
            }else{
                return null;
            }
        }
        return null;
    }

    public void print(){

    }

    public void copy(Tree newTree, Tree father){

        this.node.copy( newTree.node );
        if( this.leftPart != null ){
            newTree.leftPart = new Tree();
            this.leftPart.copy( newTree.leftPart, newTree);
        }

        if( this.rightPart != null ){
            newTree.rightPart = new Tree();
            this.rightPart.copy( newTree.rightPart, newTree);
        }

        if( this.upPart != null ){
            newTree.upPart = father;
        }

    }
}
