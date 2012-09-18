
using System;
using System.IO;

namespace FAtiMA.RemoteAgent
{
    public class ApplicationLogger
    {
        private static ApplicationLogger appLogger = new ApplicationLogger();

        public static string LOG_DIR = "logs\\";
        private const string LOG_FILE = "_application_log.txt";
        private string subDir = "default";
        private static StreamWriter sWriter;        
        
        private ApplicationLogger()
        {
            //sWriter = File.CreateText(LOG_FILE); // If file exists it is overriden
        }

        ~ApplicationLogger()
        {
            Close();
        }        
        private void Close()
        {
            try
            {
                subDir = "default";
                sWriter.Close();
            }
            catch (Exception)
            {
                //ignore
            }            
        }
        
        public static ApplicationLogger Instance()
        {
            return appLogger;
        }
        
        public void Write(string message)
        {
            if ((sWriter == null) || (sWriter.BaseStream == null) || (!sWriter.BaseStream.CanWrite))
            {
                if ((subDir != "")&&(!Directory.Exists(LOG_DIR + subDir)))
                {
                    Directory.CreateDirectory(LOG_DIR + subDir);
                }
                string dateTimeStr = DateTime.Now.Year + "-" + DateTime.Now.Month + "-" + DateTime.Now.Day + " " + DateTime.Now.Hour + "_" +
                                     DateTime.Now.Minute + "_" + DateTime.Now.Second;
                sWriter = File.CreateText(LOG_DIR + subDir + "\\" + dateTimeStr + LOG_FILE);
            }            
            sWriter.Write(message);
            sWriter.Flush();
        }

        public void WriteLine(string message)
        {
            if ((sWriter == null) || (sWriter.BaseStream == null) || (!sWriter.BaseStream.CanWrite))
            {
                if ((subDir != "") && (!Directory.Exists(LOG_DIR + subDir)))
                {
                    Directory.CreateDirectory(LOG_DIR + subDir);
                }
                string dateTimeStr = DateTime.Now.Year + "-" + DateTime.Now.Month + "-" + DateTime.Now.Day + " " + DateTime.Now.Hour + "_" +
                     DateTime.Now.Minute + "_" + DateTime.Now.Second;
                sWriter = File.CreateText(LOG_DIR + subDir + "\\" + dateTimeStr + LOG_FILE);
            }  
            sWriter.WriteLine(message);
            sWriter.Flush();
        }
        
        public void SetSubDir(String subDir)
        {
            Close(); //New file will be created in the new dir
            this.subDir = subDir;
        }
    }
}
