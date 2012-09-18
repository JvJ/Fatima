// RemoteCharacter.cs - 
//
// Copyright (C) 2006 GAIPS/INESC-ID
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// 
// Company: GAIPS/INESC-ID
// Project: FearNot!
// Created: 06/04/2006
// Created by: João Dias
// Email to: joao.dias@inesc-id.pt
// 
// History:
// João Dias: 24/09/2006 - File Created
//

using System.Text;
using System.Net.Sockets;
using System.Threading;

using ION.Core.Events;
using ION.Core.Extensions;
using ION.Core.Extensions.Events;
using ION.Meta;
using ION.Meta.Events;
//using Unity.Realizer;

using System.IO;
using System;

namespace FAtiMA.RemoteAgent
{
    public class RemoteMind
    {
        private const string EOM_TAG = "\n";
        private const string EOF_TAG = "<EOF>";
        private const int bufferSize = 1024;


        public const string REMOTE_ACTION_PARAMETERS = "REMOTE_ACTION_PARAMETERS";

        public const string START_MESSAGE = "CMD Start";
        public const string STOP_MESSAGE = "CMD Stop";
        public const string RESET_MESSAGE = "CMD Reset";
        public const string SAVE_MESSAGE = "CMD Save";
        public const string REMOVE_ALL_GOALS_MESSAGE = "REMOVEALLGOALS";
        public const string ADD_GOALS_MESSAGE_START = "ADDGOALS ";
        public const string ACTION_FINISHED = "ACTION-FINISHED";
        public const string ACTION_STARTED = "ACTION-STARTED";
        public const string ACTION_FAILED = "ACTION-FAILED";
        public const string CANCEL_ACTION = "CANCEL-ACTION";
        public const string ENTITY_ADDED = "ENTITY-ADDED";
        public const string ENTITY_REMOVED = "ENTITY-REMOVED";
        public const string PROPERTY_CHANGED = "PROPERTY-CHANGED";
        public const string PROPERTY_REMOVED = "PROPERTY-REMOVED";
        public const string USER_SPEECH = "USER-SPEECH";
        public const string AGENTS = "AGENTS";
        public const string LOOK_AT = "LOOK-AT";
        public const string ADVANCE_TIME = "ADVANCE-TIME";
        public const string STOP_TIME = "STOP-TIME";
        public const string RESUME_TIME = "RESUME-TIME";
        public const string SHUTDOWN_MESSAGE = "SHUTDOWN";
        public const string UPDATE = "UPDATE";

        //communication/socket fields
        private Socket socket;
        private NetworkStream socketStream;
        private bool receiverAlive;
        private Thread receiverThread;
        private byte[] buffer = new byte[bufferSize];

        //LanguageEngine
        private LanguageEngineMaster languageEngine;


        //look-at hack
        private LookAT lookATAction;

        //properties
        //public Property<EmotionalState> emotionalState;
        //protected Property<RelationSet> relations;
        public EntityProperty<string> Sex {get ; private set;}
        public EntityProperty<string> Role { get; private set; }
        public Entity Body { get; private set; }
        //actions  
       
        public RemoteMind(Entity body, LanguageEngineMaster languageEngine, string sex, string role) 
		{
            this.Body = body;
            this.languageEngine = languageEngine;
            this.receiverAlive = false;
            //this.emotionalState = new Property<EmotionalState>(new EmotionalState());
            //this.relations = new Property<RelationSet>(new RelationSet());
            this.Sex = new EntityProperty<string>(this.Body,"sex",sex);
            this.Role = new EntityProperty<string>(this.Body, "role", role);
            this.Body.AddProperty("*",this.Sex);
            this.Body.AddProperty("*",this.Role);

            this.lookATAction = new LookAT(this.Body,this);
            this.Body.AddAction(this.lookATAction);
            this.Body.AddToSimulation(Simulation.Instance);

            //Simulation.Instance.EventHandlers.Add<IAddedTo<Entity, Simulation>>(this.EntityAdded);
            Simulation.Instance.EventHandlers.Add<IElementAddedToSimulation>(this.ElementAdded);

            Simulation.Instance.EventHandlers.Add<ISimulationUpdated>(this.SimulationUpdated);

            //Simulation.Instance.EventHandlers.Add(new DelegateEventHandler<IRemoved<Entity, Simulation>>(this.EntityRemoved));
            
		}

