/**
 * Created by haowei on 16-11-23.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;


public class EXP {
    public static void prase_the_file(String strFile)
    throws Exception{
        File file=new File(strFile);
        if (file.isFile()&&file.exists()){
            InputStreamReader read=new InputStreamReader(
              new FileInputStream(file),"GBK"
            );
            BufferedReader br=new BufferedReader(read);
            String line=null;
            while((line =br.readLine()) !=null ){
                line=line.trim();
                if(!line.isEmpty()){
                    String [] lineArray=line.split("\\s+");
                    //判断是否四个列都齐全
                    if(lineArray.length<4){
                        System.out.println("not full ");
                        continue;
                    }

                }
            }
        }
        else{
            System.out.print("找不到要读入的四列文件");
        }
    }


    public static void main(String[] args)
    {
        
    }
}

