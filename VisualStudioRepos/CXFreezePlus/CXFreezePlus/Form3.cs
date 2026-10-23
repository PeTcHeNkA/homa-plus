namespace CXFreezePlus {
    public partial class Form3 : Form {
        public Form3() {
            InitializeComponent();
            if (Config.Default.CACHE.Length > 0) {
                TEXT.Text = Config.Default.CACHE;
            }
        }


        private void button3_Click(object sender, EventArgs e) {
            FolderBrowserDialog FD = new FolderBrowserDialog();
            FD.SelectedPath = Config.Default.SAVE_PATH;
            FD.Description = "set autosave log";

            if (FD.ShowDialog() == DialogResult.OK) {
                Config.Default.SAVE_PATH = FD.SelectedPath.Replace("\\", "/");
                Config.Default.Save();
            }
        }


        private void AUTO_SAVE_CB_CheckedChanged(object sender, EventArgs e) {
            if (AUTO_SAVE_CB.Checked) {
                if (Config.Default.SAVE_PATH == "") {
                    button3_Click(sender, e);
                    if (Config.Default.SAVE_PATH == "") {
                        AUTO_SAVE_CB.Checked = false;
                    }
                }
            }
            Config.Default.AUTO_SAVE = AUTO_SAVE_CB.Checked;
            Config.Default.Save();
        }


        private void SAVE_BTN_Click(object sender, EventArgs e) {
            SAVE_DIALOG.FileName = "history.log";
            if(Config.Default.SAVE_PATH != "") {
                SAVE_DIALOG.InitialDirectory = Config.Default.SAVE_PATH;
            }
            if (SAVE_DIALOG.ShowDialog() == DialogResult.OK) {
                StreamWriter file = new StreamWriter(SAVE_DIALOG.FileName.ToString());
                file.WriteLine(Config.Default.CACHE);
                file.Close();
                CLEAR_BTN_Click(sender, e);
                SAVE_BTN.Text = "SAVED SUCCESS!";
                new Thread(returnName).Start();
            }
        }


        private void returnName() {
            Thread.Sleep(5000);
            SAVE_BTN.Text = "SAVE";
        }


        private void CLEAR_BTN_Click(object sender, EventArgs e) {
            TEXT.Text = "";
            Config.Default.CACHE = "";
            Config.Default.Save();
        }
    }
}