        /*public override void AddToSimulation(Simulation s)
        {
            //base.AddToSimulation(s);
            //s.Elements.Add(emotionalState);
            //s.Elements.Add(loadAction);
        }*/

        ~RemoteMind()
        { 
            System.Console.WriteLine("AGENT ENTITY: " + this.Body.Name + " was destroyed...");
            
            if (this.socketStream != null)
            {
                if (receiverAlive)
                {
                    this.Send(SHUTDOWN_MESSAGE); //Makes the java mind close the socket
                    this.receiverAlive = false;
                }
                socketStream.Close();
            }

            if (this.socket != null)
            {
                try
                {
                    socket.Close();
                }
                catch(Exception)
                {
                }
            }
        }


        public Socket Socket
        {
            get
            {
                return this.socket;
            }
            set
            {
                this.socket = value;
                this.socketStream = new NetworkStream(this.socket);
            }
        }

        public bool ReceiverAlive
        {
            get
            {
                return this.receiverAlive;
            }
            set
            {
                this.receiverAlive = value;
            }
        }

        public void Start()
        {
            Entity ent;
            string agents = "";
            foreach (Element e in Simulation.Instance.Elements)
            {
                if (e is Entity)
                {
                    ent = e as Entity;
                    agents += " " + ent.Name;
                    RegisterEventListeners(e);
                }
            }

            this.Send("OK");
            this.receiverAlive = true;

            this.receiverThread = new Thread(new ThreadStart(ReceiveThread));
            this.receiverThread.Start();

            this.Send("AGENTS" + agents);
        }

        // MARCO: ADD BASE OnDestroy
        public void OnDestroy()
        {
            System.Console.WriteLine("AGENT ENTITY: " + this.Body.Name + " was destroyed...");
            if (receiverAlive)
            {
                this.Send(SHUTDOWN_MESSAGE); //Makes the java mind close the socket
                this.receiverAlive = false;
            }
        }

