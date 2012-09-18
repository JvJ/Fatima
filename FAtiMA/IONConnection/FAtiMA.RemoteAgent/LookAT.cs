using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using ION.Core;
using ION.Core.Extensions;
using ION.Core.Events;
using ION.Meta;

namespace FAtiMA.RemoteAgent
{
    public class LookAT : EntityAction<ActionParameters>
    {

        private RemoteMind remoteMind;


        public LookAT(Entity body, RemoteMind remoteMind) : base(body,"look-at")
        {
            this.remoteMind = remoteMind;
            this.EventHandlers.Add<IStarted<LookAT>>(this.OnStart);
            this.EventHandlers.Add<ISucceeded<LookAT>>(this.OnSucceed);
        }

        
        public void OnStart(IStarted<LookAT> evt)
        {
            ApplicationLogger.Instance().WriteLine("Starting Look-at action...");
            /*Element e;
            Entity targetEntity;
            String entityName = evt.Action.StartArguments.Target;
            
            System.Console.WriteLine(this.Name + " looks at " + entityName);
            string msg = LOOK_AT + " " + entityName;

            e = Simulation.Instance.Elements[Entity.getIDByName(entityName)];

            if (e is Entity)
            {
                targetEntity = e as Entity;
                msg += " " + targetEntity.getStringWithAllProperties();
            }


            Send(msg);
             * */
           
            this.Stop(true);
        }

        public void OnSucceed(ISucceeded<LookAT> evt)
        {
            
            Element e;
            Entity targetEntity;
            String entityName = evt.Action.StartArguments.Target;
            
            System.Console.WriteLine(this.Name + " looks at " + entityName);
            string msg = "LOOK-AT" + " " + entityName;

            e = Simulation.Instance.Elements[Entity.getIDByName(entityName)];

            if (e is Entity)
            {
                targetEntity = e as Entity;
                msg += " " + targetEntity.getStringWithAllProperties();
            }

            remoteMind.Send(msg);
        }
    }
}
