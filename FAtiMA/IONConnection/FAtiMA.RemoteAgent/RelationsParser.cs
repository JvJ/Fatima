using System;
using System.Collections.Generic;
using System.Text;

using System.Xml;
using System.Xml.Schema;
using System.Globalization;

namespace FAtiMA.RemoteAgent
{
    public class RelationsParser : XmlParser
    {
        class Singleton
        {
            internal static readonly RelationsParser Instance = new RelationsParser();

            // explicit static constructor to assure a single execution
            static Singleton()
            {
            }
        }

        RelationsParser()
        {
        }

        public static RelationsParser Instance
        {
            get
            {
                return Singleton.Instance;
            }
        }

        protected override void XmlErrorsHandler(object sender, ValidationEventArgs args)
        {
            // TO DO: deal with xml errors
            Console.WriteLine("Validation error: " + args.Message);
        }

        protected override object ParseElements(XmlDocument xml)
        {
            List<Relation> relations = new List<Relation>();
            Relation rel;

            foreach (XmlNode node in xml.DocumentElement.ChildNodes)
            {
                rel = new Relation(node.Name);
                
                foreach (XmlNode auxNode in node.ChildNodes)
                {
                    if (auxNode.Name.Equals("Subject"))
                    {
                        rel.Subject = auxNode.InnerText;
                    }
                    else if (auxNode.Name.Equals("Target"))
                    {
                        rel.Target = auxNode.InnerText;
                    }
                    else if (auxNode.Name.Equals("Value"))
                    {
                        rel.Value = Convert.ToSingle(auxNode.InnerText, CultureInfo.InvariantCulture);
                    }
                }

                relations.Add(rel);
            }

            return relations;
        }

        protected override void ParseElements(XmlDocument xml, object result)
        {
        }
    }
}