        private void Parse(String msg)
        {
            ActionParameters parameters;

            if (msg.StartsWith(PROPERTY_CHANGED))
            {
                /*string[] aux = msg.Split(' ');
                string propertyName = aux[2];
                string value = aux[3];
                if(this.HasProperty<Property<String>>(propertyName))
                {
                    Property<String> p = this.GetProperty<Property<String>>(propertyName);
                    if (!p.Value.Equals(value))
                    {
                        p.Value = value;
                    }
                }*/
            }
            else if (msg.StartsWith("<EmotionalState"))
            {
                string facialExpression = string.Empty;
                //EmotionalState es = (EmotionalState)EmotionalStateParser.Instance.Parse(msg);
                //this.emotionalState.Value = es;

                /*Emotion e = es.GetStrongestEmotion();
                if (e != null)
                {
                    if (e.Type.Equals(Emotion.GLOATING_EMOTION))
                    {
                        facialExpression = "laugh";
                    }
                    else if (e.Type.Equals(Emotion.REPROACH_EMOTION)
                            || e.Type.Equals(Emotion.RESENTMENT_EMOTION)
                            || e.Type.Equals(Emotion.ANGER_EMOTION)
                            || e.Type.Equals(Emotion.HATE_EMOTION))
                    {
                        facialExpression = "angry";
                    }
                    else if(e.Type.Equals(Emotion.DISTRESS_EMOTION) || e.Type.Equals(Emotion.PITTY_EMOTION))
                    {
                        facialExpression = "sad";
                    }
                    else if (e.Type.Equals(Emotion.JOY_EMOTION) || e.Type.Equals(Emotion.HAPPY_FOR_EMOTION))
                    {
                        facialExpression = "happy";
                    }
                    /*else if (es.Mood > 0.5f)
                    {
                        facialExpression = "happy";
                    }
                    else if (es.Mood < 0.5f)
                    {
                        facialExpression = "sad";
                    }
                    else
                    {
                        facialExpression = "natural";
                    }
                }
                else
                {
                    facialExpression = "natural";
                }

                Property<string> currentExpression = this.GetProperty<Property<string>>("expression");
                if (currentExpression != null)
                {
                    if (!currentExpression.Value.Equals(facialExpression))
                    {
                        currentExpression.Value = facialExpression;
                        Dictionary<string, object> arguments = new Dictionary<string, object>();
                        arguments.Add("expression", facialExpression);
                        Action a = this.GetAction("change-face");
                        a.Start(new Arguments(arguments));
                    }
                }*/
            }
            else if (msg.StartsWith("<Relations"))
            {
            }
            else if (msg.StartsWith("look-at"))
            {
                string[] aux = msg.Split(' ');
                //LookAt(aux[1]);
                parameters = new ActionParameters();
                parameters.Subject = this.Body.Name;
                parameters.ActionType = "look-at";
                parameters.Target = aux[1];
                this.lookATAction.Start(parameters);
            }
            else if (msg.StartsWith("<SpeechAct"))
            {
                SpeechActParameters speechParams = (SpeechActParameters) SpeechActParser.Instance.Parse(msg);
                if (speechParams.Meaning.Equals("episodesummary"))
                {
                    speechParams.Utterance = this.languageEngine.Narrate(speechParams.AMSummary);
                }
                else
                {
                    speechParams.Utterance = this.languageEngine.Say(speechParams);
                }

                EntityAction<ActionParameters> a = this.Body.getActionByName("SpeechAct") as EntityAction<ActionParameters>;
                if (a != null)
                {
                    a.Start(speechParams);
                }
            }
            else if (msg.StartsWith("<Action"))
            {
                parameters = (ActionParameters) ActionParametersParser.Instance.Parse(msg);
                EntityAction<ActionParameters> a = this.Body.getActionByName(parameters.ActionType) as EntityAction<ActionParameters>;
                if (a != null)
                {
                    a.Start(parameters);
                }
            }
            else if (msg.StartsWith(CANCEL_ACTION))
            {
                string actionMsg = msg.Substring(14);
                parameters = (ActionParameters)ActionParametersParser.Instance.Parse(actionMsg);
                EntityAction<ActionParameters> a = this.Body.getActionByName(parameters.ActionType) as EntityAction<ActionParameters>;
                if (a != null)
                {
                    a.Stop(false);
                }
            }
        }

        /*private void LookAt(string entityName)
        {
            Element e;
            Entity ne;
            System.Console.WriteLine(this.Body.Name + " looks at " + entityName);
            string msg =  LOOK_AT + " " + entityName;

            e = Simulation.Instance.Elements[Entity.getIDByName(entityName)];

            if(e is Entity)
            {
                ne = e as Entity;
                msg += " " + ne.getStringWithAllProperties();
            }
            

            Send(msg);
            
        }*/

        private void RegisterEventListeners(Element e)
        {
            if (!e.EventHandlers.Contains<IStarted>(this.ActionStarted))
            {
                e.EventHandlers.Add<IStarted>(this.ActionStarted);
            }
            if (!e.EventHandlers.Contains<ISucceeded>(this.ActionSucceeded))
            {
                e.EventHandlers.Add<ISucceeded>(this.ActionSucceeded);
            }
            if (!e.EventHandlers.Contains<IFailed>(this.ActionFailed))
            {
                e.EventHandlers.Add<IFailed>(this.ActionFailed);
            }
            if (!e.EventHandlers.Contains<IPropertyAdded>(this.PropertyAdded))
            {
                e.EventHandlers.Add<IPropertyAdded>(this.PropertyAdded);
                //e.EventHandlers.Add<IValueChanged<EntityProperty<object>>>(this.PropertyChanged);
            }
            if (!e.EventHandlers.Contains<IRestrictedPropertyChange>(this.RestrictedPropertyChange))
            {
                e.EventHandlers.Add<IRestrictedPropertyChange>(this.RestrictedPropertyChange);
            }
        }

        #region EventListeners

        public void ActionStarted(IStarted evt)
        {
            EntityAction<ActionParameters> action = evt.Action as EntityAction<ActionParameters>;
            if (action != null)
            {
                Console.WriteLine("-- RemoteCharacter.ActionStarted " + action.Name);
                if (this.receiverAlive)
                {
                    Send(ACTION_STARTED + " " + action.StartArguments.ToXML());
                }
            }  
        }

