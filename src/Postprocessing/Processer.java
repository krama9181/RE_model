package Postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;

public class Processer {

	public LinkedHashSet<String> classifyType(LinkedHashSet<String> tempDataSet, String stopWordPath) throws IOException {
		LinkedHashSet<String> tmResultSet = new LinkedHashSet<String>();

		String TriggerDictionaryPath = "D:/JUN/MCMT/drug_disease_RE/dictionary/newCondition/renewalRelation_type.txt";
		
		File tdDic = new File(TriggerDictionaryPath);
		BufferedReader tdBr = new BufferedReader(new FileReader(tdDic));
		
		String tdLine = "";
		
		StringBuffer decreaseWord = new StringBuffer();
		decreaseWord.append(",");
		StringBuffer increaseWord = new StringBuffer();		
		increaseWord.append(",");
		
		while((tdLine = tdBr.readLine()) != null){
			String[] splitLine = tdLine.split("\t");
			if(splitLine[1].equals("Increase")){
				increaseWord.append(splitLine[0] + ",");
			}
			else if(splitLine[1].equals("Decrease")){
				decreaseWord.append(splitLine[0] + ",");
			}
			else{
				
			}
		}
		
		String decreaseType = decreaseWord.toString();
		String increaseType = increaseWord.toString();
		
		//String decreaseType = ",decrease,decreases,decreased,decreasing,decrement,contract,contracts,contracted,contracting,contraction,decline,declines,declined,declining,reduce,reduces,reduced,reducing,reduction,shrink,shrinks,shrunk,shrank,shrinking,shrinkage,abate,abates,abated,abating,abatement,constrict,constricts,constricted,constricting,depress,depresses,depressed,depressing,depression,diminish,diminishes,diminished,diminishing,diminution,dwindle,dwindles,dwindled,dwindling,subside,subsides,subsided,subsiding,subsidence,decrescence,relieve,relieves,relieved,relieving,lessens,lessen,lessened,lessening,weaken,weakens,weakened,weakening,alleviate,alleviates,alleviated,alleviating,alleviation,ease,eases,eased,easing,inhibit,inhibitions,inhibiton,inhibiting,inhibitors,inhibition,inhibited,inhibitory,inhibits,inhibitive,inhibitor,curb,curbed,curbs,curbing,discourage,discourages,discouraged,discouraging,hinder,hinders,hindered,hindering,impede,impedes,impeded,impeder,impeding,obstruct,obstructs,obstructed,obstructing,obstructive,obstructing,restrain,restrains,restrained,restraining,restrainable,stymie,stymied,stymies,stymying,stymieing,suppress,suppresses,suppressed,suppressing,stop,stops,stopped,stopping,restrict,restricts,restricted,restricting,foil,foils,foiled,foiling,halt,halts,halted,halting,hamper,hampers,hampered,hampering,interrupt,interrupts,interrupted,interrupting,preclude,precludes,precluded,precluding,disturb,disturbs,disturbed,disturbing,prevent,preventative,prevents,prevention,preventing,preventive,preventable,prevented,avert,averts,averting,averted,forestall,forestalls,forestalled,forestalling,forfend,forfends,forfended,forfending,prohibit,prohibits,prohibited,prohibiting,prohibitive,prohibition,prohibitor,prohibiter,ban,bans,banned,banning,bannable,forbid,forbids,forbidden,forbidding,forbiddance,outlaw,outlaws,outlawed,outlawing,debar,debars,debarred,debarring,debarment,release,released,releasing,release,releases,remove,removes,removable,remove,removed,removal,removing,delete,deletes,deleted,deleting,deletion,eliminate,eliminates,eliminated,eliminating,get rid of,gets rid of,got rid of,getting rid of,treat,treatment,treats,treating,treated,cure,cured,curing,cures,remedy,remedies,remediable,remedial,disperse,disperses,dispersed,dispersing,dispersion,resolve,resolves,resolved,resolving,resolution,destroy,destroys,destroyed,destroying,destruction,alleviate,alleviates,alleviated,alleviating,alleviation,stop,stops,stopped,stopping,purge,purges,purged,purging,purgation,interrupt,interrupted,interrupts,interrupting,hold up,held up,holds up,holding up,break,broke,breaks,breaking,broken,interfere,interfered,interferes,interfering,neutralize,neutralizes,neutralized,neutralization,neutralisation,";
		//String increaseType = ",activate,activations,activates,activatory,activational,activation,activated,activator,activating,activatable,activators,arouse,arouses,aroused,arousing,arousable,add,adds,added,addition,adding,sum,sums,summed,summing,summation,summate,summates,summated,summating,assist,assists,assisted,assisting,assistance,aid,aids,aided,aiding,collaborate,collaborates,collaborated,collaborating,collaborative,cooperate,cooperates,cooperated,cooperating,cooperation,cooperative,support,supports,supported,supporting,cause,causes,caused,causing,generate,generates,generated,generating,generative,induce,induces,induced,inducing,lead,leads,led,leading,precipitate,precipitates,precipitated,precipitating,precipitative,produce,produces,produced,producing,provoke,provokes,provoked,provoking,elicit,elicits,elicited,eliciting,elicitable,engender,engenders,engendered,engendering,motivate,motivates,motivated,motivating,result in,results in,resulted in,resulting in,trigger,triggers,triggered,triggering,increase,increases,increasing,increased,increment,develop,develops,development,developable,developed,developing,escalate,escalates,escalation,escalated,escalating,expand,expands,expanded,expanding,expansion,raise,raises,raised,raising,raisable,rise,rises,rose,risen,rising,accumulate,accumulation,accumulable,accumulative,accumulated,accumulating,accumulates,augment,augments,augmented,augmenting,augmentation,augmentable,cumulate,cumulation,enlarge,enlarges,enlargement,enlargeable,extend,extends,extended,extending,extension,maximize,maximizes,maximized,maximizing,maximization,maximisation,maximation,multiply,multiplies,multiplied,multiplying,multiplication,spread,spreads,spreading,spreadability,spreadable,promote,promotes,promoted,promoting,promotion,promotable,promotive,promotional,boost,boosts,boosted,boosting,encourage,encourages,encouraged,encouraging,encouragement,stimulate,stimulates,stimulated,stimulating,stimulation,stimulative,unleash,unleashes,unleashed,unleashing,elevate,elevates,elevated,elevation,elevating,improve,improves,improved,improving,unleashed,unleashing,";
		//String unknownType = ",affect,affects,affected,affecting,affective,influence,influences,influenced,influencing,impinge,impinges,impinged,impinging,impingement,associate,associates,associated,associating,association,associational,aassociations,associative,relate,relates,related,relating,relation,soothe,soothes,soothed,soothing,regulate,regulates,regulated,regulating,regulation,";
		
		String positivePheno = "bpoc, risu, DNP, POS,";
		String negativePheno = "acab, anab, cgab, dsyn, emod, fndg, hops, inpo, mobd, neop, patf, sosy, banr, NEG,";
		 		
		// read stop word dictionary
		File stDic = new File(stopWordPath);
		BufferedReader stBr = new BufferedReader(new FileReader(stDic));
		
		StringBuffer strBuffer = new StringBuffer();		
		strBuffer.append(",");

		String line = null;
		while ((line = stBr.readLine()) != null) {
			strBuffer.append(line);
			strBuffer.append(",");
		}
		stBr.close();
		
		String stopWordList = strBuffer.toString();

		// relation type classification
		for (String tempData : tempDataSet) {
			String tempDataSplit[] = tempData.split("\t");
			String entityOneName = tempDataSplit[0];
			String Reference = tempDataSplit[1];
			String semanticType = tempDataSplit[2];
			String phenName = tempDataSplit[3];
			String startOffset = tempDataSplit[4];
			String trigger = "," + tempDataSplit[5] + ",";
			String function = tempDataSplit[7];
			String sentenceType = tempDataSplit[8];
			
			if (!stopWordList.contains("," + phenName + ",")) {
				
				String relationType = null;
				if (trigger.equals(",NA,")) {
					if(positivePheno.contains(semanticType)) relationType = "Increase";
					else if(negativePheno.contains(semanticType)) relationType = "Decrease";
					else relationType = "Decrease";
				}				
				else if (decreaseType.contains(trigger)) {
					if(positivePheno.contains(semanticType)) relationType = "Decrease";
					else if(negativePheno.contains(semanticType)) relationType = "Decrease";
					else relationType = "Decrease";
				}				
				else if (increaseType.contains(trigger)) {
					if(positivePheno.contains(semanticType)) relationType = "Increase";
					else if(negativePheno.contains(semanticType)) relationType = "Increase";
					else relationType = "Increase";
				}
				else if (trigger.equals(",No-relation,")){
					relationType = "No-relation";
				}
				else relationType = "Association";
				
				
				tmResultSet.add(Reference + "\t" + entityOneName + "\t" + relationType + "\t" + tempDataSplit[6] + "\t" + phenName + "\t" + semanticType + "\t" + startOffset + "\t" + function + "\t" + sentenceType);
			}
			
			else{
				System.out.println(phenName);
			}
		}

		return tmResultSet;
	}

}
