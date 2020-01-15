package com.company;

import com.company.utilities.getopts.Args;
import com.company.utilities.getopts.ArgsException;

/**
 * 运行命令： -l -p 123 -d abc
 * 输出结果： true:123:abc
 */
public class Main {

    public static void main(String[] args) {
        try{
            Args arg = new Args("l, p#, d*", args);
            boolean logging = arg.getBoolean('l');
            int port = arg.getInt('p');
            String directory = arg.getString('d');
            System.out.println(logging + ":" + port + ":" + directory);
        }catch (ArgsException e){
            System.out.printf("ArgumentError:%s\n", e.errorMessage());
        }
    }
}
