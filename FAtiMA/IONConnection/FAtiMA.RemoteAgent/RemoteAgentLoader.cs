using System;
using System.Text;
using System.Net.Sockets;
using ION.Core;
using System.Diagnostics;
using System.Net;
using ION.Core.Events;
using ION.Meta;

namespace FAtiMA.RemoteAgent
{
    public class LoadRemoteAgentArgs
    {
        public enum LoadMode {CREATENEW,RESTORE};

        public bool DebugMode { get; private set; }
        public LoadMode Mode { get; private set; }
        public string LanguageActs { get; private set; }
        public string UserLanguageActs { get; private set; }
        public string DisplayName { get; private set; }
        public string UserDirectory { get; private set; }
        public string Scenario { get; private set; }
        public string ScenariosFile { get; private set; }

        public LoadRemoteAgentArgs(bool debugMode,LoadMode mode, string scenariosFile, string scenario, string languageActs, string userLanguageActs, string displayName, string userDirectory)
        {
            this.DebugMode = debugMode;
            this.Mode = mode;
            this.LanguageActs = languageActs;
            this.UserLanguageActs = userLanguageActs;
            this.DisplayName = displayName;
            this.UserDirectory = userDirectory;
            this.Scenario = scenario;
            this.ScenariosFile = scenariosFile;
        }
    }

    public class RemoteAgentLoader 
    {
        
        public const string SEX = "sex";
        public const string ROLE = "role";
        

        //This parameter is not used in the action
        //It is here just so the SF can get it from the RunningContext of the action
        public const string NAME = "Name";
        public const string ACTION_NAME = "LoadRemoteAgent";

        protected const string EOM_TAG = "\n";
        protected const string EOF_TAG = "<EOF>";
        //only one connection at a time
        protected const int PENDING_CONNECTION_QUEUE_LENGTH = 10;
        protected const int INITIAL_SERVER_PORT = 46874;
        protected const int SERVER_PORT_RANGE = 100;
        protected const int bufferSize = 1024;

        protected Socket serverSocket;
        protected byte[] buffer = new byte[bufferSize];
        protected static int currentServerPort = INITIAL_SERVER_PORT;

        protected RemoteMind remoteAgent;

        public RemoteAgentLoader(RemoteMind remoteAgent)
        {
            this.remoteAgent = remoteAgent;
        }

        public void Launch(LoadRemoteAgentArgs args, int port)
        {
           
            string sex = this.remoteAgent.Sex.Value;
            string role = this.remoteAgent.Role.Value;

            StartServer(port);
            //once the Server is on, we can launch the java proccess

            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            proc.StartInfo.FileName = "\"c:/Program Files/java/jre6/bin/java.exe\"";
            proc.StartInfo.CreateNoWindow = true;
            proc.StartInfo.WindowStyle = ProcessWindowStyle.Maximized;
            proc.StartInfo.UseShellExecute = true;

            if (args.Mode == LoadRemoteAgentArgs.LoadMode.RESTORE && System.IO.File.Exists(args.UserDirectory + "/" + this.remoteAgent.Body.Name))
            {
                /*proc.StartInfo.Arguments = "-cp \"FAtiMA.jar;Language.jar\" " +
                "FAtiMA.Agent  " +
                "localhost " +
                currentServerPort + " " +
                args.UserDirectory + " " +
                this.remoteMind.Body.Name;*/
            }
            else
            {
                proc.StartInfo.Arguments = "-cp \"FAtiMA-Modular.jar;xmlenc-0.52.jar" +
                    "\"" +
                    " AgentLauncher " + args.ScenariosFile + " " + args.Scenario + " " + this.remoteAgent.Body.Name;
            }


            proc.Start();

        }
        
        
        #region RemoteConnection

        protected void StartServer(int port)
        {
            // Establish local endpoint...
            ApplicationLogger.Instance().WriteLine("FAtiMA: Creating the socket server..");
            IPAddress ipAddress = IPAddress.Any;
            IPEndPoint localEndPoint = new IPEndPoint(ipAddress, port);
            // Create a TCP/IP socket...
            this.serverSocket = new Socket(AddressFamily.Unspecified, SocketType.Stream, ProtocolType.Tcp);
            // Bind the socket...
            this.serverSocket.Bind(localEndPoint);
            this.serverSocket.Listen(PENDING_CONNECTION_QUEUE_LENGTH);

            // accept new connection...
            this.serverSocket.BeginAccept(new AsyncCallback(AcceptCallback), this.serverSocket);
            // wait until a connection is made before continuing...
            ApplicationLogger.Instance().WriteLine("FAtiMA: Comm ready!");
        }

        protected void AcceptCallback(IAsyncResult ar)
        {
            // get the socket handler...
            try
            {
                this.remoteAgent.Socket = ((Socket)ar.AsyncState).EndAccept(ar);
                ApplicationLogger.Instance().WriteLine("FAtiMA: Incoming connection ...");

                // create the state object...

                StringBuilder data = new StringBuilder();
                // begin receiving the connection request
                this.remoteAgent.Socket.BeginReceive(this.buffer, 0, bufferSize, 0,
                    new AsyncCallback(ReceiveCallback), data);

                //now that we received the connection, we can stop the serversocket
                ApplicationLogger.Instance().WriteLine("FAtiMA: Shuting Down...");
                this.serverSocket.Close();

            }
            catch (Exception e)
            {
                System.Console.WriteLine(e);
            }
        }

        protected void ReceiveCallback(IAsyncResult ar)
        {
            try
            {
                String receivedMsg = String.Empty;
                // read data from remote device...
                int bytesRead = +this.remoteAgent.Socket.EndReceive(ar);
                if (bytesRead > 0)
                {
                    // there may be more...
                    StringBuilder data = (StringBuilder)ar.AsyncState;
                    data.Append(Encoding.UTF8.GetString(this.buffer, 0, bytesRead));
                    // check for EOM.
                    receivedMsg = data.ToString();
                    int EomIndex = receivedMsg.IndexOf(EOM_TAG);
                    if (EomIndex > -1)
                    {
                        // finished receiving...
                        receivedMsg = receivedMsg.Substring(0, EomIndex);
                        // create the corresponding character
                        if (receivedMsg.StartsWith(this.remoteAgent.Body.Name))
                        {
                            //everything is ok, the agent that connected is the right agent
                            this.remoteAgent.Start();
                            //Aqui tenho de lançar o evento de que a mente acabou de se ligar com sucesso
                        }
                        else
                        {
                            //Serious error - someone else tried to connect to this RemoteCharacter
                            this.remoteAgent.Socket.Close();
                            this.remoteAgent.Socket = null;
                            //lançar um evento a dizer que houve merda
                        }
                    }
                    else
                    {
                        // not all data read. Read more...
                        this.remoteAgent.Socket.BeginReceive(this.buffer, 0, bufferSize, 0,
                            new AsyncCallback(ReceiveCallback), data);
                    }
                }
            }
            catch (Exception e)
            {
                System.Console.WriteLine(e);
            }
        }
        #endregion RemoteConnection
    }
}
