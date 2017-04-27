import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import EdgeDetector.ExtractRelation;
import NodeDetector.NodeDetection;
import Postprocessing.Processer;

public class Main_RE {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String InputTextFile = "D:/JUN/MCMT/drug_disease_RE/REpaper_precision/final_golddata.txt";
		String AnnFileFolder = "D:/JUN/MCMT/drug_disease_RE/REpaper_precision/workspace/annotated/";
		String RelationResultOutputPath = "D:/JUN/MCMT/drug_disease_RE/REpaper_precision/workspace/Output/Final_results.txt";
		String TriggerDictionaryPath = "D:/JUN/MCMT/drug_disease_RE/dictionary/newCondition/renewalRelation_type.txt";
		String stopWordPath = "D:/JUN/MCMT/drug_disease_RE/dictionary/newCondition/stopword.txt";
		
		
		NodeDetection node_detector = new NodeDetection();
		ExtractRelation relation = new ExtractRelation();
		Processer classifier = new Processer();
		
		System.out.println("+++++Start the Node Detector+++++");
		node_detector.NodeDectector(TriggerDictionaryPath, InputTextFile, AnnFileFolder);
		System.out.println("+++++Finish the Node Detector+++++");
		
		
		System.out.println("-----Start the Relation Extraction-----");
		LinkedHashSet<String> out = new LinkedHashSet<String>();
		out = relation.RelationDetector(AnnFileFolder);
		System.out.println("-----Finish the Relation Extraction-----");
		
		
		System.out.println("~~~~~Start the Postprocessing~~~~~");
		LinkedHashSet<String> tmResultSet = classifier.classifyType(out, stopWordPath);
		System.out.println("~~~~~Finish the Postprocessing~~~~~");
		
		File ResultPath = new File(RelationResultOutputPath);
		BufferedWriter ResultOut = new BufferedWriter(new FileWriter(ResultPath));
		
		
		for(String o : tmResultSet){
			ResultOut.write(o);
			ResultOut.newLine();
		}
		
		ResultOut.close();
		
		
	}

}
