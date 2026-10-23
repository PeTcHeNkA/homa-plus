namespace CXFreezePlus {
    public partial class Form2 : Form {
        public static Form2? Instance = null;

        public Form2() : this("") {}

        public Form2(String def) {
            Instance = this;
            InitializeComponent();
            Thread t = new Thread(tickS);
            t.IsBackground = true;
            t.Start();

            if (Config.Default.MODE == 1) {
                blocked = true;
                AUTO.Checked = true;
                blocked = false;
                CONSOLE.Text = ">> "+ def + "\r\n";
                Program.start(def);
            } else {
                INPUT.Text = def;
            }
        }

        private bool blocked = false;
        private void AUTO_CheckedChanged(object sender, EventArgs e) {
            if (blocked) return;
            blocked = true;
            if (AUTO.Checked) {
                Config.Default.MODE = 1;
                while(INPUT.Text != "") {
                    BTN_Click(sender, e);
                }
            } else {
                Config.Default.MODE = 0;
            }
            Config.Default.Save();
            blocked = false;
        }

        public List<String> s = new List<string>();
        public void insertLine(String text) {
            //MessageBox.Show(text);
            s.Add(text);
        }
        public void tickS() {
            while (true) {
                Thread.Sleep(60);
                onTickS();
            }
        }

        private void onTickS() {
            if (s.Count > 0) {
                String text = String.Join("\r\n", s) + "\r\n";
                s = new List<string>();
                CONSOLE.Text += text;
                CONSOLE.SelectionStart = CONSOLE.Text.Length;
                CONSOLE.ScrollToCaret();
            }
        }


        private List<String> lines = new List<String>();
        public void insertLineToRun(String text) {
            if (Config.Default.MODE == 1) {
                insertLine(">> "+text + "\r\n");
                Program.runCmd(text);
            } else {
                if(INPUT.Text == "") {
                    INPUT.Text = text;
                } else {
                    lines.Add(text);
                }
            }
        }


        private void BTN_Click(object sender, EventArgs e) {
            if (INPUT.Text == "") return;
            else {
                insertLine(">> " + INPUT.Text + "\r\n");
                Program.runCmd(INPUT.Text);
                if (lines.Count > 0) {
                    INPUT.Text = lines.Last();
                    lines.RemoveAt(lines.Count - 1);
                } else {
                    INPUT.Text = "";
                }
            }
        }


        protected override void OnFormClosing(FormClosingEventArgs e) {
            //base.OnFormClosing(e);
            onClose(0);
        }


        public void onClose(int code) {
            onTickS();
            Config.Default.CACHE = CONSOLE.Text;
            if (Config.Default.AUTO_SAVE) {
                StreamWriter file = new StreamWriter(
                    Config.Default.SAVE_PATH.Replace("\\", "/")
         + $"/history-{DateTime.Now.ToString().Replace(".", "-").Replace(":", "-").Replace(" ", "-")}.log"
                );
                file.WriteLine(Config.Default.CACHE);
                file.Close();
            }
            Config.Default.Save();
            Environment.Exit(0);
        }

    }
}