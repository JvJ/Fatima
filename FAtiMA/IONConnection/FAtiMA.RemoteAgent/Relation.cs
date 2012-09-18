using System;
using System.Collections.Generic;
using System.Text;

namespace FAtiMA.RemoteAgent
{
    public class Relation
    {
        private string relationType;
        private string subject;
        private string target;
        private float value;

        public Relation(string relationType, string subject, string target, float value)
        {
            this.relationType = relationType;
            this.subject = subject;
            this.target = target;
            this.value = value;
        }

        public Relation(string relationType)
        {
            this.relationType = relationType;
        }

        public string RelationType
        {
            get
            {
                return this.relationType;
            }
            set
            {
                this.relationType = value;
            }
        }

        public string Subject
        {
            get
            {
                return this.subject;
            }
            set
            {
                this.subject = value;
            }
        }

        public string Target
        {
            get
            {
                return this.target;
            }
            set
            {
                this.target = value;
            }
        }

        public float Value
        {
            get
            {
                return this.value;
            }
            set
            {
                this.value = value;
            }
        }
 
    }
}
