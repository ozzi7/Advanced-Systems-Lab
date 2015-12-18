using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.Threading;
using System.Windows.Forms;
using System.Globalization;
using System.IO;



namespace serviceTimeMW
{
    class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            OpenFileDialog dlg = new OpenFileDialog();
            dlg.Multiselect = true;

            dlg.FileName = "Document"; // Default file name
            dlg.DefaultExt = ".text"; // Default file extension
            dlg.Filter = "Text documents (.txt)|*.txt"; // Filter files by extension

            // Show save file dialog box
            DialogResult result = dlg.ShowDialog();

            /*Open all files from same type*/
            int ignoreEnd = 10;
            int ignoreBeginning = 5000;
            double divBy = 1000000;
            string saveTo = "DBV4CResponse.txt";
             //string saveTo = "responseTimeClients.txt";
            //string saveTo = "responseTimeDBThreads.txt";

            if (result == DialogResult.OK)
            {
                string pathoutput = @"C:\Users\Admin\Desktop\Dropbox\ProcessedLogs\" + saveTo;
                string path = (new System.IO.FileInfo(dlg.FileName)).DirectoryName;
                if (!File.Exists(pathoutput))
                {
                    using (StreamWriter sw = File.CreateText(pathoutput))
                    { }
                }
                /*Contains all measurements with start/end cutoff*/
                List<string> lines = new List<string>();

                /* cycle all threads of same cons*/
                foreach (String filename2 in dlg.FileNames)
                {
                        string fullPath = Path.Combine(path, filename2);
                        List<string> tempLines = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                        List<string> tempLines2 = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                        tempLines2 = tempLines.GetRange(ignoreBeginning, tempLines.Count - (ignoreEnd + ignoreBeginning));
                        lines.AddRange(tempLines2);
                }
                /*avg*/
                double total = 0;
                double temp = 0;
                double dtemp = 0;
                double stdDevTemp = 0;
                long dataCount = lines.Count();
                string[] twoLongs;
                for (int j = 0; j < dataCount; ++j)
                {
                    twoLongs = lines[j].Split(' ');
                    double.TryParse(twoLongs[1], out temp);
                    total += temp/divBy; /*->ns -> ms*/
                }
                double avg = total / dataCount;

                /*conf intervals*/
                for (int j = 0; j < dataCount; ++j)
                {
                    twoLongs = lines[j].Split(' ');
                    double.TryParse(twoLongs[1], out temp);
                    dtemp = temp;
                    dtemp /= divBy;
                    stdDevTemp += (avg - dtemp) * (avg - dtemp);
                }
                stdDevTemp /= dataCount;
                double stdDev = Math.Pow(stdDevTemp, 0.5);

                double zValue95 = 1.96; /* 95% confidence -> alpha 5%/2 -> Z value*/
                using (StreamWriter sw = File.AppendText(pathoutput))
                {
                    sw.Write(avg + " ");
                    sw.Write((avg - zValue95 * stdDev / Math.Pow(dataCount, 1 / 2)) + " ");
                    sw.Write((avg + zValue95 * stdDev / Math.Pow(dataCount, 1 / 2)) + "\n");
                }
            }
        }
    }
}
