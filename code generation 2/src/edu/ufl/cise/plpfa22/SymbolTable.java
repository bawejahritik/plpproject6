package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.Declaration;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

//LeBlanc-Cook Symbol table
//Pseudocode
//1) More efficient than stack
//void enterScope(){
//    get nextScopeID
//        push scopeid onto scope stack
//        incremement nesting level
//        }
//
//void closeScope(){
//    remove top of scope stack
//        decrement nesting level
//        }
//
//void lookup(String name){
//    get matching entry in hash map
//        scan chain and return attributes for entry with scope no. closest to top of scope stack
//        }

public class SymbolTable {
    Stack<String> scopeStack;
    List<String> scopeList = new ArrayList<>();
    String scopeId;
    int temp;

    HashMap<String, ArrayList<Pair>> map;
    int currentLevel;

    private static AtomicLong idCounter = new AtomicLong();

    //generating random scopeID
    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }

    //suneet
    public SymbolTable(){
        this.scopeStack = new Stack<>();
        this.map = new HashMap<>();
        this.currentLevel = 0;
        this.scopeId = createID();
        scopeStack.push(scopeId);
    }

    //hritik
    public void enterScope(){
        scopeId = scopeList.get(temp++);
        scopeStack.add(scopeId);
        currentLevel+=1;
    }

    //suneet
    public void entry(){
        //System.out.println("1: " + scopeList);
        currentLevel++;
        scopeId = createID();
        scopeList.add(scopeId);
        scopeStack.push(scopeId);
        //System.out.println("2: " + scopeList);
    }
    //hritik
//    public void enterScope(Boolean pass){
//        System.out.println("1: " + scopeList);
//        if(pass == false) {
//            scopeId = createID();
//            scopeStack.push(scopeId);
//            scopeList.add(scopeId);
//        }
//        System.out.println("2: " + scopeList);
//
//        currentLevel+=1;
//    }

    //suneet
    public void closeScope(){
        scopeStack.pop();
        currentLevel -= 1;
        scopeId = scopeStack.peek();
    }

    //hritik
    public Declaration lookup(String name) throws PLPException {
        ArrayList<Pair> currentPair = map.get(name);
        if(currentPair == null){
            throw new ScopeException();
        }

        Declaration currentDeclaration = null;
        for(Pair pair : currentPair){
            for(int i=0; i<scopeStack.size(); i++){
                if(scopeStack.get(i) == pair.getscopeId()) currentDeclaration = pair.getDec();
            }
        }
        return currentDeclaration;
    }

    //suneet
    public void insert(String name, Declaration declaration) throws PLPException {
        //ArrayList<Pair> currentPair = new ArrayList<>();
        //System.out.println("getName: " + name + " "+map.get(name));
        if(map.containsKey(name)){
            ArrayList<Pair> currentPair = map.get(name);
            //System.out.println("map: " + currentPair.size());
            int i = 0;
            while(i < currentPair.size()){
                //System.out.println("i:" + i + " "+currentPair.get(i).getscopeId());
                //System.out.println("inwhile "+scopeId);
                if(currentPair.get(i).getscopeId() == scopeId){
                    //System.out.println("here");
                    //System.out.println(currentPair.get(i).getscopeId() + " " + scopeId);
                    throw new ScopeException();
                }
                i++;
            }
            currentPair.add(new Pair(scopeId, currentLevel, declaration));
            map.put(name, currentPair);
           // System.out.println("name: " + name + " map: " + map);
        }
        else {
            Pair pair = new Pair(scopeId, currentLevel, declaration);

            //System.out.println("else: "+scopeId);
            ArrayList<Pair> currentPair = new ArrayList<>();
            currentPair.add(pair);
            map.put(name, currentPair);
            //System.out.println("name: " + name + " map: " + map);
        }
    }

    //hritik
    public class Pair{
        String scopeId;
        Declaration dec;
        int currentScopeLevel;

        public Pair(String scopeId, int currentScopeLevel, Declaration dec){
            this.scopeId = scopeId;
            this.currentScopeLevel = currentScopeLevel;
            this.dec = dec;
        }

        public String getscopeId(){
            return scopeId;
        }

        public int getCurrentScopeLevel(){
            return  currentScopeLevel;
        }

        public Declaration getDec(){
            return dec;
        }
    }
}

