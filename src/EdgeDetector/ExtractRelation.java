package EdgeDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class ExtractRelation {
	static String modelPath = DependencyParser.DEFAULT_MODEL;
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	static DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
	static String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	static String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };

	static LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
	static TreebankLanguagePack tlp = lp.getOp().langpack();
	static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        
    private final TokenizerFactory<CoreLabel> tokenizerFactory_wh = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
    
	static String sentence = "";
	static String EntityOneID = "";
	static String EntityOneName = "";
	static String dataType = "";
	static String KindofText = "";
	static String whWord = "";

	public ExtractRelation() {
		sentence = "";
		EntityOneID = "";
		EntityOneName = "";
		dataType = "";
		KindofText = "";
		whWord = "";
	}

	public static LinkedHashSet<String> RelationDetector(String AnnFileFolder) throws IOException {
		LinkedHashSet<String> Result = new LinkedHashSet<String>();
		Result.clear();

		/*
		 * 
		 * Get Relation from annotated_FunctionFolder
		 * 
		 */

		File Functionfolder = new File(AnnFileFolder);
		File[] Function_listOfFiles = Functionfolder.listFiles();

		for (File file : Function_listOfFiles) {
			if (file.isFile()) {
				
				boolean writing = false;
				
				String fileName = file.getName().replace(".txt", "");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;

				LinkedHashSet<String> annotatedPhenotype = new LinkedHashSet<String>();
				LinkedHashSet<String> annotatedTrigger = new LinkedHashSet<String>();
				LinkedHashSet<String> annotatedEntityOne = new LinkedHashSet<String>();
				String checkLine = "ID_";
				System.out.println(file);
				while ((line = br.readLine()) != null) {

					if (checkLine.equals(line.substring(0, 3))) {
						String temp = line.substring(0 + "ID_OriginalText_Reference:".length(), line.length());
						String[] temp_split = temp.split("\t");
						EntityOneName = temp_split[1];
						EntityOneID = temp_split[2];
						continue;
					}

					if (line.contains("SplitSentence:")) {
						sentence = line.substring(0 + "SplitSentence:".length(), line.length());
					}
					String phenotype = "";
					if (line.contains("Phenotype:")) {
						phenotype = line.substring(0 + "Phenotype:".length(), line.length());
						annotatedPhenotype.add(phenotype);
					}
					String trigger = "";
					if (line.contains("Trigger:")) {
						trigger = line.substring(0 + "Trigger:".length(), line.length());
						annotatedTrigger.add(trigger);
					}
					String entityOne = "";
					if (line.contains("EntityOne:")) {
						entityOne = line.substring(0 + "EntityOne:".length(), line.length());
						annotatedEntityOne.add(entityOne);
					}
					if (line.equals("@@@")) {
						/*
						Entity1 is irrelevant
						Trigger is irrelevant
						Phenotype at least 1
						*/
						if (annotatedEntityOne.size() >= 0 && annotatedTrigger.size() >= 0 && annotatedPhenotype.size() > 0) {
							
							////// no relation /////

							if (annotatedTrigger.size() == 0) {

								if (annotatedEntityOne.size() == 0) {
									for (String p : annotatedPhenotype) {
										String[] pheno = p.split("\t");
										
										Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3] + "\t" + pheno[0] + "\t" + pheno[1]
												+ "\t" + "NA" + "\t" + "NA" + "\t" + sentence + "\t" + "1");
									}
								} else {
									for (String e : annotatedEntityOne) {
										String[] entiOne = e.split("\t");
										int e_begin = Integer.valueOf(entiOne[1]);
										int e_end = Integer.valueOf(entiOne[2]);

										for (String p : annotatedPhenotype) {
											writing = false;
											String[] pheno = p.split("\t");

											int p_begin = Integer.valueOf(pheno[1]);
											int p_end = Integer.valueOf(pheno[2]);
											int p_begin_minus_e_begin = p_begin - e_begin;

											if (p_begin_minus_e_begin > 0) {
												writing = true;
												Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
														+ pheno[0]+ "\t" + pheno[1] + "\t" + "NA" + "\t" + "NA" + "\t" + sentence + "\t"
														+ "2");
											}
											
											if (!writing) Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
													+ pheno[0]+ "\t" + pheno[1] + "\t" + "No-relation" + "\t" + "No-relation" + "\t" + sentence + "\t"
													+ "2");
										}

									}
								}

							}

							// ### when only one trigger's detected.
							else if (annotatedTrigger.size() == 1) {
								if (annotatedEntityOne.size() == 0) {
									for (String t : annotatedTrigger) {
										String[] trig = t.split("\t");
										int t_begin = Integer.valueOf(trig[1]);
										int t_end = Integer.valueOf(trig[2]);

										for (String p : annotatedPhenotype) {
											writing = false;
											String[] pheno = p.split("\t");
											
											int p_begin = Integer.valueOf(pheno[1]);
											int p_end = Integer.valueOf(pheno[2]);
											int p_begin_minus_t_end = p_begin - t_end;

											if (p_begin_minus_t_end > 0) {
												boolean marking = checkMark(trig[1], pheno[1]);
												if (marking) {

													int[] dep_distance = new int[2];
													dep_distance = cal_distance(sentence, pheno[0], trig[0], trig[1],
															lp, gsf);
													int result = Math.abs(dep_distance[0] - dep_distance[1]);

													if (result < 5) {
														writing = true;
														Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3] 
																+ "\t" + pheno[0]+ "\t" + pheno[1] + "\t" + trig[0] + "\t" + trig[0]
																+ "\t" + sentence + "\t" + "3");
													}
												}
											}
											
											if (!writing) Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
													+ pheno[0]+ "\t" + pheno[1] + "\t" + "No-relation" + "\t" + "No-relation" + "\t" + sentence + "\t"
													+ "3");
										}
									}
								}

								else {
									for (String e : annotatedEntityOne) {
										String[] entiOne = e.split("\t");
										int e_begin = Integer.valueOf(entiOne[1]);
										int e_end = Integer.valueOf(entiOne[2]);

										for (String t : annotatedTrigger) {
											String[] trig = t.split("\t");
											int t_begin = Integer.valueOf(trig[1]);
											int t_end = Integer.valueOf(trig[2]);
											int t_begin_minus_e_begin = t_begin - e_begin;

											if (t_begin_minus_e_begin > 0) {
												for (String p : annotatedPhenotype) {
													writing = false;
													String[] pheno = p.split("\t");

													int p_begin = Integer.valueOf(pheno[1]);
													int p_end = Integer.valueOf(pheno[2]);
													int p_begin_minus_t_begin = p_begin - t_begin;

													if (p_begin_minus_t_begin > 0) {
														boolean marking = checkMark(trig[1], pheno[1]);
														if (marking) {

															int[] dep_distance = new int[2];
															dep_distance = cal_distance(sentence, pheno[0], trig[0],
																	trig[1], lp, gsf);
															int result = Math.abs(dep_distance[0] - dep_distance[1]);
															if (result < 5) {
																writing = true;
																Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3] + "\t"
																		+ "\t" + pheno[0] + "\t" + pheno[1] + "\t" + trig[0]
																		+ "\t" + trig[0] + "\t" + sentence + "\t"
																		+ "4");
															}
														}
													}
													
													if (!writing) Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
															+ pheno[0]+ "\t" + pheno[1] + "\t" + "No-relation" + "\t" + "No-relation" + "\t" + sentence + "\t"
															+ "4");
												}
											}
										}
									}
								}
							}

							// ### when multiple triggers are detected.
							else {
								if (annotatedEntityOne.size() == 0) {
									for (String p : annotatedPhenotype) {
										writing = false;
										
										String[] pheno = p.split("\t");
										int p_begin = Integer.valueOf(pheno[1]);
										int p_end = Integer.valueOf(pheno[2]);
										int first;
										ArrayList<Integer> myList = new ArrayList<Integer>();
										LinkedHashMap<Integer, String> temp_annotatedTrigger = new LinkedHashMap<Integer, String>();

										for (String t : annotatedTrigger) {
											String[] trig = t.split("\t");
											int t_begin = Integer.valueOf(trig[1]);
											int t_end = Integer.valueOf(trig[2]);
											int p_begin_minus_t_begin = p_begin - t_begin;
											if (p_begin_minus_t_begin > 0) {
												myList.add(p_begin_minus_t_begin);
												temp_annotatedTrigger.put(p_begin_minus_t_begin, t);
											}
										}
										Integer[] arrNum = (Integer[]) myList.toArray(new Integer[myList.size()]);
										first = print2Smallest(arrNum);

										for (Entry<Integer, String> entry : temp_annotatedTrigger.entrySet()) {
											int key = entry.getKey();
											String value = entry.getValue();
											String[] value_split = value.split("\t");

											if (first == key) {

												boolean marking = checkMark(value_split[1], pheno[1]);
												if (marking) {

													int[] dep_distance = new int[2];
													dep_distance = cal_distance(sentence, pheno[0], value_split[0],
															value_split[1], lp, gsf);
													int result = Math.abs(dep_distance[0] - dep_distance[1]);

													if (result < 5) {
														writing = true;
														Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3] 
																+ "\t" + pheno[0]+ "\t" + pheno[1] + "\t" + value_split[0] + "\t"
																+ value_split[0] + "\t" + sentence + "\t"
																+ "5");
													}
												}
											}
										}
										
										if (!writing) Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
												+ pheno[0]+ "\t" + pheno[1] + "\t" + "No-relation" + "\t" + "No-relation" + "\t" + sentence + "\t"
												+ "5");
									}
								}

								else {
									for (String e : annotatedEntityOne) {
										String[] entiOne = e.split("\t");
										int e_begin = Integer.valueOf(entiOne[1]);
										int e_end = Integer.valueOf(entiOne[2]);

										for (String p : annotatedPhenotype) {
											writing = false;
											String[] pheno = p.split("\t");

											int p_begin = Integer.valueOf(pheno[1]);
											int p_end = Integer.valueOf(pheno[2]);
											int first;
											ArrayList<Integer> myList = new ArrayList<Integer>();
											LinkedHashMap<Integer, String> temp_annotatedTrigger = new LinkedHashMap<Integer, String>();

											for (String t : annotatedTrigger) {
												String[] trig = t.split("\t");
												int t_begin = Integer.valueOf(trig[1]);
												int t_end = Integer.valueOf(trig[2]);
												int p_begin_minus_t_begin = p_begin - t_begin;
												int t_begin_minus_e_begin = t_begin - e_begin;

												if ((p_begin_minus_t_begin > 0) && (t_begin_minus_e_begin > 0)) {
													myList.add(p_begin_minus_t_begin);
													temp_annotatedTrigger.put(p_begin_minus_t_begin, t);
												}
											}
											Integer[] arrNum = (Integer[]) myList.toArray(new Integer[myList.size()]);
											first = print2Smallest(arrNum);

											for (Entry<Integer, String> entry : temp_annotatedTrigger.entrySet()) {
												int key = entry.getKey();
												String value = entry.getValue();
												String[] value_split = value.split("\t");

												if (first == key) {
													boolean marking = checkMark(value_split[1], pheno[1]);
													if (marking) {
														int[] dep_distance = new int[2];
														dep_distance = cal_distance(sentence, pheno[0], value_split[0],
																value_split[1], lp, gsf);
														int result = Math.abs(dep_distance[0] - dep_distance[1]);

														if (result < 5) {
															writing = true;
															Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3] 
																	+ "\t" + pheno[0]+ "\t" + pheno[1] + "\t" + value_split[0] + "\t"
																	+ value_split[0] + "\t" + sentence + "\t"
																	+ "6");
														}
													}
												}
											}
											
											if (!writing) Result.add(EntityOneName + "\t" + EntityOneID + "\t" + pheno[3]  + "\t" 
													+ pheno[0]+ "\t" + pheno[1] + "\t" + "No-relation" + "\t" + "No-relation" + "\t" + sentence + "\t"
													+ "6");
										}
									}
								}
							}
						}

						annotatedPhenotype.clear();
						annotatedTrigger.clear();
						annotatedEntityOne.clear();

					}

				}
				br.close();
			}
		}

		return Result;
	}

	static int print2Smallest(Integer arr[]) {

		int first, second, arr_size = arr.length;

		first = Integer.MAX_VALUE;
		for (int i = 0; i < arr_size; i++) {
			if (arr[i] < first) {
				first = arr[i];
			}
		}
		return first;
	}

	/// cal distance from word to root
	static int[] cal_distance(String main_sentence, String entity2, String trigger, String triggerOffset,
			LexicalizedParser lp, GrammaticalStructureFactory gsf) {

		whWord = "";
		int whOffset = 0;
		int triOffset = Integer.parseInt(triggerOffset);

		int[] distance = new int[2]; // 0 : from entity2, 1 : from trigger

		String originalTrigger = trigger;
		String text = main_sentence;
		text = text.toLowerCase();
		text = text.replace(".", "");

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		Iterator<List<HasWord>> it = tokenizer.iterator();
		List<HasWord> sentence = null;

		while (it.hasNext()) {
			sentence = it.next();
		}

		String[] splitEntity2 = entity2.split(" ");
		entity2 = splitEntity2[splitEntity2.length - 1];

		Tree parse = lp.apply(sentence);
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection tdl = gs.typedDependencies();

		List<Tree> leaves = parse.getLeaves();
		String termSplitter = ",WDT, ,WP, ,WP$, ,WRB,";

		for (Tree leaf : leaves) {
			Tree parent = leaf.parent(parse);
			// System.out.println(leaf.label().value() + "-" +
			// parent.label().value() + " ");

			if (termSplitter.contains("," + parent.label().value() + ",")) {
				// System.out.println("contain");
				whWord = leaf.label().value();
				// System.out.println("++whWord is ::"+whWord);
				whOffset = main_sentence.lastIndexOf(whWord);
			}
		}

		String[] depArrayTrigger = new String[tdl.size()];
		String[] depArrayEntity2 = new String[tdl.size()];
		String[] depArrayWh = new String[tdl.size()];
		String[] token_array = new String[tdl.size()];
		int depSize = 0;

		for (Iterator<TypedDependency> iter = tdl.iterator(); iter.hasNext();) {

			TypedDependency var = iter.next();
			String dependencyType = var.reln().getShortName();
			int Token_ID = var.dep().index();
			int Parent_ID = var.gov().index();

			String token = var.dep().toString();
			String Parent = var.gov().toString();
			// Parent = Parent.substring(0, Parent.length()-3);

			String token_ch = token;
			String parent_ch = Parent;

			String regBigAlpha = "ABCDEFGHIJKMNLOPQRSTUVWXYZ";

			StringBuffer sb = new StringBuffer();
			////////////////// Token ////////////////////////////
			for (int j = 0; j < token_ch.length(); j++) {
				char c = token_ch.charAt(j);
				String c_string = "" + c;

				if (regBigAlpha.contains(c_string)) {
				} else {
					sb.append(c_string);
				}
			}
			String token_result = sb.toString();

			if (token_result.length() > 1) {
				token_result = token_result.substring(0, token_result.length() - 1);
			}
			///////////////////// Parent ////////////////////////////
			StringBuffer sb_2 = new StringBuffer();
			for (int j = 0; j < parent_ch.length(); j++) {
				char c = parent_ch.charAt(j);
				String c_string = "" + c;
				if (regBigAlpha.contains(c_string)) {
				} else {
					sb_2.append(c_string);
				}
			}
			String parent_result = sb_2.toString();
			if (parent_result.length() > 1) {
				parent_result = parent_result.substring(0, parent_result.length() - 1);
			} else {
				parent_result = "root";
			}

			depArrayTrigger[depSize] = token_result + "\t" + parent_result;
			depArrayEntity2[depSize] = token_result + "\t" + parent_result;
			depArrayWh[depSize] = token_result + "\t" + parent_result;

			token_array[depSize] = token_result;
			depSize++;
		}

		//////////////////////////////////////////////
		//////// Make Dependency parser easily////////
		//////////////////////////////////////////////

		String upper = "";
		int limit = 0;

		StringBuffer triggerDep = new StringBuffer();
		triggerDep.append(trigger);
		triggerDep.append("\t");

		while (!upper.equals("root")) {
			limit++;
			for (int i = 0; i < depSize; i++) {
				String[] depSplit = depArrayTrigger[i].split("\t");

				if (trigger.equals(depSplit[0])) {
					upper = depSplit[1];
					trigger = depSplit[1];

					depArrayTrigger[i] = "z9x9c9" + "\t" + "z9x9c9";

					triggerDep.append(upper);
					triggerDep.append("\t");
					break;
				}
			}
			if (limit > 200) {
				break;
			}
		}
		String[] tempTrigger = triggerDep.toString().split("\t");
		int distanceTrigger = tempTrigger.length;

		if (!(limit == 0))
			distanceTrigger = limit;

		// System.out.println("trigger : " + distanceTrigger);
		// System.out.println(triggerDep);

		///////////////////////////////////////////////////////////

		upper = "";
		limit = 0;

		StringBuffer entity2Dep = new StringBuffer();
		entity2Dep.append(entity2);
		entity2Dep.append("\t");

		while (!upper.equals("root")) {
			limit++;
			for (int i = 0; i < depSize; i++) {
				String[] depSplit = depArrayEntity2[i].split("\t");

				if (entity2.equals(depSplit[0])) {
					upper = depSplit[1];
					entity2 = depSplit[1];

					depArrayEntity2[i] = "z9x9c9" + "\t" + "z9x9c9";

					entity2Dep.append(upper);
					entity2Dep.append("\t");
					break;
				}
			}

			if (limit > 200) {
				break;
			}
		}
		String[] tempEntity2 = entity2Dep.toString().split("\t");
		int distanceEntity2 = tempEntity2.length;

		if (!(limit == 0))
			distanceEntity2 = limit;

		// System.out.println("Entity2 : " + distanceEntity2);
		// System.out.println(entity2Dep);

		///////////////////////////////////////////////////////////

		upper = "";
		limit = 0;

		StringBuffer whDep = new StringBuffer();
		whDep.append(whWord);
		whDep.append("\t");

		while (!upper.equals("root")) {
			limit++;
			for (int i = 0; i < depSize; i++) {
				String[] depSplit = depArrayWh[i].split("\t");

				if (whWord.equals(depSplit[0])) {
					upper = depSplit[1];
					whWord = depSplit[1];

					depArrayWh[i] = "z9x9c9" + "\t" + "z9x9c9";

					whDep.append(upper);
					whDep.append("\t");
					break;
				}
			}

			if (limit > 200) {
				break;
			}
		}
		String[] tempwhWord = whDep.toString().split("\t");
		int distancewhWord = tempwhWord.length;

		if (!(limit == 0))
			distancewhWord = limit;

		String whDepTree = whDep.toString();

		distance[0] = distanceTrigger;
		distance[1] = distanceEntity2;

		String subSentence = main_sentence.substring(0, whOffset);

		if (whDepTree.contains(originalTrigger) && whOffset < triOffset) {
			if (!(subSentence.contains(" is ") || subSentence.contains(" binds "))) {
				distance[0] = 1000;
				distance[1] = 0;
			}
		} else {
		}

		/////// return value ////////

		return distance;

	}

	static boolean checkMark(String triggerOffset_str, String phenoOffset_str) {
		boolean check = true;
		///// exclude relation when find ; . ( /////

		int triggerOffset = Integer.parseInt(triggerOffset_str);
		int phenoOffset = Integer.parseInt(phenoOffset_str);

		if (triggerOffset < phenoOffset) {
			String subString = sentence.substring(triggerOffset, phenoOffset);

			if (subString.contains(";") || subString.contains("(") || subString.contains(".")) {
				check = false;
			}
		}

		else {
			check = false;
		}

		return check;

	}
}
