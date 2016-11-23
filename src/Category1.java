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


public class Category1 {

	public static HashMap<String,String> patternMap = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		loadPattern("dat/问题类别模式.txt");
		processCluster("dat/stock_cluster_result.txt", "dat/stock_cluster_category.txt");
	}
	
	public static void loadPattern(String strFile)
			throws Exception {
		File file = new File(strFile);

		if (file.isFile() && file.exists()) {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file), "GBK");
			BufferedReader br = new BufferedReader(read);
			String line = null;
			while ((line = br.readLine()) != null) {
				
				line = line.trim();
				if (!line.isEmpty()){
					int index = line.indexOf(" ");
					String key = line.substring(0, index);  //空格前内容作为类别
					String value = line.substring(index+1).trim(); //空格后内容作为类别值
					value = value.replaceAll("\\+", "");  //去除加号
					value = value.replaceAll("\\*", ".*"); //将*替换为.*

                    //添加以处理 W1|W2*W3|W4的输入
                    String[] vauleArray=value.split("\\s+");
                    StringBuffer sb=new StringBuffer();
                    for (int i=0;i<vauleArray.length;i++){
                        String[] phaseArrary=vauleArray[i].split(".\\*");
                        for (int j=0;j<phaseArrary.length;j++) {
                            String s = phaseArrary[j];

                            if (s.contains("|"))
                                s = "(" + s + ")";
                            if (j != phaseArrary.length - 1)
                                s += ".*";
                            System.out.println(s);
                            sb.append(s);
                        }
                        if(i!=vauleArray.length-1)
                            sb.append("|");
                    }
                    if (sb.length()>0) {
                        value = sb.toString();
                        System.out.println(value);
                    }
                    if(patternMap.containsKey(key))
                        value = patternMap.get(key)+ "|" + value;
                    patternMap.put(key, value);
                }
            }
			br.close();
			System.out.print(patternMap);
		}
		else {
			System.out.println("找不到指定的文件");
		}
	}

	public static void processCluster(String inFile, String outFile)
			throws Exception {
		HashSet<String> resultSet = new HashSet<>();
		ArrayList<String> rList = new ArrayList<>();
		File fileIn = new File(inFile);
		File fileOut = new File(outFile);
		fileOut.createNewFile();
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
		BufferedWriter bw = new BufferedWriter(osw); 
		
		if (fileIn.isFile() && fileIn.exists()) {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					fileIn), "GBK");
			BufferedReader br = new BufferedReader(read);
			String line = null;
			
			while ((line = br.readLine()) != null) {
				if(!line.isEmpty()&&!line.startsWith("Cluster")){
					String result = type(line).trim();
					//if(!resultSet.contains(result)){
						rList.add(result);
					//	resultSet.add(result);
					//}
					continue;
				}
				rList.add(line);
			}
			
			for(String e : rList)
				bw.write(e + "\n");
		/*	for(String e : rList)
				if(e.startsWith("无类别") && e.length() > 20){
					bw.write(e + "\n");
				}*/
			
			br.close();
			bw.close();
		}
		else {
			System.out.println("找不到指定的文件");
		}
	}
	
	private static String type(String line) {
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String regex = entry.getValue();
			if (Pattern.compile(regex).matcher(line).find()){
				if(sb.indexOf(key) == -1)
					sb.append(key + "|");
			}
		}
		String type= "无类别";	
		if (sb.length() != 0)
			type = sb.substring(0,sb.length() - 1);	
		return type + "\t" + line;
	}
	
}
