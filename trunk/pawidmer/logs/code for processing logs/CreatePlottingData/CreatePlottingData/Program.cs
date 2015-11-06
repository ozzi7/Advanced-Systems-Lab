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



namespace CreatePlottingData
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
            int ignoreEnd = 2;
            int ignoreBeginning = 2;
            double divBy = 1;
            string saveTo = "outputfilename.txt";
            List<string> cons = new List<string> { "Cons1.txt", "Cons2.txt", "Cons4.txt", "Cons6.txt", "Cons8.txt", "Cons10.txt", "Cons12.txt", "Cons16.txt", "Cons20.txt" };

            if (result == DialogResult.OK)
            {
                string pathoutput = @"C:\Users\Admin\Desktop\Synced\ASLCURRENT\ProcessedLogs\" + saveTo;
                string path = (new System.IO.FileInfo(dlg.FileName)).DirectoryName;
                if (!File.Exists(pathoutput))
                {
                    using (StreamWriter sw = File.CreateText(pathoutput))
                    { }
                }
                /* cycle all connections of same type*/
                for (int i = 0; i < cons.Count; ++i)
                {
                    int nofcons = 2 * i;
                    if (nofcons == 0) { nofcons = 1; }
                    if (nofcons == 16) { nofcons = 20; }
                    if (nofcons == 14) { nofcons = 16; }

                    List<string> lines = new List<string>();

                    /* cycle all threads of same cons*/
                    foreach (String filename2 in dlg.FileNames)
                    {
                        if (filename2.Contains(cons[i]))
                        {
                            string fullPath = Path.Combine(path, filename2);
                            List<string> tempLines = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                            List<string> tempLines2 = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                            tempLines2 = tempLines.GetRange(ignoreBeginning, tempLines.Count - (ignoreEnd + ignoreBeginning));
                            lines.AddRange(tempLines2);
                        }
                    }

                    double total = 0;
                    int temp = 0;
                    double dtemp = 0;
                    double stdDevTemp = 0;
                    int dataCount = lines.Count();
                    for (int j = 0; j < lines.Count(); ++j)
                    {
                        int.TryParse(lines[j], out temp);
                        total += temp;
                    }
                    double avg = total / dataCount;
                    avg /= divBy; /*->ns -> ms*/
                    for (int j = 0; j < lines.Count(); ++j)
                    {
                        int.TryParse(lines[j], out temp);
                        dtemp = temp;
                        dtemp /= divBy;
                        stdDevTemp += (avg - dtemp) * (avg - dtemp);
                    }
                    stdDevTemp /= dataCount;
                    double stdDev = Math.Pow(stdDevTemp, 0.5);

                    double zValue95 = 1.96; /* 95% confidence -> alpha 5%/2 -> Z value*/
                    using (StreamWriter sw = File.AppendText(pathoutput))
                    {
                        sw.Write(nofcons + " ");
                        sw.Write(avg + " ");
                        sw.Write((avg - zValue95 * stdDev / Math.Pow(dataCount, 1 / 2)) + " ");
                        sw.Write((avg + zValue95 * stdDev / Math.Pow(dataCount, 1 / 2)) + "\n");
                        // sw.Write((avg - stdDev) + " ");
                        // sw.Write((avg +stdDev ) + "\n");
                    }
                }
            }
        }
    }
}
