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
            string saveTo = "ThroughputClientMWLocalXClients.txt";

            if (result == DialogResult.OK)
            {
                string pathoutput = @"C:\Users\Admin\Desktop\pawidmer\logs\ProcessedLogs\" + saveTo;
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
                    data.Add(0);
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
            }
        }
    }
}
