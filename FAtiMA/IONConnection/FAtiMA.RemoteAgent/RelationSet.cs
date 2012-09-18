using System;
using System.Collections.Generic;
using System.Text;

namespace FAtiMA.RemoteAgent
{
    public class RelationSet
    {
        private Dictionary<string, Dictionary<string, Relation>> relations;

        public RelationSet()
        {
            this.relations = new Dictionary<string,Dictionary<string, Relation>>();
        }

        public void AddRelation(Relation rel)
        {
            Dictionary<string, Relation> aux;
            string relationType = rel.RelationType;
            if (this.relations.ContainsKey(relationType))
            {
                aux = this.relations[relationType];
            }
            else
            {
                aux = new Dictionary<string,Relation>();
                this.relations.Add(relationType, aux);
            }
            aux.Add(rel.Target, rel);
        }

        public Relation GetRelation(string relationType, string target)
        {
            if (this.relations.ContainsKey(relationType))
            {
                return this.relations[relationType][target];
            }
            else return null;
        }

        public string toXml()
        {
            string relations = string.Empty;

            foreach (KeyValuePair<string, Dictionary<string, Relation>> aux in this.relations)
            {
                relations += "<Relations type=\"" + aux.Key + "\">";
                foreach (KeyValuePair<string, Relation> r in aux.Value)
                {
                    relations += "<" + r.Key + ">" + r.Value.Value + "</" + r.Key + ">";
                }
            }
            return relations;
        }
    }
}
