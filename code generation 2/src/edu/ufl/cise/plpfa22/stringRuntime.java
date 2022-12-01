package edu.ufl.cise.plpfa22;

import java.util.Objects;

public class stringRuntime {

    public static int a=0;
    public static int b=0;
    public static int c=0;
    public int temp=0;

    //greaterorequalto
    public static boolean ge(String arg1, String arg2){
        return arg1.endsWith(arg2);
    }
    
    //lessorequalto
    public static boolean le(String arg1, String arg2){
        return arg2.startsWith(arg1);
    }

    //greater
    public static boolean gt(String arg1, String arg2){
        return arg1.endsWith(arg2) && !arg1.equals(arg2);
    }

    public static boolean not(boolean arg) {
        return !arg;
    }

    //lessthen
    public static boolean lt(int arg1 , int arg2) {
        return arg1 < arg2;
    }

    //greaterthen
    public static boolean gt(int arg1 , int arg2) {return arg1 > arg2;}

    //equal
    public static boolean eq(int arg1 , int arg2) {
        return arg1 == arg2;
    }

    //equal
    public static boolean eq(boolean arg1 , boolean arg2) {
        return arg1 == arg2;
    }

    //lessthen
    public static boolean lt(boolean arg1 , boolean arg2) {
        return !arg1 && arg2;
    }

    //greaterthen
    public static boolean gt(boolean arg1 , boolean arg2) {
        return arg1 && !arg2;
    }
    
    //lessthen
    public static boolean lt(String arg1, String arg2){
        if(Objects.equals(arg1, arg2)){
            return false;
        }
        return arg2.startsWith(arg1);
    }
    
}