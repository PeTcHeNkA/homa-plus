namespace CXFreezePlus {
    public partial class Form1 : Form {
        public static int GMODE = 0;
        public Form1() {
            InitializeComponent();
            FILE_DIALOG.Title = "Set PATH";
            FILE_DIALOG.Filter = "Java (*.exe)|*.exe|All files (*.*)|*.*";
            FILE_DIALOG.InitialDirectory = @"C:\";
            PATH.Text = Config.Default.PATH;
            onInitMode();
            ARGS.Text = Config.Default.LAST_ARGS;
        }

        private void onInitMode() {
            int mode = Config.Default.MODE;
            if (mode == 0) {
                FREEZE_B.Checked = true;
            } else {
                PROXY_B.Checked = true;
            }
            GMODE = mode;
        }


        private void OPEN_PATH_Click(object sender, EventArgs e) {
            if (DialogResult.OK == FILE_DIALOG.ShowDialog()) {
                String data = FILE_DIALOG.FileName;
                PATH.Text = data;
                Config.Default.PATH = data;
                Config.Default.Save();
            }
        }

        private void onUodateMode(int mode) {
            GMODE = mode;
            Config.Default.MODE = mode;
            Config.Default.Save();
        }


        private bool block = false;
        private void FREEZE_B_CheckedChanged(object sender, EventArgs e) {
            if (block) return;
            block = true;
            FREEZE_B.Checked = true;
            PROXY_B.Checked = false;
            onUodateMode(0);
            block = false;
        }

        private void PROXY_B_CheckedChanged(object sender, EventArgs e) {
            if (block) return;
            block = true;
            PROXY_B.Checked = true;
            FREEZE_B.Checked = false;
            onUodateMode(1);
            block = false;
        }

        private void RUN_Click(object sender, EventArgs e) {
            try {
                
                this.Hide();
                new Form2(ARGS.Text).ShowDialog();
                //Program.start(ARGS.Text);
            } catch (Exception ex) {
                MessageBox.Show(ex.Message);
            }
        }

        private void PATH_TextChanged(object sender, EventArgs e) {
            Config.Default.PATH = PATH.Text;
            Config.Default.Save();
        }


        private void HISTORY_OPEN_BTN_Click(object sender, EventArgs e) {
            new Form3().ShowDialog();
        }
    }
}