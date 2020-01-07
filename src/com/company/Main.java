package com.company;

import com.company.utilities.args.Args;
import com.company.utilities.args.ArgsException;

public class Main {

    public static void main(String[] args) {
        try{
            Args arg = new Args("l, p#, d*", args);
            boolean logging = arg.getBoolean('l');
            int port = arg.getInt('p');
            String directory = arg.getString('d');
        }catch (ArgsException e){
            System.out.printf("ArgumentError:%s\n", e.errorMessage());
        }
    }
}
