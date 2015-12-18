using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MeanValueAnalysis
{
    class Program
    {
        static void Main(string[] args)
        {
            double Nstart = 1;
            double Nend = 1000;
            double Z = 0.03;
            string saveTo = "MVA_Clients" + Nstart + "_" + Nend + "_ThinkTime" + (Z * 1000) +  "Prediction8xDB.txt";
            string pathoutput = @"C:\Users\Admin\Desktop\Dropbox\ProcessedLogs\" + saveTo;

            for (double N = Nstart; N <= Nend; N++)
            {
                List<double> Si = new List<double>();
                List<double> Vi = new List<double>();

                /*network*/
                for (int i = 1; i <= 32; ++i)
                {
                    Si.Add(0.000183);
                    Vi.Add(0.03125);
                }
                /*client T*/
                for (int i = 1; i <= 8; ++i)
                {
                    Si.Add(0.00008);
                    Vi.Add(0.125);
                }
                /*DB T*/
                for (int i = 1; i <= 8; ++i)
                {
                    Si.Add(0.00004);
                    Vi.Add(0.125);
                }
                /*network mwdb*/
                for (int i = 1; i <= 8; ++i)
                {
                    Si.Add(0.00023);
                    Vi.Add(0.125);
                }
                /*db*/
                for (int i = 1; i <= 4; ++i)
                {
                    Si.Add(0.0002797); // local 2.2376, 2.4648 remote /4 0.0005594 /8 0.0002797
                    Vi.Add(0.25);
                }
                int M = Si.Count;

                double X = 0.0;
                double R = 0.0;
                List<double> Ri = new List<double>(M);
                List<double> Qi = new List<double>(M);
                List<double> Ui = new List<double>(M);
                List<double> Xi = new List<double>(M);

                for (int i = 1; i <= M; ++i)
                {
                    Qi.Add(0.0);
                    Ri.Add(0.0);
                    Ui.Add(0.0);
                    Xi.Add(0.0);
                }
                for (int n = 1; n <= N; ++n)
                {
                    for (int i = 0; i < M; ++i)
                    {
                        Ri[i] = Si[i] * (1 + ((N - 1) / N) * Qi[i]);// (1 + ((N - 1) / N) * Qi[i]);
                    }

                    R = 0.0;
                    for (int i = 0; i < M; ++i)
                    {
                        R += Ri[i] * Vi[i];
                    }

                    X = N / (Z + R);

                    for (int i = 0; i < M; ++i)
                    {
                        Qi[i] = X * Vi[i] * Ri[i];
                    }
                }

                for (int j = 0; j < M; ++j)
                {
                    Xi[j] = X * Vi[j];
                }
                for (int j = 0; j < M; ++j)
                {
                    Ui[j] = X * Si[j] * Vi[j];
                }

                if (!File.Exists(pathoutput))
                {
                    using (StreamWriter sw = File.CreateText(pathoutput))
                    { }
                }
                using (StreamWriter sw = File.AppendText(pathoutput))
                {
                    sw.Write((int)N + " " + R + " " + X + " " + Ui[0] + " " + Ui[32] +" "+ Ui[41]+" " + Ui[50]+ " " + Ui[58]+" " + Xi[0] + " " + Xi[32] + " " + Xi[41] + " " + Xi[50] + " " + Xi[58]+"\n");
                }
                //Console.WriteLine("Throughput X: " + X);
                //Console.WriteLine("Response R: " + R + "\n");

                //for (int j = 0; j < M; ++j)
                //{
                //    Console.WriteLine("Throughput X" + j + ": " + Xi[j]);
                //    Console.WriteLine("Response R" + j + ": " + Ri[j]);
                //    Console.WriteLine("Jobs in device Q" + j + ": " + Qi[j]);
                //    Console.WriteLine("Util U" + j + ": " + Ui[j] + "\n");
                //}
                //Console.Read();
            }
        }
    }
}
