using System.Diagnostics;

namespace CXFreezePlus {
    internal static class Program {
        public static String args = "";
        private static Process? proc;

        [STAThread]
        public static void Main(String[] arg) {
            if (arg.Length > 0) {
                args = parseArgs(arg);
            }
            Config.Default.CACHE += ("\r\n" + args + "\r\n");
            Config.Default.Save();

            ApplicationConfiguration.Initialize();

            Thread t = new Thread(input);
            t.IsBackground = true;
            t.Start();


            if (args == "") {
                Application.Run(new Form1());
            } else {
                Application.Run(new Form2(args));
                //onOut(args);
            }
        }
        private static String parseArgs(String[] args) {
            for(int i = 0; i < args.Length; i++) {
                if(args[i].Contains(" ")) {
                    args[i] = "\""+args[i]+"\"";
                }
            }
            return String.Join(" ", args);
        }


        public static void input() {
            while (true) {
                String? data = Console.ReadLine();
                if (data != null) {
                    onOut(data);
                } else {
                    Thread.Sleep(100);
                }
            }
        }

        private static StreamReader? reader;
        private static StreamWriter? writer;
        private static StreamReader? erstream;
        public static void start(String args0) {
            proc = new Process();
            proc.StartInfo.FileName = Config.Default.PATH;
            Config.Default.LAST_ARGS = args0;
            Config.Default.Save();
            if (args0 != null && args0 != "") {
                proc.StartInfo.Arguments = args0;
            }
            proc.StartInfo.RedirectStandardOutput = true;
            proc.StartInfo.RedirectStandardInput = true;
            proc.StartInfo.RedirectStandardError = true;
            proc.Start();

            erstream = proc.StandardError;
            reader = proc.StandardOutput;
            writer = proc.StandardInput;

            Thread t = new Thread(output);
            t.IsBackground = true;
            t.Start();
            Thread t2 = new Thread(errorout);
            t2.IsBackground = true;
            t2.Start();
            new Thread(onEnd).Start();
        }
        public static void onEnd() {
            if (proc == null) return;
            proc.WaitForExit();
            if (Form2.Instance != null) {
                Form2.Instance.onClose(proc.ExitCode);
            } else {
                Environment.Exit(proc.ExitCode);
            }
        }


        public static void runCmd(String s) {
            s = s.Trim();
            try {
                if (writer != null) {
                    writer.WriteLine(s);
                } else {
                    start(s);
                }
            } catch (Exception e) {
                MessageBox.Show(e.Message);
            }
        }

        private static void output() {
            if (reader == null) return;
            outputStram(reader);
        }
        private static void errorout() {
            if (erstream == null) return;
            outputStram(erstream);
        }

        private static void outputStram(StreamReader r) {
            try {
                while (true) {
                    //if (r != null) {
                        String? data = r.ReadLine();
                        if (data != null && data != "") {
                            onStr(data);
                        } else { 
                            Thread.Sleep(250); 
                        }
                    //} else {
                    //    Thread.Sleep(250);
                    //}
                }
            } catch (Exception e) { 
                MessageBox.Show(e.Message);
            }
        }

      


        public static void onOut(String data) {
            if (Form2.Instance != null) {
                Form2.Instance.insertLineToRun(data);
            }
        }
        

        public static void onStr(String data) {
            Console.WriteLine(data);
            if (Form2.Instance != null) {
                Form2.Instance.insertLine(data);
            } else {
                MessageBox.Show(data);
            }
        }
    }
}