using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ResponseTimeLaw
{
    class Program
    {
        static void Main(string[] args)
        {
            double n = 32;
            double response = 4.98/ 1000;
            double thinktime = 0.02502;
            Console.WriteLine(n/(response+thinktime));
            Console.Read();
        }
    }
}