        public void ActionSucceeded(ISucceeded evt)
        {
            EntityAction<ActionParameters> action = evt.Action as EntityAction<ActionParameters>;
            if (action != null)
            {
                // DEBUG
                Console.WriteLine("-- RemoteCharacter.ActionEnded " + action.Name);

                if (this.receiverAlive)
                {

                    Send(ACTION_FINISHED + " " + action.StartArguments.ToXML());
                }
            }
        }

        public void ActionFailed(IFailed evt)
        {
            EntityAction<ActionParameters> action = evt.Action as EntityAction<ActionParameters>;
            if (action != null)
            {
                Console.WriteLine("-- RemoteCharacter.ActionFailed " + action.Name);
                if (this.receiverAlive)
                {
                    Send(ACTION_FAILED + " " + action.StartArguments.ToXML());
                }
            }
        }

        public void PropertyAdded(IPropertyAdded evt)
        {
            if (this.receiverAlive)
            {
                Entity entity = evt.Entity;
                IEntityProperty property = evt.Property;
                
                string msg = PROPERTY_CHANGED + " " + evt.Visibility + " " + entity.Name + " " + property.Name + " " + property.Value;
                Send(msg);
            }
        }

        /*public void PropertyChanged(IValueChanged<EntityProperty<object>> evt)
        {
            if (this.receiverAlive)
            {
                Entity entity = evt.Property.Parent;
                string msg = PROPERTY_CHANGED + " * " + entity.Name + " " + evt.Property.Name + " " + evt.NewValue;
                Send(msg);
            }
        }*/

        public void RestrictedPropertyChange(IRestrictedPropertyChange evt)
        {
            if (this.receiverAlive)
            {
                Event e = evt as Event;
                
                Entity entity = evt.Entity;
                string msg = PROPERTY_CHANGED + " " + evt.Visibility + " " + entity.Name + " " + evt.Property.Name + " " + evt.NewValue;
                Send(msg);
            }
        }

        public void ElementAdded(IElementAddedToSimulation evt)
        {
            Entity entity;
            
            if (evt.Element is Entity)
            {
                entity = evt.Element as Entity;
                RegisterEventListeners(entity);
                if (this.receiverAlive)
                {
                    
                    string msg = ENTITY_ADDED + " " + entity.Name;
                    Send(msg);
                }
            }
        }

        public void SimulationUpdated(ISimulationUpdated evt)
        {
            /*if (this.receiverAlive)
            {
                string msg = UPDATE;
                Send(msg);
            }*/
        }
        
        /*public void EntityAdded(IAddedTo<Entity,Simulation> evt)
        {
            //RegisterEventListeners(evt.Item);
            if (this.receiverAlive)
            {
                string msg = ENTITY_ADDED + " " + evt.Item.Name;
                Send(msg);
            }
        }*/

        public void DriveChanged(IDriveChanged evt)
        {
            if (this.receiverAlive)
            {
                string msg = "DRIVECHANGED " + evt.EntityName + " " + evt.DriveName + " " + evt.Value;
                Send(msg);
            }
        }

        #endregion

    #region RemoteCommunication

        private void ReceiveThread()
        {
            StreamReader socketReader = null;
            string msg = string.Empty;
            try
            {
                while (receiverAlive)
                {
                    if (socketReader == null)
                    {
                        socketReader = new StreamReader(this.socketStream, Encoding.UTF8);
                    }

                    msg = socketReader.ReadLine();

                    Parse(msg);
                }
            }
            catch (Exception e)
            {
                ApplicationLogger.Instance().WriteLine("Agent " + this.Body.Name + " lost the connection with the mind...: " + e.Message);
                ApplicationLogger.Instance().WriteLine(e.StackTrace);
            } 
        }

        public void Send(String msg)
        {
            byte[] aux = Encoding.UTF8.GetBytes(msg + "\n");
            this.socketStream.Write(aux, 0, aux.Length);
            this.socketStream.Flush();
        }

        #endregion
    }
}
