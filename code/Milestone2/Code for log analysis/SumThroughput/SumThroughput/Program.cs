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
            int ignoreEnd = 1;
            int ignoreBeginning = 1;
            int nofclients = 1;
            string saveTo = "DBV"+ nofclients + "C.txt";
            string saveToSummary = "DBV" + nofclients + "CAvg.txt";

            if (result == DialogResult.OK)
            {
                string pathoutput = @"C:\Users\Admin\Desktop\Dropbox\ProcessedLogs\" + saveTo;
                string pathoutputSummary = @"C:\Users\Admin\Desktop\Dropbox\ProcessedLogs\" + saveToSummary;

                string path = (new System.IO.FileInfo(dlg.FileName)).DirectoryName;
                if (!File.Exists(pathoutput))
                {
                    using (StreamWriter sw = File.CreateText(pathoutput))
                    { }
                }

                List<int> data = new List<int>();

                /* cycle all threads*/
                foreach (String filename2 in dlg.FileNames)
                {
                    string fullPath = Path.Combine(path, filename2);
                    List<string> tempLines = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                    for (int k = 0; k < tempLines.Count; ++k)
                    {
                        List<int> numbers = tempLines[k].Split(' ').Select(int.Parse).ToList();
                        data.Add(numbers[0]);
                    }
                    break;
                }
                for (int k = 0; k < data.Count; ++k)
                {
                    data[k] = 0;
                }

                /* cycle all threads*/
                foreach (String filename2 in dlg.FileNames)
                {
                    string fullPath = Path.Combine(path, filename2);
                    List<string> tempLines = System.IO.File.ReadAllLines(fullPath, Encoding.Default).ToList();
                    for (int k = 0; k < tempLines.Count && k < data.Count; ++k)
                    {
                        List<int> numbers = tempLines[k].Split(' ').Select(int.Parse).ToList();
                        data[k] += numbers[0];
                    }
                }

                using (StreamWriter sw = File.AppendText(pathoutput))
                {
                    for (int k = ignoreBeginning; k < data.Count-ignoreEnd; ++k)
                        sw.Write((k- ignoreBeginning) + " " + data[k] + "\n");
                }
                /* calc average */
                double total = 0;
                double avg = 0;
                for (int k = ignoreBeginning; k < data.Count - ignoreEnd; ++k)
                    total += data[k];
                avg = total/(data.Count - (ignoreBeginning + ignoreEnd));

                /*Conf*/
                double stdDevTemp = 0;
                for (int k = ignoreBeginning; k < data.Count - ignoreEnd; ++k)
                {
                    stdDevTemp += (avg - data[k]) * (avg - data[k]);
                }
                stdDevTemp /= data.Count;
                double zValue95 = 1.96; /* 95% confidence -> alpha 5%/2 -> Z value*/

                double varSum = nofclients * stdDevTemp * stdDevTemp;
                stdDevTemp = Math.Sqrt(varSum);
                //using (StreamWriter sw = File.AppendText(pathoutputSummary))
                //{
                //    sw.Write(avg * nofclients + " ");
                //    sw.Write((avg * nofclients - zValue95 * stdDevTemp / Math.Pow(data.Count, 1 / 2)) + " ");
                //    sw.Write((avg * nofclients + zValue95 * stdDevTemp / Math.Pow(data.Count, 1 / 2)) + "\n");
                //}
                using (StreamWriter sw = File.AppendText(pathoutputSummary))
                {
                    sw.Write(avg + " ");
                    sw.Write((avg - zValue95 * stdDevTemp / Math.Pow(data.Count, 1 / 2)) + " ");
                    sw.Write((avg + zValue95 * stdDevTemp / Math.Pow(data.Count, 1 / 2)) + "\n");
                }
            }
        }
    }
}
