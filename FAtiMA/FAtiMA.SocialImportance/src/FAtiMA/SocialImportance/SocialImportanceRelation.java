package FAtiMA.SocialImportance;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
public class SocialImportanceRelation {
	
	protected String _subj1;
	protected String _subj2;

	private static final int DEFAULT_RELATION_VALUE = 0;

	private static final long serialVersionUID = 1L;

	public SocialImportanceRelation(){}
	
	
	public SocialImportanceRelation(String sub1, String sub2) {
		this._subj1 = sub1;
		this._subj2 = sub2;
	}

	public String increment(Memory m, float value){
		float si = getValue(m);
		si += value;
		setValue(m, si);
		return _subj2 + ": " + si;
	}

	public String decrement(Memory m, float value){
		float si = getValue(m);
		si -= value;
		setValue(m, si);
		return _subj2 + ": " + si;
	}
	
	public float getValue(Memory m){
		Name relationProperty = Name.ParseName("SI(" + this._subj1 + ","
				+ this._subj2 + ")");
		Float result = (Float) m.getSemanticMemory().AskProperty(relationProperty);
		//If relation doesn't exists, create it in a neutral state
		if (result == null) {
			m.getSemanticMemory().Tell(true,relationProperty, new Float(0));
			return 0;
		}
		return result.floatValue();
	}
	
	public void setValue(Memory m, float relationValue){
		Name relationProperty = Name.ParseName("SI(" + this._subj1 + ","+ this._subj2 + ")");
		m.getSemanticMemory().Tell(true,relationProperty, new Float(relationValue));

	}
	
	public String getHashKey(){
		return "SI"+"-" + this._subj1 + this._subj2;
	}
	
	public String getSubject(){
		return _subj1;
	}
	
	public String getTarget(){
		return _subj2;
	}

	public static SocialImportanceRelation getRelation(String subject1, String subject2) {
		return new SocialImportanceRelation(subject1, subject2);
	}
	

	public static ArrayList<SocialImportanceRelation> getAllRelations(Memory m, String subject1) {
		ArrayList<SocialImportanceRelation> relations = new ArrayList<SocialImportanceRelation>();

		Name relationProperty = Name.ParseName("SI(" + subject1 + ",[X])");
		ArrayList<SubstitutionSet> bindingSets = m.getSemanticMemory().GetPossibleBindings(relationProperty);

		if (bindingSets != null) {
			for (ListIterator<SubstitutionSet> li = bindingSets.listIterator(); li.hasNext();) {
				SubstitutionSet subSet =  li.next();
				Substitution sub = (Substitution) subSet.GetSubstitutions().get(0);
				String target = sub.getValue().toString();
				relations.add(new SocialImportanceRelation(subject1, target));
			}
		}

		return relations;
	}
	
	public static void resetValueAllRelations(Memory m) {
		Name relationProperty = Name.ParseName("SI(SELF,[X])");
		ArrayList<SubstitutionSet> bindingSets = m.getSemanticMemory().GetPossibleBindings(relationProperty);

		if (bindingSets != null) {
			for (ListIterator<SubstitutionSet> li = bindingSets.listIterator(); li.hasNext();) {
				SubstitutionSet subSet =  li.next();
				Substitution sub = (Substitution) subSet.GetSubstitutions().get(0);
				String target = sub.getValue().toString();
				SocialImportanceRelation relation = new SocialImportanceRelation("SELF", target);
				relation.setValue(m, DEFAULT_RELATION_VALUE);
			}
		}
		
	}

}
