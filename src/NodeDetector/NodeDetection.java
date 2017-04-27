package NodeDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class NodeDetection {

	static final double CHUNK_SCORE = 1.0;
		
	public void NodeDectector(String TriggerDictionaryPath, String InputTextFile, String AnnFileFolder)
			throws Exception {
		// TODO Auto-generated method stub

		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		
		MapDictionary<String> Trigger_dictionary = new MapDictionary<String>();
		File TDicfile = new File(TriggerDictionaryPath);
		BufferedReader TDic_br = new BufferedReader(new FileReader(TDicfile));
		String Tline = null;

		System.out.println("Trigger Dictionary loading....");
		while ((Tline = TDic_br.readLine()) != null) {
			String[] contents = Tline.split("\t");
			String word = contents[0];
			String id = contents[1];

			Trigger_dictionary.addEntry(new DictionaryEntry<String>(word, id, CHUNK_SCORE));

		}
		System.out.println("Trigger Dictionary is sucessfully loaded");

		ExactDictionaryChunker T_dictionaryChunkerTF = new ExactDictionaryChunker(Trigger_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);

		File InputTFile = new File(InputTextFile);
		BufferedReader Input_br = new BufferedReader(new FileReader(InputTFile));

		String Iline = null;

		LinkedHashSet<String> temp_Tchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Tchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Pchunk_result = new LinkedHashSet<String>();

		int counting = 0;
		String ExFunction = "";

		
		File temp = new File("D:/JUN/MCMT/drug_disease_RE/REpaper_precision/workspace/sentence_classification_trigger.txt");
		BufferedWriter temp_out = new BufferedWriter(new FileWriter(temp));
		
		
		
		while ((Iline = Input_br.readLine()) != null) {

			String[] SplitIline = Iline.split("\t");
			String EntityOne = SplitIline[0];
			String Function = SplitIline[1];
			String Reference = SplitIline[2];

			String PhenoName = SplitIline[4];
			String PhenoType = SplitIline[5];
			String Start_offset = SplitIline[6];
			String End_offset = SplitIline[7];

			if (ExFunction.equals(Function)) {
				Pchunk_result.add(PhenoName + "\t" + Start_offset + "\t" + End_offset + "\t" + PhenoType);
			}

			else {
				Pchunk_result.clear();
				Pchunk_result.add(PhenoName + "\t" + Start_offset + "\t" + End_offset + "\t" + PhenoType);
				counting++;
			}
			
			String FileName = null;
			
			
			if(EntityOne.length() > 10){
				FileName = Integer.toString(counting) + EntityOne.substring(0,7);
			}
			else{
				FileName = Integer.toString(counting) + EntityOne;
			}
			
			
			File files = new File(AnnFileFolder + FileName + ".ann");
			BufferedWriter out = new BufferedWriter(new FileWriter(files));
			
			
			
			
			String sentenceType = Sentence_Classificator(EntityOne, Function, lp, TriggerDictionaryPath);
					
			
			
			
			temp_out.write(Reference + "\t" + EntityOne + "\t" + Function + "\t" + sentenceType);
			temp_out.newLine();
			
			
			
			
			out.write("ID_OriginalText_Reference:" + "term" + "\t" + EntityOne + "\t" + Reference);
			out.newLine();

			out.write("SplitSentence:" + Function);
			out.newLine();

			for (String p : Pchunk_result) {
				out.write("Phenotype:" + p);
				out.newLine();
			}

			temp_Tchunk_result.clear();
			Tchunk_result.clear();
			temp_Tchunk_result = Tchunk(T_dictionaryChunkerTF, Function.toLowerCase().trim(), "BROMFED-DM", "9");
			for (String t : temp_Tchunk_result) {
				String[]splitT = t.split("\t");
				int start_trig = Integer.parseInt(splitT[1]);
				int end_trig = Integer.parseInt(splitT[2]);
				
				for (String p : Pchunk_result){
					String[]splitP = p.split("\t");
					int start_pheno = Integer.parseInt(splitP[1]);
					int end_pheno = Integer.parseInt(splitP[2]);
				
					if(end_trig < start_pheno || start_trig > end_pheno){
						Tchunk_result.add(t);
					}
					else{
						
					}
				}
			}
			
			for(String t:Tchunk_result){
				out.write("Trigger:" + t);
				out.newLine();
			}

			out.write("@@@");
			out.close();
			
			ExFunction = Function;

		}
		Input_br.close();
		TDic_br.close();

		temp_out.close();
		
	}


	static LinkedHashSet<String> Tchunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID)throws IOException {
		LinkedHashMap<String, String> result_map = new LinkedHashMap<String, String>();
		LinkedHashSet<String> result_set = new LinkedHashSet<String>();
		result_map.clear();
		result_set.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words
			if (end + 3 <= text.length()) {
				if (text.toLowerCase().trim().substring(start, end + 3).equals(phrase.toLowerCase().trim() + " by")) {
					continue;
				}
			}
			if (result_map.containsKey(phrase + "\t" + start + "\t" + end)) {
				result_map.put(phrase + "\t" + start + "\t" + end,
						result_map.get(phrase + "\t" + start + "\t" + end) + "|" + type);
			} else {
				result_map.put(phrase + "\t" + start + "\t" + end, type);
			}
		}

		for (Entry<String, String> entry : result_map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result_set.add(key + "\t" + value);
		}
		return result_set;
	}

	static String Sentence_Classificator(String entityOne, String Function, LexicalizedParser lp, String TriggerDictionaryPath) throws IOException{
		
		String Clause_level = "S, SBAR, SBARQ, SINV, SQ,";
		String sentenceType = null;
		
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(Function));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);

		StringBuffer parserTree = new StringBuffer();			
		parserTree.append(parse);
		
		String[] splitTree = parserTree.toString().split(" ");
		
		//System.out.println(Function);
		//System.out.println(parse);
		
		
		
		
		//Entity1 is in the Function
		if (Function.toLowerCase().contains(entityOne.toLowerCase().trim())) {
			for (int i = 0; i < splitTree.length; i++) {
				if (splitTree[i].contains("(")) {
					if (Clause_level.contains(splitTree[i].substring(1) + ",")) {
						sentenceType = "SentenceWithEntityOne";
						//System.out.println(Function);
						//System.out.println("This Data is sentence with Entity1");
						
					}	else{}
				}else{}
			}
		}
		
		//Entity1 is not in the Function
		else {
			if (sentenceType == null) {
				BufferedReader RelationDic = new BufferedReader(new FileReader(TriggerDictionaryPath));
				String relationLine = "";

				while ((relationLine = RelationDic.readLine()) != null) {
					String[] split_Relation = relationLine.split("\t");
					String triggerName = split_Relation[0];
					String RelationName = split_Relation[1];
					
					String space_trigger = " "+triggerName;			// __trigger
					String trigger_space = triggerName + " ";		// trigger__
					
					if (Function.toLowerCase().contains(space_trigger) || Function.toLowerCase().contains(trigger_space)) {
						sentenceType = "DataWithoutEntityOnewithTrigger\tsplit_Relation[0]";	
						//System.out.println(Function);
						//System.out.println("This Data is TextData with Trigger except Entity1");
					}
				}
				RelationDic.close();
			}

			if (sentenceType == null) {
				for (int k = 0; k < splitTree.length; k++) {
					if (splitTree[k].contains("(")) {
						if (Clause_level.contains(splitTree[k].substring(1) + ",")) {
							sentenceType = "DataWithoutEntityOnewithoutTrigger";
							//System.out.println("This Data is sentence without trigger except Entity1");

						}	else{}
					} else {}
				}
			}
			
			if (sentenceType == null) {
				sentenceType = "NP without Trigger";
				//System.out.println("NP without Trigger");
			}			
		}
		
		
		return sentenceType;
	}
}
